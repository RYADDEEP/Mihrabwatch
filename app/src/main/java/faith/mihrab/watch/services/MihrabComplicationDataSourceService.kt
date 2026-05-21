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
import faith.mihrab.watch.data.nextPrayerView
import faith.mihrab.watch.data.resolveNextPrayer
import faith.mihrab.watch.data.ringProgress
import java.time.Instant

class MihrabComplicationDataSourceService : SuspendingComplicationDataSourceService() {

    override fun getPreviewData(type: ComplicationType): ComplicationData? =
        build(type, previewData(), progress = PREVIEW_PROGRESS)

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData? {
        val resolved = SyncPayloadCache(applicationContext).load()
            ?.let { it.copy(nextPrayer = resolveNextPrayer(it)) }
        val now = Instant.now()
        val view = nextPrayerView(applicationContext, resolved, now)
        val progress = ringProgress(resolved?.lastUpdated, resolved?.nextPrayer?.time, now)
        return build(request.complicationType, view, progress)
    }

    private fun previewData(): NextPrayerView = NextPrayerView(
        name = applicationContext.getString(R.string.prayer_maghrib),
        time = "18:22",
        countdownShort = applicationContext.getString(R.string.watch_countdown_short_m, 20),
        countdownLong = applicationContext.getString(R.string.watch_countdown_long_m, 20),
    )

    private fun build(type: ComplicationType, p: NextPrayerView, progress: Float): ComplicationData? {
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

            ComplicationType.RANGED_VALUE -> RangedValueComplicationData.Builder(
                value = (progress * 100f).coerceIn(0f, 100f),
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
