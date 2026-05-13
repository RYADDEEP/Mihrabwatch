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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import faith.mihrab.watch.ui.theme.MihrabBlack
import faith.mihrab.watch.ui.theme.MihrabGold
import faith.mihrab.watch.ui.theme.MihrabRingCountdownText
import faith.mihrab.watch.ui.theme.MihrabRingTrack
import faith.mihrab.watch.ui.theme.MihrabWatchTheme
import faith.mihrab.watch.ui.theme.MihrabWhite
import kotlinx.coroutines.delay
import java.time.LocalTime
import java.time.format.DateTimeFormatter

// Design guide spec values (MIHRAB_WATCH_DESIGN_GUIDE.md Part 5 Screen 1, round-face adapted)
private val SafeZonePadding = 24.dp
private val RingSize = 280.dp
private val RingStrokeWidth = 10.dp
private val TimeToPrayerSpacing = 8.dp
private val PrayerToCountdownSpacing = 4.dp
private const val DashTime = "—:—"
private const val DashPrayer = "—"
private const val DashCountdown = "—"

data class PrayerHomeState(
    val prayerName: String,
    val countdown: String,
    val progress: Float,
)

@Composable
fun PrayerHomeScreen(state: PrayerHomeState? = null) {
    val now by rememberCurrentTime()
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }
    PrayerHomeContent(
        timeText = if (state == null) DashTime else now.format(timeFormatter),
        prayerName = state?.prayerName ?: DashPrayer,
        countdown = state?.countdown ?: DashCountdown,
        progress = state?.progress ?: 0f,
    )
}

@Composable
private fun PrayerHomeContent(
    timeText: String,
    prayerName: String,
    countdown: String,
    progress: Float,
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
private fun rememberCurrentTime(): State<LocalTime> {
    val time = remember { mutableStateOf(LocalTime.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            time.value = LocalTime.now()
            delay(1000L)
        }
    }
    return time
}

fun formatCountdown(durationMs: Long): String {
    if (durationMs <= 0L) return "now"
    val totalMinutes = (durationMs / 60_000L).coerceAtLeast(1L)
    val hours = totalMinutes / 60L
    val minutes = totalMinutes % 60L
    return if (hours >= 1L) "in ${hours}h ${minutes}m" else "in ${minutes}m"
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
        )
    }
}
