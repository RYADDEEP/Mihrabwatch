package faith.mihrab.watch.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.Text
import faith.mihrab.watch.data.QIBLA_BEARING_BANGKOK
import faith.mihrab.watch.data.QiblaCompassRepository
import faith.mihrab.watch.data.bearingToCardinalName
import faith.mihrab.watch.ui.theme.MihrabBlack
import faith.mihrab.watch.ui.theme.MihrabGold
import faith.mihrab.watch.ui.theme.MihrabWatchTheme
import faith.mihrab.watch.ui.theme.MihrabWhite

// Design guide spec values (MIHRAB_WATCH_DESIGN_GUIDE.md Part 5 Screen 3, round-face adapted)
private val SafeZonePadding = 24.dp
private val CompassDiameter = 240.dp
private val CompassStroke = 2.dp
private val TickLength = 8.dp
private val TickStroke = 1.dp
private val ArrowLength = 80.dp
private val ArrowHalfBase = 8.dp
private val ArrowHeight = 24.dp
private val ArrowTipDotRadius = 3.dp
private val ArrowGlowRadius = 14.dp
private val CardinalEdgeInset = 6.dp
private val CompassToInfoSpacing = 16.dp
private val DegreesToNameSpacing = 4.dp

private val CompassRingBorder = Color(0x26FFFFFF)
private val CompassTick = Color(0x33FFFFFF)
private val DimCardinal = Color(0x80EBEBF5)
private val DirectionNameColor = Color(0x99EBEBF5)
private val LocationLabelColor = Color(0x4DEBEBF5)

@Composable
fun QiblaCompassScreen(repository: QiblaCompassRepository) {
    val heading by repository.heading.collectAsState()
    DisposableEffect(repository) {
        repository.start()
        onDispose { repository.stop() }
    }
    QiblaCompassContent(
        currentHeading = heading,
        qiblaBearing = QIBLA_BEARING_BANGKOK,
        directionName = bearingToCardinalName(QIBLA_BEARING_BANGKOK),
        locationLabel = "Bangkok",
    )
}

@Composable
private fun QiblaCompassContent(
    currentHeading: Float,
    qiblaBearing: Float,
    directionName: String,
    locationLabel: String,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MihrabBlack)
            .padding(SafeZonePadding),
    ) {
        Text(
            text = "Qibla",
            color = MihrabWhite,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.align(Alignment.TopCenter),
        )

        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            CompassRing(currentHeading = currentHeading, qiblaBearing = qiblaBearing)
            Spacer(modifier = Modifier.height(CompassToInfoSpacing))
            Text(
                text = "${qiblaBearing.toInt()}°",
                color = MihrabGold,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(DegreesToNameSpacing))
            Text(
                text = directionName,
                color = DirectionNameColor,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
            )
        }

        Text(
            text = "📍 $locationLabel",
            color = LocationLabelColor,
            fontSize = 13.sp,
            fontWeight = FontWeight.Normal,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
private fun CompassRing(currentHeading: Float, qiblaBearing: Float) {
    Box(modifier = Modifier.size(CompassDiameter)) {
        Canvas(modifier = Modifier.matchParentSize()) {
            drawCompass(currentHeading = currentHeading, qiblaBearing = qiblaBearing)
        }
        Text(
            text = "N",
            color = MihrabWhite,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = CardinalEdgeInset),
        )
        Text(
            text = "E",
            color = DimCardinal,
            fontSize = 15.sp,
            fontWeight = FontWeight.Normal,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = CardinalEdgeInset),
        )
        Text(
            text = "S",
            color = DimCardinal,
            fontSize = 15.sp,
            fontWeight = FontWeight.Normal,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = CardinalEdgeInset),
        )
        Text(
            text = "W",
            color = DimCardinal,
            fontSize = 15.sp,
            fontWeight = FontWeight.Normal,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = CardinalEdgeInset),
        )
    }
}

private fun DrawScope.drawCompass(currentHeading: Float, qiblaBearing: Float) {
    val strokePx = CompassStroke.toPx()
    val ringRadius = size.minDimension / 2f - strokePx / 2f
    val center = Offset(size.width / 2f, size.height / 2f)

    drawCircle(
        color = CompassRingBorder,
        radius = ringRadius,
        center = center,
        style = Stroke(width = strokePx),
    )

    val tickStart = ringRadius - strokePx / 2f
    val tickEnd = tickStart - TickLength.toPx()
    val tickStrokePx = TickStroke.toPx()
    for (i in 0 until 12) {
        rotate(degrees = i * 30f, pivot = center) {
            drawLine(
                color = CompassTick,
                start = Offset(center.x, center.y - tickStart),
                end = Offset(center.x, center.y - tickEnd),
                strokeWidth = tickStrokePx,
            )
        }
    }

    // Arrow rotates (qiblaBearing - currentHeading) clockwise from "up".
    // At heading=0 with bearing=294, tip lands in the upper-left (NW) quadrant.
    val arrowRotation = qiblaBearing - currentHeading
    val arrowLengthPx = ArrowLength.toPx()
    val arrowHeightPx = ArrowHeight.toPx()
    val arrowHalfBasePx = ArrowHalfBase.toPx()
    val glowRadiusPx = ArrowGlowRadius.toPx()
    val tipDotRadiusPx = ArrowTipDotRadius.toPx()

    rotate(degrees = arrowRotation, pivot = center) {
        val tip = Offset(center.x, center.y - arrowLengthPx)
        val baseLeft = Offset(center.x - arrowHalfBasePx, center.y - arrowLengthPx + arrowHeightPx)
        val baseRight = Offset(center.x + arrowHalfBasePx, center.y - arrowLengthPx + arrowHeightPx)

        drawCircle(
            color = MihrabGold.copy(alpha = 0.4f),
            radius = glowRadiusPx,
            center = tip,
        )

        val path = Path().apply {
            moveTo(tip.x, tip.y)
            lineTo(baseRight.x, baseRight.y)
            lineTo(baseLeft.x, baseLeft.y)
            close()
        }
        drawPath(path = path, color = MihrabGold)

        drawCircle(
            color = MihrabGold,
            radius = tipDotRadiusPx,
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
            qiblaBearing = QIBLA_BEARING_BANGKOK,
            directionName = "West-Northwest",
            locationLabel = "Bangkok",
        )
    }
}
