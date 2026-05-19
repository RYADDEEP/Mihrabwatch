package faith.mihrab.watch.services

import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.LongTextComplicationData
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import faith.mihrab.watch.data.NextPrayerView
import faith.mihrab.watch.data.SyncPayloadCache
import faith.mihrab.watch.data.nextPrayerView
import faith.mihrab.watch.data.resolveNextPrayer

class MihrabComplicationDataSourceService : SuspendingComplicationDataSourceService() {

    private val previewData = NextPrayerView(
        name = "Maghrib",
        time = "18:22",
        countdownShort = "20m",
        countdownLong = "in 20 minutes",
    )

    override fun getPreviewData(type: ComplicationType): ComplicationData? =
        build(type, previewData)

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData? {
        val payload = SyncPayloadCache(applicationContext).load()
        return build(
            request.complicationType,
            nextPrayerView(applicationContext, payload?.copy(nextPrayer = resolveNextPrayer(payload))),
        )
    }

    private fun build(type: ComplicationType, p: NextPrayerView): ComplicationData? {
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

            else -> null
        }
    }
}
