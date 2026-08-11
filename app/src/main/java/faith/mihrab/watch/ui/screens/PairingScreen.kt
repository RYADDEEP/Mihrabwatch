package faith.mihrab.watch.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.Text
import faith.mihrab.watch.R
import faith.mihrab.watch.data.PairingDataStore
import faith.mihrab.watch.data.PairingRepository
import faith.mihrab.watch.data.PairingRow
import faith.mihrab.watch.ui.theme.MihrabBlack
import faith.mihrab.watch.ui.theme.MihrabGold
import faith.mihrab.watch.ui.theme.MihrabGoldBright
import faith.mihrab.watch.ui.theme.MihrabWatchTheme
import faith.mihrab.watch.ui.theme.WatchScale
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration.Companion.minutes
import java.time.Instant

// The code is 8 characters of A–Z0–9 in Monospace, so it has exactly one width target and no
// long case. Auto-sizing to that target is what makes "never wrapping, never clipping" true on
// every screen — a fixed size cannot, and 44sp could not.
private val CodeMinSize = 20.sp
private val CodeMaxSize = 40.sp
private val CodeStep = 0.5.sp
private val CodeTracking = 0.09.em

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

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(MihrabBlack)
            .clickable(enabled = state is PairingState.Error) { attempt += 1 },
        contentAlignment = Alignment.Center,
    ) {
        val scale = WatchScale.from(maxWidth, maxHeight)
        val contentModifier = Modifier.padding(horizontal = scale.screenPadding)

        when (val s = state) {
            is PairingState.Loading -> Text(
                text = stringResource(R.string.watch_pairing_connecting),
                color = MihrabGold.copy(alpha = 0.7f),
                fontSize = scale.connectingSize,
                textAlign = TextAlign.Center,
                modifier = contentModifier,
            )

            is PairingState.Error -> Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = contentModifier,
            ) {
                Text(
                    text = stringResource(R.string.watch_pairing_error_title),
                    color = MihrabGold,
                    fontSize = scale.errorTitleSize,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(scale.codeToExpiry / 2))
                Text(
                    text = stringResource(R.string.watch_pairing_error_retry),
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = scale.expirySize,
                    maxLines = 2,
                    textAlign = TextAlign.Center,
                )
            }

            is PairingState.Active -> {
                val expiresEpoch = remember(s.row.expiresAt) {
                    runCatching { Instant.parse(s.row.expiresAt).epochSecond }.getOrDefault(0L)
                }
                val remaining = (expiresEpoch - nowEpoch).coerceAtLeast(0L)
                val expired = remaining <= 0L
                val capReached = expired && autoRefreshCount >= 5
                PairingActiveContent(
                    code = s.row.pairingCode,
                    caption = when {
                        capReached -> stringResource(R.string.watch_pairing_code_expired_refresh)
                        expired -> ""
                        else -> formatExpiryMinutes(remaining)
                    },
                    scale = scale,
                    modifier = if (capReached) {
                        contentModifier.clickable { attempt += 1 }
                    } else {
                        contentModifier
                    },
                )
            }
        }
    }
}

/**
 * The Active state, stateless — shared with the previews so what is inspected in the IDE is
 * the layout that ships, not a hand-copied likeness of it.
 */
@Composable
private fun PairingActiveContent(
    code: String,
    caption: String,
    scale: WatchScale,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier,
    ) {
        BasicText(
            text = code,
            modifier = Modifier.width(scale.codeTargetWidth),
            style = TextStyle(
                brush = Brush.linearGradient(colors = listOf(MihrabGold, MihrabGoldBright)),
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                // Tracking in em, so it scales with whatever size auto-sizing settles on.
                letterSpacing = CodeTracking,
                shadow = Shadow(color = MihrabGold.copy(alpha = 0.5f), blurRadius = 10f),
                textAlign = TextAlign.Center,
            ),
            maxLines = 1,
            softWrap = false,
            autoSize = TextAutoSize.StepBased(
                minFontSize = CodeMinSize,
                maxFontSize = CodeMaxSize,
                stepSize = CodeStep,
            ),
        )
        Spacer(Modifier.height(scale.codeToExpiry))
        // Height reserved for one line so the code does not jump when the caption empties at
        // expiry; it still grows to two lines where a translation needs them.
        Box(
            modifier = Modifier.heightIn(min = scale.expiryLineHeight),
            contentAlignment = Alignment.TopCenter,
        ) {
            Text(
                text = caption,
                color = MihrabGold.copy(alpha = 0.85f),
                fontSize = scale.expirySize,
                fontWeight = FontWeight.Normal,
                maxLines = 2,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun formatExpiryMinutes(remainingSeconds: Long): String {
    // Only ever called with a positive remainder — the expired cases are handled by the caller.
    val minutes = ((remainingSeconds + 59L) / 60L).toInt()
    return pluralStringResource(R.plurals.watch_pairing_expires_in, minutes, minutes)
}

@Preview(
    device = "id:wearos_large_round",
    showBackground = true,
    backgroundColor = 0xFF000000,
)
@Composable
private fun PairingLargeRoundPreview() {
    MihrabWatchTheme {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize().background(MihrabBlack),
            contentAlignment = Alignment.Center,
        ) {
            PairingActiveContent(
                code = "A3BK9M2X",
                caption = "Expires in 5 minutes",
                scale = WatchScale.from(maxWidth, maxHeight),
            )
        }
    }
}

// 384px, and the longest supporting string in the set (Turkish refresh prompt).
@Preview(
    device = "id:wearos_small_round",
    showBackground = true,
    backgroundColor = 0xFF000000,
)
@Composable
private fun PairingSmallRoundPreview() {
    MihrabWatchTheme {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize().background(MihrabBlack),
            contentAlignment = Alignment.Center,
        ) {
            PairingActiveContent(
                code = "WWWWWWWW",
                caption = "Süresi doldu — yenilemek için dokunun",
                scale = WatchScale.from(maxWidth, maxHeight),
            )
        }
    }
}
