package faith.mihrab.watch.services

import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService

// Session 1 stub. Session 6 replaces with live next-prayer data
// sourced from the phone via device_pairings.sync_payload.
class MihrabComplicationDataSourceService : SuspendingComplicationDataSourceService() {

    override fun getPreviewData(type: ComplicationType): ComplicationData? {
        if (type != ComplicationType.SHORT_TEXT) return null
        return buildPlaceholder()
    }

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData? {
        if (request.complicationType != ComplicationType.SHORT_TEXT) return null
        return buildPlaceholder()
    }

    private fun buildPlaceholder(): ShortTextComplicationData =
        ShortTextComplicationData.Builder(
            text = PlainComplicationText.Builder("Dhuhr 13:30").build(),
            contentDescription = PlainComplicationText.Builder("Mihrab next prayer").build(),
        ).build()
}
