package faith.mihrab.watch.services

import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.LongTextComplicationData
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.RangedValueComplicationData
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import faith.mihrab.watch.R
import faith.mihrab.watch.data.NextPrayerView
import faith.mihrab.watch.data.SyncPayloadCache
import faith.mihrab.watch.data.nextOrFallback
import faith.mihrab.watch.data.nextPrayerView
import faith.mihrab.watch.data.resolveWindow
import faith.mihrab.watch.data.windowProgress
import java.time.Instant

class MihrabComplicationDataSourceService : SuspendingComplicationDataSourceService() {

    override fun getPreviewData(type: ComplicationType): ComplicationData? =
        build(type, previewData(), progress = PREVIEW_PROGRESS)

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData? {
        val now = Instant.now()
        val payload = SyncPayloadCache(applicationContext).load()
        // One window, one `now` — the gauge below and the Prayer Home ring are the same maths,
        // so two arcs on one wrist can never disagree at the same moment.
        val window = payload?.let { resolveWindow(it, now = now) }
        val resolved = payload?.copy(nextPrayer = window.nextOrFallback(payload))
        val view = nextPrayerView(applicationContext, resolved, now)
        return build(request.complicationType, view, windowProgress(window, now))
    }

    private fun previewData(): NextPrayerView = NextPrayerView(
        name = applicationContext.getString(R.string.prayer_maghrib),
        time = "18:22",
        countdownShort = applicationContext.getString(R.string.watch_countdown_short_m, 20),
        countdownLong = applicationContext.getString(R.string.watch_countdown_short_m, 20),
    )

    private fun build(type: ComplicationType, p: NextPrayerView, progress: Float?): ComplicationData? {
        val description = PlainComplicationText
            .Builder("Mihrab next prayer ${p.name} ${p.countdownLong}")
            .build()
        return when (type) {
            ComplicationType.SHORT_TEXT -> ShortTextComplicationData.Builder(
                text = PlainComplicationText.Builder(p.name.take(7)).build(),
                contentDescription = description,
            )
                .setTitle(PlainComplicationText.Builder(p.countdownShort).build())
                .build()

            ComplicationType.LONG_TEXT -> LongTextComplicationData.Builder(
                text = PlainComplicationText.Builder("${p.name} ${p.time}").build(),
                contentDescription = description,
            )
                .setTitle(PlainComplicationText.Builder(p.countdownLong).build())
                .build()

            // An unknown window reads as an empty gauge rather than a blank slot — the text
            // still names the prayer, which is the part the watch does know.
            ComplicationType.RANGED_VALUE -> RangedValueComplicationData.Builder(
                value = ((progress ?: 0f) * 100f).coerceIn(0f, 100f),
                min = 0f,
                max = 100f,
                contentDescription = description,
            )
                .setText(PlainComplicationText.Builder(p.name.take(7)).build())
                .setTitle(PlainComplicationText.Builder(p.countdownShort).build())
                .build()

            else -> null
        }
    }

    private companion object {
        const val PREVIEW_PROGRESS = 0.65f
    }
}
