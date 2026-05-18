package faith.mihrab.watch.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.tooling.preview.Preview
import faith.mihrab.watch.ui.theme.MihrabWatchTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.Text
import faith.mihrab.watch.data.PairingDataStore
import faith.mihrab.watch.data.PairingRepository
import faith.mihrab.watch.data.PairingRow
import faith.mihrab.watch.ui.theme.MihrabBlack
import faith.mihrab.watch.ui.theme.MihrabGold
import faith.mihrab.watch.ui.theme.MihrabGoldBright
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration.Companion.minutes
import java.time.Instant

private sealed interface PairingState {
    object Loading : PairingState
    data class Active(val row: PairingRow) : PairingState
    object Error : PairingState
}

@Composable
fun PairingScreen(
    repository: PairingRepository,
    dataStore: PairingDataStore,
    onPaired: () -> Unit,
) {
    var state by remember { mutableStateOf<PairingState>(PairingState.Loading) }
    var nowEpoch by remember { mutableLongStateOf(Instant.now().epochSecond) }
    var attempt by remember { mutableIntStateOf(0) }
    var autoRefreshCount by remember { mutableIntStateOf(0) }

    val activeExpired = remember(state, nowEpoch) {
        (state as? PairingState.Active)
            ?.row?.expiresAt
            ?.let { runCatching { Instant.parse(it).epochSecond }.getOrNull() }
            ?.let { nowEpoch >= it }
            ?: false
    }

    suspend fun handlePaired(row: PairingRow, source: String) {
        if (row.pairedDeviceType != "watch") return
        val userId = row.pairedUserId
        if (row.status == "paired" && userId != null) {
            Log.d(
                "Pairing",
                "PairingScreen: handlePaired source=$source id=${row.id} -> savePairing + onPaired",
            )
            dataStore.savePairing(row.id, userId)
            onPaired()
        }
    }

    LaunchedEffect(attempt) {
        state = PairingState.Loading
        try {
            val row = repository.createPairing()
            state = PairingState.Active(row)
        } catch (t: Throwable) {
            if (t is CancellationException) throw t
            Log.d("Pairing", "PairingScreen: createPairing error=${t.message}")
            state = PairingState.Error
        }
    }

    LaunchedEffect(state) {
        val current = state
        if (current is PairingState.Active) {
            try {
                repository.observePairing(current.row.id).collect { update ->
                    handlePaired(update, source = "realtime")
                }
            } catch (t: Throwable) {
                if (t is CancellationException) throw t
                Log.d("Pairing", "PairingScreen: observePairing collect error=${t.message}")
                state = PairingState.Error
            }
        }
    }

    LaunchedEffect(state) {
        val current = state
        if (current is PairingState.Active) {
            val id = current.row.id
            var attemptN = 0
            var reason = "timeout"
            Log.d("Pairing", "Polling: started pairing_id=$id interval=2s timeout=5min")
            try {
                withTimeoutOrNull(5.minutes) {
                    while (isActive) {
                        delay(2_000L)
                        attemptN += 1
                        Log.d("Pairing", "Polling: tick attempt=$attemptN pairing_id=$id")
                        val row = runCatching { repository.fetchPairing(id) }.getOrNull()
                        if (row != null &&
                            row.status == "paired" &&
                            row.pairedUserId != null &&
                            row.pairedDeviceType == "watch"
                        ) {
                            Log.d(
                                "Pairing",
                                "Polling: detected status=paired pairing_id=$id attempt=$attemptN",
                            )
                            reason = "paired"
                            handlePaired(row, source = "polling")
                            return@withTimeoutOrNull
                        }
                    }
                }
                if (reason == "timeout") {
                    Log.d("Pairing", "Polling: timeout reached after 5min pairing_id=$id")
                }
            } catch (t: Throwable) {
                if (t is CancellationException) {
                    Log.d("Pairing", "Polling: cancelled pairing_id=$id reason=dispose")
                    throw t
                }
                Log.d("Pairing", "Polling: error pairing_id=$id error=${t.message}")
                reason = "error"
            }
            Log.d("Pairing", "Polling: cancelled pairing_id=$id reason=$reason")
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            nowEpoch = Instant.now().epochSecond
        }
    }

    LaunchedEffect(activeExpired) {
        if (activeExpired && autoRefreshCount < 5) {
            autoRefreshCount += 1
            Log.d("Pairing", "AutoRefresh: attempt count=$autoRefreshCount reason=ttl_expired")
            attempt += 1
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MihrabBlack)
            .clickable(enabled = state is PairingState.Error) { attempt += 1 }
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        when (val s = state) {
            is PairingState.Loading -> Text(
                text = "Connecting…",
                color = MihrabGold.copy(alpha = 0.7f),
                fontSize = 14.sp,
            )
            is PairingState.Error -> Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = "Connection error",
                    color = MihrabGold,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Tap to retry",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 13.sp,
                )
            }
            is PairingState.Active -> {
                val expiresEpoch = remember(s.row.expiresAt) {
                    runCatching { Instant.parse(s.row.expiresAt).epochSecond }.getOrDefault(0L)
                }
                val remaining = (expiresEpoch - nowEpoch).coerceAtLeast(0L)
                val expired = remaining <= 0L
                val capReached = expired && autoRefreshCount >= 5
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = if (capReached) Modifier.clickable { attempt += 1 } else Modifier,
                ) {
                    Text(
                        text = s.row.pairingCode,
                        maxLines = 1,
                        softWrap = false,
                        style = TextStyle(
                            brush = Brush.linearGradient(
                                colors = listOf(MihrabGold, MihrabGoldBright),
                            ),
                            fontSize = 28.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 4.sp,
                            shadow = Shadow(
                                color = MihrabGold.copy(alpha = 0.5f),
                                blurRadius = 10f,
                            ),
                        ),
                    )
                    Spacer(Modifier.height(20.dp))
                    Text(
                        text = when {
                            capReached -> "Expired — tap to refresh"
                            expired -> ""
                            else -> formatExpiryMinutes(remaining)
                        },
                        color = MihrabGold.copy(alpha = 0.6f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal,
                    )
                }
            }
        }
    }
}

private fun formatExpiryMinutes(remainingSeconds: Long): String {
    if (remainingSeconds <= 0L) return "Expired"
    val minutes = ((remainingSeconds + 59L) / 60L).toInt()
    val noun = if (minutes == 1) "minute" else "minutes"
    return "Expires in $minutes $noun"
}

// PairingScreen is a monolithic stateful Composable that requires PairingRepository and
// PairingDataStore — both need a live Supabase client and cannot be cheaply mocked.
// This preview renders a static representative layout matching PairingState.Active so the
// visual shape can be audited. Refactor to stateful/stateless split is deferred to the
// follow-up polish session.
@Preview(
    device = "id:wearos_large_round",
    showBackground = true,
    backgroundColor = 0xFF000000,
)
@Composable
private fun PairingScreenPreview() {
    MihrabWatchTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MihrabBlack)
                .padding(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = "A3BK9M2X",
                    maxLines = 1,
                    softWrap = false,
                    style = TextStyle(
                        brush = Brush.linearGradient(
                            colors = listOf(MihrabGold, MihrabGoldBright),
                        ),
                        fontSize = 28.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 4.sp,
                        shadow = Shadow(
                            color = MihrabGold.copy(alpha = 0.5f),
                            blurRadius = 10f,
                        ),
                    ),
                )
                Spacer(Modifier.height(20.dp))
                Text(
                    text = "Expires in 5 minutes",
                    color = MihrabGold.copy(alpha = 0.6f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                )
            }
        }
    }
}
