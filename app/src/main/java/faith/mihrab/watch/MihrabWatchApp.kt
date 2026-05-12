package faith.mihrab.watch

import androidx.compose.runtime.Composable
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import faith.mihrab.watch.ui.screens.PrayerAlertScreen
import faith.mihrab.watch.ui.screens.PrayerHomeScreen
import faith.mihrab.watch.ui.screens.PrayerListScreen
import faith.mihrab.watch.ui.screens.QiblaCompassScreen
import faith.mihrab.watch.ui.screens.SettingsScreen

private object Routes {
    const val HOME = "home"
    const val PRAYERS = "prayers"
    const val QIBLA = "qibla"
    const val ALERT = "alert"
    const val SETTINGS = "settings"
}

@Composable
fun MihrabWatchApp() {
    val navController = rememberSwipeDismissableNavController()

    SwipeDismissableNavHost(
        navController = navController,
        startDestination = Routes.HOME,
    ) {
        composable(Routes.HOME) {
            PrayerHomeScreen(
                onNavigateToPrayers = { navController.navigate(Routes.PRAYERS) },
                onNavigateToQibla = { navController.navigate(Routes.QIBLA) },
                onNavigateToAlert = { navController.navigate(Routes.ALERT) },
                onNavigateToSettings = { navController.navigate(Routes.SETTINGS) },
            )
        }
        composable(Routes.PRAYERS) { PrayerListScreen() }
        composable(Routes.QIBLA) { QiblaCompassScreen() }
        composable(Routes.ALERT) { PrayerAlertScreen() }
        composable(Routes.SETTINGS) { SettingsScreen() }
    }
}
