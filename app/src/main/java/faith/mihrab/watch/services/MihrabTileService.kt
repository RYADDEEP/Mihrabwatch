package faith.mihrab.watch.services

import androidx.wear.protolayout.ActionBuilders
import androidx.wear.protolayout.ColorBuilders.argb
import androidx.wear.protolayout.DimensionBuilders.dp
import androidx.wear.protolayout.DimensionBuilders.expand
import androidx.wear.protolayout.DimensionBuilders.sp
import androidx.wear.protolayout.LayoutElementBuilders.Box
import androidx.wear.protolayout.LayoutElementBuilders.Column
import androidx.wear.protolayout.LayoutElementBuilders.FONT_WEIGHT_BOLD
import androidx.wear.protolayout.LayoutElementBuilders.FontStyle
import androidx.wear.protolayout.LayoutElementBuilders.HORIZONTAL_ALIGN_CENTER
import androidx.wear.protolayout.LayoutElementBuilders.Spacer
import androidx.wear.protolayout.LayoutElementBuilders.Text
import androidx.wear.protolayout.ModifiersBuilders.Background
import androidx.wear.protolayout.ModifiersBuilders.Clickable
import androidx.wear.protolayout.ModifiersBuilders.Modifiers
import androidx.wear.protolayout.ResourceBuilders.Resources
import androidx.wear.protolayout.TimelineBuilders.Timeline
import androidx.wear.tiles.RequestBuilders.ResourcesRequest
import androidx.wear.tiles.RequestBuilders.TileRequest
import androidx.wear.tiles.TileBuilders.Tile
import androidx.wear.tiles.TileService
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import faith.mihrab.watch.data.NextPrayer
import faith.mihrab.watch.data.NextPrayerProvider

class MihrabTileService : TileService() {

    override fun onTileRequest(
        requestParams: TileRequest,
    ): ListenableFuture<Tile> {
        val prayer = NextPrayerProvider.current()
        val tile = Tile.Builder()
            .setResourcesVersion(RESOURCES_VERSION)
            .setFreshnessIntervalMillis(60_000L)
            .setTileTimeline(Timeline.fromLayoutElement(rootLayout(prayer)))
            .build()
        return Futures.immediateFuture(tile)
    }

    override fun onTileResourcesRequest(
        requestParams: ResourcesRequest,
    ): ListenableFuture<Resources> {
        val resources = Resources.Builder()
            .setVersion(RESOURCES_VERSION)
            .build()
        return Futures.immediateFuture(resources)
    }

    private fun rootLayout(p: NextPrayer): Box =
        Box.Builder()
            .setWidth(expand())
            .setHeight(expand())
            .setModifiers(
                Modifiers.Builder()
                    .setBackground(
                        Background.Builder()
                            .setColor(argb(COLOR_BLACK))
                            .build(),
                    )
                    .setClickable(launchAppClickable())
                    .build(),
            )
            .addContent(
                Column.Builder()
                    .setHorizontalAlignment(HORIZONTAL_ALIGN_CENTER)
                    .addContent(label(text = "Next Prayer", sizeSp = 13f, colorArgb = COLOR_LABEL_DIM))
                    .addContent(spacer(8f))
                    .addContent(label(text = p.name, sizeSp = 32f, colorArgb = COLOR_GOLD, bold = true))
                    .addContent(spacer(4f))
                    .addContent(label(text = p.time, sizeSp = 20f, colorArgb = COLOR_GOLD_BRIGHT))
                    .addContent(spacer(8f))
                    .addContent(label(text = p.countdownLong, sizeSp = 13f, colorArgb = COLOR_GOLD_DIM))
                    .build(),
            )
            .build()

    private fun launchAppClickable(): Clickable =
        Clickable.Builder()
            .setId("open_mihrab")
            .setOnClick(
                ActionBuilders.LaunchAction.Builder()
                    .setAndroidActivity(
                        ActionBuilders.AndroidActivity.Builder()
                            .setPackageName(applicationContext.packageName)
                            .setClassName("faith.mihrab.watch.MainActivity")
                            .build(),
                    )
                    .build(),
            )
            .build()

    private fun label(
        text: String,
        sizeSp: Float,
        colorArgb: Int,
        bold: Boolean = false,
    ): Text {
        val fontStyle = FontStyle.Builder()
            .setSize(sp(sizeSp))
            .setColor(argb(colorArgb))
            .apply { if (bold) setWeight(FONT_WEIGHT_BOLD) }
            .build()
        return Text.Builder()
            .setText(text)
            .setFontStyle(fontStyle)
            .build()
    }

    private fun spacer(heightDp: Float): Spacer =
        Spacer.Builder().setHeight(dp(heightDp)).build()

    private companion object {
        const val RESOURCES_VERSION = "0"

        // Mihrab theme tokens (kept in sync with ui/theme/Color.kt)
        const val COLOR_BLACK = 0xFF000000.toInt()
        const val COLOR_GOLD = 0xFFD4A537.toInt()
        const val COLOR_GOLD_BRIGHT = 0xFFF5C842.toInt()
        const val COLOR_LABEL_DIM = 0x99FFFFFF.toInt()
        const val COLOR_GOLD_DIM = 0x99D4A537.toInt()
    }
}
