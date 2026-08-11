package faith.mihrab.watch.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.wear.compose.material3.Text
import faith.mihrab.watch.R
import faith.mihrab.watch.data.QiblaCompassRepository
import faith.mihrab.watch.data.SyncPayload
import faith.mihrab.watch.data.SyncPayloadState
import faith.mihrab.watch.ui.theme.MihrabBlack
import faith.mihrab.watch.ui.theme.MihrabCompassRing
import faith.mihrab.watch.ui.theme.MihrabGold
import faith.mihrab.watch.ui.theme.MihrabGoldBright
import faith.mihrab.watch.ui.theme.MihrabWatchTheme
import faith.mihrab.watch.ui.theme.MihrabWhite
import faith.mihrab.watch.ui.theme.WatchScale
import kotlinx.coroutines.flow.Flow

// Arrow geometry, all as fractions of the compass radius R, so the tip and its glow stay
// inside the ring at every screen size. The glow's outer edge lands at 0.98·R.
private const val ARROW_TIP = 0.78f
private const val ARROW_BASE = 0.50f
private const val ARROW_HALF_BASE = 0.075f
private const val GLOW_OUTER = 0.20f
private const val GLOW_INNER = 0.09f
private const val TIP_DOT = 0.042f
private const val TIP_JEWEL = 0.021f

private const val GLOW_OUTER_ALPHA = 0.2f
private const val GLOW_INNER_ALPHA = 0.6f

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
    val qiblaBearing = payload?.qibla?.bearingDegrees?.toFloat()

    QiblaCompassContent(
        currentHeading = heading,
        qiblaBearing = qiblaBearing,
        hasSensor = repository.hasOrientationSensor,
    )
}

@Composable
private fun QiblaCompassContent(
    currentHeading: Float?,
    qiblaBearing: Float?,
    hasSensor: Boolean,
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(MihrabBlack),
        contentAlignment = Alignment.Center,
    ) {
        val scale = WatchScale.from(maxWidth, maxHeight)

        CompassRing(
            diameter = scale.compassDiameter,
            stroke = scale.compassStroke,
            // The arrow is drawn only when there is both somewhere to point and a way to know
            // which way is which. Anything else would be a confident, meaningless arrow.
            arrowRotation = if (qiblaBearing != null && currentHeading != null) {
                qiblaBearing - currentHeading
            } else {
                null
            },
        )

        val message = when {
            qiblaBearing == null -> stringResource(R.string.sync_qibla_missing)
            !hasSensor -> stringResource(R.string.watch_qibla_no_compass)
            else -> null
        }
        if (message != null) {
            Text(
                text = message,
                color = MihrabWhite,
                fontSize = scale.messageSize,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = scale.screenPadding),
            )
        }
    }
}

@Composable
private fun CompassRing(diameter: Dp, stroke: Dp, arrowRotation: Float?) {
    Canvas(modifier = Modifier.size(diameter)) {
        val strokePx = stroke.toPx()
        val radius = size.minDimension / 2f - strokePx / 2f
        val center = Offset(size.width / 2f, size.height / 2f)

        // The ring is frame, never information — it carries no cardinal marks, because it is
        // fixed to the screen rather than to the world and could only ever mislead.
        drawCircle(
            color = MihrabCompassRing,
            radius = radius,
            center = center,
            style = Stroke(width = strokePx),
        )

        if (arrowRotation == null) return@Canvas
        drawQiblaArrow(center = center, radius = radius, rotation = arrowRotation)
    }
}

private fun DrawScope.drawQiblaArrow(center: Offset, radius: Float, rotation: Float) {
    val tipR = radius * ARROW_TIP
    val baseR = radius * ARROW_BASE
    val halfBase = radius * ARROW_HALF_BASE

    rotate(degrees = rotation, pivot = center) {
        val tip = Offset(center.x, center.y - tipR)
        val baseLeft = Offset(center.x - halfBase, center.y - baseR)
        val baseRight = Offset(center.x + halfBase, center.y - baseR)

        // Outer halo — soft bloom around the tip
        drawCircle(
            color = MihrabGold.copy(alpha = GLOW_OUTER_ALPHA),
            radius = radius * GLOW_OUTER,
            center = tip,
        )
        // Inner glow — brighter halo
        drawCircle(
            color = MihrabGold.copy(alpha = GLOW_INNER_ALPHA),
            radius = radius * GLOW_INNER,
            center = tip,
        )

        // Arrow body — gradient from MihrabGold (base) to MihrabGoldBright (tip)
        val arrowBrush = Brush.linearGradient(
            colors = listOf(MihrabGold, MihrabGoldBright),
            start = Offset(center.x, center.y - baseR),
            end = Offset(center.x, center.y - tipR),
        )
        val path = Path().apply {
            moveTo(tip.x, tip.y)
            lineTo(baseRight.x, baseRight.y)
            lineTo(baseLeft.x, baseLeft.y)
            close()
        }
        drawPath(path = path, brush = arrowBrush)

        // Tip dot, then the pure-white jewel core inside it — the brightest point on the face.
        drawCircle(color = MihrabGold, radius = radius * TIP_DOT, center = tip)
        drawCircle(color = MihrabWhite, radius = radius * TIP_JEWEL, center = tip)
    }
}

@Preview(
    device = "id:wearos_large_round",
    showBackground = true,
    backgroundColor = 0xFF000000,
)
@Composable
private fun QiblaCompassLargeRoundPreview() {
    MihrabWatchTheme {
        QiblaCompassContent(currentHeading = 0f, qiblaBearing = 294f, hasSensor = true)
    }
}

@Preview(
    device = "id:wearos_small_round",
    showBackground = true,
    backgroundColor = 0xFF000000,
)
@Composable
private fun QiblaCompassSmallRoundPreview() {
    MihrabWatchTheme {
        QiblaCompassContent(currentHeading = 40f, qiblaBearing = 294f, hasSensor = true)
    }
}

@Preview(
    device = "id:wearos_small_round",
    showBackground = true,
    backgroundColor = 0xFF000000,
)
@Composable
private fun QiblaCompassNoSensorPreview() {
    MihrabWatchTheme {
        QiblaCompassContent(currentHeading = null, qiblaBearing = 294f, hasSensor = false)
    }
}
