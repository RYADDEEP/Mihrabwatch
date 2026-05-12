package faith.mihrab.watch.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.FilledTonalButton
import faith.mihrab.watch.ui.theme.MihrabBlack
import faith.mihrab.watch.ui.theme.MihrabGold
import faith.mihrab.watch.ui.theme.MihrabSecondaryText

@Composable
fun PrayerHomeScreen(
    onNavigateToPrayers: () -> Unit,
    onNavigateToQibla: () -> Unit,
    onNavigateToAlert: () -> Unit,
    onNavigateToSettings: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MihrabBlack),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(horizontal = 16.dp),
        ) {
            Text(
                text = "Mihrab",
                color = MihrabGold,
                style = MaterialTheme.typography.displayLarge,
            )
            Text(
                text = "Home",
                color = MihrabSecondaryText,
                style = MaterialTheme.typography.bodyLarge,
            )
            FilledTonalButton(onClick = onNavigateToPrayers) {
                Text("Prayers", style = MaterialTheme.typography.bodyMedium)
            }
            FilledTonalButton(onClick = onNavigateToQibla) {
                Text("Qibla", style = MaterialTheme.typography.bodyMedium)
            }
            FilledTonalButton(onClick = onNavigateToAlert) {
                Text("Alert", style = MaterialTheme.typography.bodyMedium)
            }
            FilledTonalButton(onClick = onNavigateToSettings) {
                Text("Settings", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
