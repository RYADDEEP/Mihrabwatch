package faith.mihrab.watch

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import faith.mihrab.watch.data.PairingCredentials
import faith.mihrab.watch.data.PairingDataStore
import faith.mihrab.watch.data.PairingRepository
import faith.mihrab.watch.ui.screens.PairingScreen
import faith.mihrab.watch.ui.screens.PrayerAlertScreen
import faith.mihrab.watch.ui.screens.PrayerHomeScreen
import faith.mihrab.watch.ui.screens.PrayerListScreen
import faith.mihrab.watch.ui.screens.QiblaCompassScreen
import faith.mihrab.watch.ui.screens.SettingsScreen
import faith.mihrab.watch.ui.theme.MihrabBlack

private object Routes {
    const val PAIRING = "pairing"
    const val HOME = "home"
    const val PRAYERS = "prayers"
    const val QIBLA = "qibla"
    const val ALERT = "alert"
    const val SETTINGS = "settings"
}

private sealed interface CredentialState {
    object Loading : CredentialState
    object Unpaired : CredentialState
    data class Paired(val creds: PairingCredentials) : CredentialState
}

@Composable
fun MihrabWatchApp(
    pairingRepository: PairingRepository,
    pairingDataStore: PairingDataStore,
) {
    val credentialState by produceState<CredentialState>(CredentialState.Loading) {
        pairingDataStore.credentialsFlow.collect { creds ->
            value = if (creds != null) CredentialState.Paired(creds) else CredentialState.Unpaired
        }
    }

    when (credentialState) {
        is CredentialState.Loading -> Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MihrabBlack),
        )
        else -> {
            val navController = rememberSwipeDismissableNavController()
            val startDestination = if (credentialState is CredentialState.Paired) {
                Routes.HOME
            } else {
                Routes.PAIRING
            }

            SwipeDismissableNavHost(
                navController = navController,
                startDestination = startDestination,
            ) {
                composable(Routes.PAIRING) {
                    PairingScreen(
                        repository = pairingRepository,
                        dataStore = pairingDataStore,
                        onPaired = {
                            navController.navigate(Routes.HOME) {
                                popUpTo(Routes.PAIRING) { inclusive = true }
                            }
                        },
                    )
                }
                composable(Routes.HOME) {
                    PrayerHomeScreen()
                }
                composable(Routes.PRAYERS) { PrayerListScreen() }
                composable(Routes.QIBLA) { QiblaCompassScreen() }
                composable(Routes.ALERT) { PrayerAlertScreen() }
                composable(Routes.SETTINGS) { SettingsScreen() }
            }
        }
    }
}
