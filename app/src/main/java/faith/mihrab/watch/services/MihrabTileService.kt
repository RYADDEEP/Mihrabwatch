package faith.mihrab.watch.services

import androidx.wear.protolayout.ColorBuilders.argb
import androidx.wear.protolayout.DimensionBuilders.dp
import androidx.wear.protolayout.DimensionBuilders.sp
import androidx.wear.protolayout.LayoutElementBuilders.Box
import androidx.wear.protolayout.LayoutElementBuilders.FontStyle
import androidx.wear.protolayout.LayoutElementBuilders.Text
import androidx.wear.protolayout.ModifiersBuilders.Background
import androidx.wear.protolayout.ModifiersBuilders.Modifiers
import androidx.wear.protolayout.ResourceBuilders.Resources
import androidx.wear.protolayout.TimelineBuilders.Timeline
import androidx.wear.tiles.RequestBuilders.ResourcesRequest
import androidx.wear.tiles.RequestBuilders.TileRequest
import androidx.wear.tiles.TileBuilders.Tile
import androidx.wear.tiles.TileService
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture

// Session 1 stub. Session 6 replaces this with the real next-prayer Tile
// (countdown to next prayer, prayer color, last-sync indicator).
class MihrabTileService : TileService() {

    override fun onTileRequest(
        requestParams: TileRequest,
    ): ListenableFuture<Tile> {
        val tile = Tile.Builder()
            .setResourcesVersion(RESOURCES_VERSION)
            .setTileTimeline(
                Timeline.fromLayoutElement(
                    Box.Builder()
                        .setWidth(dp(192f))
                        .setHeight(dp(192f))
                        .setModifiers(
                            Modifiers.Builder()
                                .setBackground(
                                    Background.Builder()
                                        .setColor(argb(0xFF000000.toInt()))
                                        .build(),
                                )
                                .build(),
                        )
                        .addContent(
                            Text.Builder()
                                .setText("Mihrab")
                                .setFontStyle(
                                    FontStyle.Builder()
                                        .setColor(argb(0xFFD4A537.toInt()))
                                        .setSize(sp(24f))
                                        .build(),
                                )
                                .build(),
                        )
                        .build(),
                ),
            )
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

    private companion object {
        const val RESOURCES_VERSION = "0"
    }
}
