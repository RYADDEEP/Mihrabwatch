package faith.mihrab.watch.ui.screens

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
import kotlinx.coroutines.delay
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

    LaunchedEffect(attempt) {
        state = PairingState.Loading
        try {
            val row = repository.createPairing()
            state = PairingState.Active(row)
        } catch (_: Throwable) {
            state = PairingState.Error
        }
    }

    LaunchedEffect(state) {
        val current = state
        if (current is PairingState.Active) {
            try {
                repository.observePairing(current.row.id).collect { update ->
                    if (update.pairedDeviceType != "watch") return@collect
                    if (update.status == "paired" && update.pairedUserId != null) {
                        dataStore.savePairing(update.id, update.pairedUserId)
                        onPaired()
                    }
                }
            } catch (_: Throwable) {
                state = PairingState.Error
            }
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            nowEpoch = Instant.now().epochSecond
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
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = if (expired) Modifier.clickable { attempt += 1 } else Modifier,
                ) {
                    Text(
                        text = s.row.pairingCode,
                        style = TextStyle(
                            brush = Brush.linearGradient(
                                colors = listOf(MihrabGold, MihrabGoldBright),
                            ),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 3.sp,
                            fontFeatureSettings = "tnum",
                            shadow = Shadow(
                                color = MihrabGold.copy(alpha = 0.5f),
                                blurRadius = 10f,
                            ),
                        ),
                    )
                    Spacer(Modifier.height(20.dp))
                    Text(
                        text = if (expired) "Expired — tap to refresh" else formatExpiryMinutes(remaining),
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
                    text = "123456",
                    style = TextStyle(
                        brush = Brush.linearGradient(
                            colors = listOf(MihrabGold, MihrabGoldBright),
                        ),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 3.sp,
                        fontFeatureSettings = "tnum",
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
