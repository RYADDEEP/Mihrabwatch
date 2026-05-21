package faith.mihrab.watch.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import faith.mihrab.watch.R
import faith.mihrab.watch.data.QiblaCompassRepository
import faith.mihrab.watch.data.SyncPayload
import faith.mihrab.watch.data.SyncPayloadState
import faith.mihrab.watch.ui.theme.MihrabBlack
import faith.mihrab.watch.ui.theme.MihrabGold
import faith.mihrab.watch.ui.theme.MihrabGoldBright
import faith.mihrab.watch.ui.theme.MihrabWatchTheme
import faith.mihrab.watch.ui.theme.MihrabWhite
import kotlinx.coroutines.flow.Flow

// Design guide spec values (MIHRAB_WATCH_DESIGN_GUIDE.md Part 5 Screen 3, round-face adapted)
private val SafeZonePadding = 24.dp
private val CompassDiameter = 240.dp
private val CompassStroke = 2.5.dp
private val InnerGlowRingInset = 2.dp
private val InnerGlowRingStroke = 1.dp
private val CardinalTickLength = 10.dp
private val CardinalTickStroke = 1.5.dp
private val MinorTickLength = 6.dp
private val MinorTickStroke = 1.dp
private val ArrowLength = 80.dp
private val ArrowHalfBase = 6.dp
private val ArrowHeight = 24.dp
private val ArrowTipDotRadius = 3.dp
private val ArrowTipCoreRadius = 1.5.dp
private val ArrowGlowInnerRadius = 8.dp
private val ArrowGlowOuterRadius = 20.dp
private val CardinalEdgeInset = 6.dp

private val CompassRingBorder = Color(0x26FFFFFF)
private val InnerGlowRingColor = Color(0x0DFFFFFF)
private val CardinalTickColor = Color(0x66FFFFFF)
private val MinorTickColor = Color(0x33FFFFFF)
private val DimCardinal = Color(0x80EBEBF5)

@Composable
fun QiblaCompassScreen(
    repository: QiblaCompassRepository,
    payloadFlow: Flow<SyncPayloadState>?,
) {
    val heading by repository.heading.collectAsState()
    DisposableEffect(repository) {
        repository.start()
        onDispose { repository.stop() }
    }

    val state = payloadFlow
        ?.collectAsState(initial = SyncPayloadState.Loading)
        ?.value
    // Best-effort across states: a bearing is rendered whenever any payload carries one,
    // including a newer-than-supported payload (graceful degradation, schema §5/§8.C).
    val payload: SyncPayload? = when (state) {
        is SyncPayloadState.Ready -> state.payload
        is SyncPayloadState.Error -> state.lastGood
        is SyncPayloadState.UnsupportedVersion -> state.payload
        else -> null
    }
    // Graceful degradation (schema §5/§8.D): render the bearing whenever we have one.
    val qiblaBearing = payload?.qibla?.bearingDegrees?.toFloat()

    QiblaCompassContent(
        currentHeading = heading,
        qiblaBearing = qiblaBearing,
    )
}

@Composable
private fun QiblaCompassContent(
    currentHeading: Float,
    qiblaBearing: Float?,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MihrabBlack)
            .padding(SafeZonePadding),
        contentAlignment = Alignment.Center,
    ) {
        CompassRing(
            currentHeading = currentHeading,
            qiblaBearing = qiblaBearing,
        )
        if (qiblaBearing == null) {
            Text(
                text = stringResource(R.string.sync_qibla_missing),
                color = MihrabWhite,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun CompassRing(
    currentHeading: Float,
    qiblaBearing: Float?,
) {
    Box(modifier = Modifier.size(CompassDiameter)) {
        Canvas(modifier = Modifier.matchParentSize()) {
            drawCompass(currentHeading = currentHeading, qiblaBearing = qiblaBearing)
        }

        // N — primary orientation anchor, glowing halo
        Text(
            text = stringResource(R.string.watch_compass_cardinal_north),
            color = MihrabWhite,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = CardinalEdgeInset),
            style = TextStyle(
                shadow = Shadow(
                    color = MihrabWhite.copy(alpha = 0.5f),
                    blurRadius = 8f,
                ),
            ),
        )

        // E/S/W — subordinate, smaller, medium weight, no glow
        Text(
            text = stringResource(R.string.watch_compass_cardinal_east),
            color = DimCardinal,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = CardinalEdgeInset),
        )
        Text(
            text = stringResource(R.string.watch_compass_cardinal_south),
            color = DimCardinal,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = CardinalEdgeInset),
        )
        Text(
            text = stringResource(R.string.watch_compass_cardinal_west),
            color = DimCardinal,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = CardinalEdgeInset),
        )
    }
}

private fun DrawScope.drawCompass(currentHeading: Float, qiblaBearing: Float?) {
    val strokePx = CompassStroke.toPx()
    val ringRadius = size.minDimension / 2f - strokePx / 2f
    val center = Offset(size.width / 2f, size.height / 2f)

    // Main ring border
    drawCircle(
        color = CompassRingBorder,
        radius = ringRadius,
        center = center,
        style = Stroke(width = strokePx),
    )

    // Inner faint glow ring — adds depth, very subtle
    val innerGlowRadius = ringRadius - InnerGlowRingInset.toPx()
    drawCircle(
        color = InnerGlowRingColor,
        radius = innerGlowRadius,
        center = center,
        style = Stroke(width = InnerGlowRingStroke.toPx()),
    )

    // Tick marks with cardinal/minor hierarchy
    val tickStart = ringRadius - strokePx / 2f
    val cardinalTickLengthPx = CardinalTickLength.toPx()
    val minorTickLengthPx = MinorTickLength.toPx()
    val cardinalTickStrokePx = CardinalTickStroke.toPx()
    val minorTickStrokePx = MinorTickStroke.toPx()

    for (i in 0 until 12) {
        val isCardinal = i % 3 == 0  // i=0 (N), i=3 (E), i=6 (S), i=9 (W)
        val tickLength = if (isCardinal) cardinalTickLengthPx else minorTickLengthPx
        val tickStroke = if (isCardinal) cardinalTickStrokePx else minorTickStrokePx
        val tickColor = if (isCardinal) CardinalTickColor else MinorTickColor

        rotate(degrees = i * 30f, pivot = center) {
            drawLine(
                color = tickColor,
                start = Offset(center.x, center.y - tickStart),
                end = Offset(center.x, center.y - tickStart + tickLength),
                strokeWidth = tickStroke,
            )
        }
    }

    // No bearing from the phone yet — draw ring + ticks only, the screen shows a sync prompt.
    if (qiblaBearing == null) return

    // Qibla arrow with gradient fill + two-layer glow + jewel tip
    val arrowRotation = qiblaBearing - currentHeading
    val arrowLengthPx = ArrowLength.toPx()
    val arrowHeightPx = ArrowHeight.toPx()
    val arrowHalfBasePx = ArrowHalfBase.toPx()
    val glowInnerPx = ArrowGlowInnerRadius.toPx()
    val glowOuterPx = ArrowGlowOuterRadius.toPx()
    val tipDotPx = ArrowTipDotRadius.toPx()
    val tipCorePx = ArrowTipCoreRadius.toPx()

    rotate(degrees = arrowRotation, pivot = center) {
        val tip = Offset(center.x, center.y - arrowLengthPx)
        val baseLeft = Offset(center.x - arrowHalfBasePx, center.y - arrowLengthPx + arrowHeightPx)
        val baseRight = Offset(center.x + arrowHalfBasePx, center.y - arrowLengthPx + arrowHeightPx)

        // Outer halo — soft bloom around tip
        drawCircle(
            color = MihrabGold.copy(alpha = 0.2f),
            radius = glowOuterPx,
            center = tip,
        )
        // Inner glow — brighter halo
        drawCircle(
            color = MihrabGold.copy(alpha = 0.6f),
            radius = glowInnerPx,
            center = tip,
        )

        // Arrow body — gradient from MihrabGold (base) to MihrabGoldBright (tip)
        val arrowBrush = Brush.linearGradient(
            colors = listOf(MihrabGold, MihrabGoldBright),
            start = Offset(center.x, center.y - arrowLengthPx + arrowHeightPx),  // base
            end = Offset(center.x, center.y - arrowLengthPx),                    // tip
        )
        val path = Path().apply {
            moveTo(tip.x, tip.y)
            lineTo(baseRight.x, baseRight.y)
            lineTo(baseLeft.x, baseLeft.y)
            close()
        }
        drawPath(path = path, brush = arrowBrush)

        // Tip dot — 6dp full gold
        drawCircle(
            color = MihrabGold,
            radius = tipDotPx,
            center = tip,
        )
        // Tip jewel core — 3dp pure white inside the gold dot
        drawCircle(
            color = MihrabWhite,
            radius = tipCorePx,
            center = tip,
        )
    }
}

@Preview(
    device = "id:wearos_large_round",
    showBackground = true,
    backgroundColor = 0xFF000000,
)
@Composable
private fun QiblaCompassPreview() {
    MihrabWatchTheme {
        QiblaCompassContent(
            currentHeading = 0f,
            qiblaBearing = 294f,
        )
    }
}
