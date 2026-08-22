package faith.mihrab.watch.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.Text
import faith.mihrab.watch.R
import faith.mihrab.watch.data.SyncPayload
import faith.mihrab.watch.data.SyncPayloadState
import faith.mihrab.watch.data.countdownLong
import faith.mihrab.watch.data.formatLocalTime
import faith.mihrab.watch.data.localizedPrayerName
import faith.mihrab.watch.data.nextOrFallback
import faith.mihrab.watch.data.remainingMillis
import faith.mihrab.watch.data.resolveWindow
import faith.mihrab.watch.data.windowProgress
import faith.mihrab.watch.ui.components.InscribedColumn
import faith.mihrab.watch.ui.theme.MihrabBlack
import faith.mihrab.watch.ui.theme.MihrabGold
import faith.mihrab.watch.ui.theme.MihrabRingCountdownText
import faith.mihrab.watch.ui.theme.MihrabRingTrack
import faith.mihrab.watch.ui.theme.MihrabWatchTheme
import faith.mihrab.watch.ui.theme.MihrabWhite
import faith.mihrab.watch.ui.theme.WatchScale
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import java.time.Instant

private const val DashTime = "—:—"
private const val DashPrayer = "—"
private const val DashCountdown = "—"

/** Half a degree renders as the two Round caps overlapping: one gold dot at 12 o'clock. */
private const val MinSweepDegrees = 0.5f
private val AutoSizeStep = 1.sp

@Composable
fun PrayerHomeScreen(payloadFlow: Flow<SyncPayloadState>) {
    val context = LocalContext.current
    val state by payloadFlow.collectAsState(initial = SyncPayloadState.Loading)
    val nowInstant by rememberCurrentInstant()

    // Resolve the payload to render: Ready directly, Error falls back to last-known-good.
    val payload: SyncPayload? = when (val s = state) {
        is SyncPayloadState.Ready -> s.payload
        is SyncPayloadState.Error -> s.lastGood
        else -> null
    }

    when {
        state is SyncPayloadState.Empty ->
            MessageScreen(stringResource(R.string.sync_waiting))

        state is SyncPayloadState.UnsupportedVersion ->
            MessageScreen(stringResource(R.string.sync_unsupported))

        payload == null && state is SyncPayloadState.Error ->
            MessageScreen(stringResource(R.string.sync_waiting))

        payload == null -> // Loading (and no cached fallback): keep the dash placeholder ring.
            PrayerHomeContent(
                timeText = DashTime,
                prayerName = DashPrayer,
                countdown = DashCountdown,
                progress = null,
            )

        else -> {
            // The window is recomputed on every minute tick, so the day boundary and the
            // window rollover both fall out of the same clock — no separate midnight timer.
            val window = remember(payload, nowInstant) {
                resolveWindow(payload, now = nowInstant)
            }
            val next = window.nextOrFallback(payload)
            val remaining = remainingMillis(next?.time, nowInstant)
            // Locale-aware: Turkish uppercases "İkindi" to "İKİNDİ", not "İKINDI".
            // Read through LocalConfiguration, not the Context — the app switches locale at
            // runtime from sync_payload, and only this source recomposes when it does.
            val locale = LocalConfiguration.current.locales[0]

            PrayerHomeContent(
                timeText = formatLocalTime(next?.time, payload.timezone) ?: DashTime,
                prayerName = localizedPrayerName(context, next?.name)
                    .ifBlank { DashPrayer }
                    .uppercase(locale),
                countdown = countdownLong(context, remaining),
                progress = windowProgress(window, nowInstant),
            )
        }
    }
}

@Composable
private fun MessageScreen(message: String) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(MihrabBlack),
        contentAlignment = Alignment.Center,
    ) {
        val scale = WatchScale.from(maxWidth, maxHeight)
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

@Composable
private fun PrayerHomeContent(
    timeText: String,
    prayerName: String,
    countdown: String,
    progress: Float?,
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(MihrabBlack),
        contentAlignment = Alignment.Center,
    ) {
        val scale = WatchScale.from(maxWidth, maxHeight)
        val floor = WatchScale.FLOOR_SP.sp

        ProgressRing(scale = scale, progress = progress)

        // Three beats of one breath — and the inscribed layout is what lets all three fit.
        InscribedColumn(radius = scale.ringClearRadius) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                FittedLine(
                    text = timeText,
                    minSize = scale.timeSizeMin,
                    maxSize = scale.timeSize,
                    weight = FontWeight.Bold,
                    color = MihrabWhite,
                )
                Spacer(modifier = Modifier.height(scale.timeToName))
                FittedLine(
                    text = prayerName,
                    minSize = floor,
                    maxSize = scale.nameSize,
                    weight = FontWeight.SemiBold,
                    color = MihrabGold,
                )
                Spacer(modifier = Modifier.height(scale.nameToCountdown))
                FittedLine(
                    text = countdown,
                    minSize = floor,
                    maxSize = scale.countdownSize,
                    weight = FontWeight.Medium,
                    color = MihrabRingCountdownText,
                )
            }
        }
    }
}

/**
 * One line that shrinks to fit rather than wrapping or clipping. The floor is the design
 * guide's 13sp; below that the line would stop being glanceable, so it clips at the chord
 * instead — inside the ring either way.
 */
@Composable
private fun FittedLine(
    text: String,
    minSize: TextUnit,
    maxSize: TextUnit,
    weight: FontWeight,
    color: Color,
) {
    val upper = maxOf(maxSize.value, minSize.value).sp
    BasicText(
        text = text,
        style = TextStyle(
            color = color,
            fontFamily = FontFamily.Default,
            fontWeight = weight,
            textAlign = TextAlign.Center,
        ),
        maxLines = 1,
        softWrap = false,
        autoSize = TextAutoSize.StepBased(
            minFontSize = minSize,
            maxFontSize = upper,
            stepSize = AutoSizeStep,
        ),
    )
}

@Composable
private fun ProgressRing(scale: WatchScale, progress: Float?) {
    Canvas(modifier = Modifier.size(scale.ringDiameter)) {
        val strokePx = scale.ringStroke.toPx()
        val inset = strokePx / 2f
        val arcSize = Size(size.width - strokePx, size.height - strokePx)
        val topLeft = Offset(inset, inset)
        // Track: full 360°, always present — the dial the fill is read against.
        drawArc(
            color = MihrabRingTrack,
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokePx, cap = StrokeCap.Round),
        )
        // A null progress means the window is unknown. Neither an empty ring nor a full one
        // would be honest, so nothing is drawn — the track alone makes no claim.
        if (progress != null) {
            val sweep = (progress.coerceIn(0f, 1f) * 360f).coerceAtLeast(MinSweepDegrees)
            drawArc(
                color = MihrabGold,
                startAngle = -90f,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round),
            )
        }
    }
}

@Composable
private fun rememberCurrentInstant(): State<Instant> {
    val instant = remember { mutableStateOf(Instant.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            instant.value = Instant.now()
            delay(60_000L)
        }
    }
    return instant
}

// device id literals equivalent to androidx.wear.tooling.preview.devices.WearDevices
// (wear-tooling-preview dep not on classpath).
@Preview(
    device = "id:wearos_large_round",
    showBackground = true,
    backgroundColor = 0xFF000000,
)
@Composable
private fun PrayerHomeLargeRoundPreview() {
    MihrabWatchTheme {
        PrayerHomeContent(
            timeText = "05:42",
            prayerName = "FAJR",
            countdown = "23m",
            progress = 0.7f,
        )
    }
}

// The 384px case the whole scale system exists for. Swahili is the widest countdown in the set.
@Preview(
    device = "id:wearos_small_round",
    showBackground = true,
    backgroundColor = 0xFF000000,
)
@Composable
private fun PrayerHomeSmallRoundPreview() {
    MihrabWatchTheme {
        PrayerHomeContent(
            timeText = "18:05",
            prayerName = "MAGHARIBI",
            countdown = "5h 30d",
            progress = 0.33f,
        )
    }
}
