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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import faith.mihrab.watch.R
import faith.mihrab.watch.data.SyncPayload
import faith.mihrab.watch.data.SyncPayloadState
import faith.mihrab.watch.data.countdownLong
import faith.mihrab.watch.data.formatLocalTime
import faith.mihrab.watch.data.localizedPrayerName
import faith.mihrab.watch.data.remainingMillis
import faith.mihrab.watch.data.resolveNextPrayer
import faith.mihrab.watch.data.resolveZone
import faith.mihrab.watch.data.ringProgress
import faith.mihrab.watch.ui.theme.MihrabBlack
import faith.mihrab.watch.ui.theme.MihrabGold
import faith.mihrab.watch.ui.theme.MihrabRingCountdownText
import faith.mihrab.watch.ui.theme.MihrabRingTrack
import faith.mihrab.watch.ui.theme.MihrabWatchTheme
import faith.mihrab.watch.ui.theme.MihrabWhite
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

// Design guide spec values (MIHRAB_WATCH_DESIGN_GUIDE.md Part 5 Screen 1, round-face adapted)
private val SafeZonePadding = 24.dp
private val RingSize = 280.dp
private val RingStrokeWidth = 10.dp
private val TimeToPrayerSpacing = 8.dp
private val PrayerToCountdownSpacing = 4.dp
private val CaptionSpacing = 6.dp
private const val DashTime = "—:—"
private const val DashPrayer = "—"
private const val DashCountdown = "—"

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

    val zone = resolveZone(payload?.timezone)
    val dayRollover by rememberDayRollover(zone)

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
                progress = 0f,
                caption = null,
            )

        else -> {
            val next = remember(payload, nowInstant, dayRollover) { resolveNextPrayer(payload) }
            val remaining = remainingMillis(next?.time, nowInstant)
            val stale = payload.date?.let { d ->
                runCatching { LocalDate.parse(d) }.getOrNull()
                    ?.isBefore(LocalDate.now(zone)) == true
            } ?: false
            // Ready state: no caption. Error state with lastGood: show "Last updated" only.
            val caption = if (state !is SyncPayloadState.Ready && stale && payload.lastUpdated != null) {
                val time = formatLocalTime(payload.lastUpdated, payload.timezone) ?: "—"
                stringResource(R.string.watch_last_updated, time)
            } else null

            PrayerHomeContent(
                timeText = formatLocalTime(next?.time, payload.timezone) ?: DashTime,
                prayerName = localizedPrayerName(context, next?.name).ifBlank { DashPrayer },
                countdown = countdownLong(remaining),
                progress = ringProgress(payload.lastUpdated, next?.time, nowInstant),
                caption = caption,
            )
        }
    }
}

@Composable
private fun MessageScreen(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MihrabBlack)
            .padding(SafeZonePadding),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = message,
            color = MihrabWhite,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun PrayerHomeContent(
    timeText: String,
    prayerName: String,
    countdown: String,
    progress: Float,
    caption: String?,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MihrabBlack),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier.padding(SafeZonePadding),
            contentAlignment = Alignment.Center,
        ) {
            ProgressRing(progress = progress)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = timeText,
                    color = MihrabWhite,
                    style = MaterialTheme.typography.displayLarge,
                )
                Spacer(modifier = Modifier.height(TimeToPrayerSpacing))
                Text(
                    text = prayerName.uppercase(),
                    color = MihrabGold,
                    style = MaterialTheme.typography.titleLarge,
                )
                Spacer(modifier = Modifier.height(PrayerToCountdownSpacing))
                Text(
                    text = countdown,
                    color = MihrabRingCountdownText,
                    style = MaterialTheme.typography.bodyLarge,
                )
                if (caption != null) {
                    Spacer(modifier = Modifier.height(CaptionSpacing))
                    Text(
                        text = caption,
                        color = MihrabRingCountdownText,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

@Composable
private fun ProgressRing(progress: Float) {
    Canvas(modifier = Modifier.size(RingSize)) {
        val strokePx = RingStrokeWidth.toPx()
        val inset = strokePx / 2f
        val arcSize = Size(size.width - strokePx, size.height - strokePx)
        val topLeft = Offset(inset, inset)
        // Track: full 360°, dim white. StrokeCap is irrelevant on a closed circle but kept Round for consistency.
        drawArc(
            color = MihrabRingTrack,
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokePx, cap = StrokeCap.Round),
        )
        val clamped = progress.coerceIn(0f, 1f)
        if (clamped > 0f) {
            drawArc(
                color = MihrabGold,
                startAngle = -90f,
                sweepAngle = clamped * 360f,
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

// Fires at every local-time midnight so the resolver recomputes the new day's prayers
// without an app restart (service path only — the fallback path's rollover comes from the
// phone pushing a fresh payload at 00:00).
@Composable
private fun rememberDayRollover(zone: ZoneId): State<Int> {
    val tick = remember(zone) { mutableIntStateOf(0) }
    LaunchedEffect(zone) {
        while (true) {
            val now = ZonedDateTime.now(zone)
            val nextMidnight = now.toLocalDate().plusDays(1).atStartOfDay(zone)
            val delayMs = Duration.between(now, nextMidnight).toMillis().coerceAtLeast(1_000L)
            delay(delayMs)
            tick.intValue += 1
        }
    }
    return tick
}

// device id literal equivalent to androidx.wear.tooling.preview.devices.WearDevices.LARGE_ROUND
// (wear-tooling-preview dep not on classpath; dep changes are out of scope for this session).
@Preview(
    device = "id:wearos_large_round",
    showBackground = true,
    backgroundColor = 0xFF000000,
)
@Composable
private fun PrayerHomePreview() {
    MihrabWatchTheme {
        PrayerHomeContent(
            timeText = "05:42",
            prayerName = "FAJR",
            countdown = "in 23m",
            progress = 0.7f,
            caption = null,
        )
    }
}
