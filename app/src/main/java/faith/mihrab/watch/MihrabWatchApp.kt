package faith.mihrab.watch

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.wear.compose.foundation.pager.HorizontalPager
import androidx.wear.compose.foundation.pager.rememberPagerState
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import faith.mihrab.watch.data.PairingCredentials
import faith.mihrab.watch.data.PairingDataStore
import faith.mihrab.watch.data.PairingRepository
import faith.mihrab.watch.data.QiblaCompassRepository
import faith.mihrab.watch.ui.screens.PairingScreen
import faith.mihrab.watch.ui.screens.PrayerHomeScreen
import faith.mihrab.watch.ui.screens.QiblaCompassScreen
import faith.mihrab.watch.ui.theme.MihrabBlack

private object Routes {
    const val PAIRING = "pairing"
    const val MAIN = "main"
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
                Routes.MAIN
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
                            navController.navigate(Routes.MAIN) {
                                popUpTo(Routes.PAIRING) { inclusive = true }
                            }
                        },
                    )
                }
                composable(Routes.MAIN) {
                    val context = LocalContext.current
                    val qiblaRepository = remember(context) {
                        QiblaCompassRepository(context.applicationContext)
                    }
                    val pagerState = rememberPagerState(pageCount = { 2 })
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize(),
                    ) { page ->
                        when (page) {
                            0 -> PrayerHomeScreen()
                            1 -> QiblaCompassScreen(repository = qiblaRepository)
                            else -> Unit
                        }
                    }
                }
            }
        }
    }
}
