package faith.mihrab.watch.services

import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.LongTextComplicationData
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import faith.mihrab.watch.data.NextPrayer
import faith.mihrab.watch.data.NextPrayerProvider

class MihrabComplicationDataSourceService : SuspendingComplicationDataSourceService() {

    override fun getPreviewData(type: ComplicationType): ComplicationData? =
        build(type, NextPrayerProvider.current())

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData? =
        build(request.complicationType, NextPrayerProvider.current())

    private fun build(type: ComplicationType, p: NextPrayer): ComplicationData? {
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
