package faith.mihrab.watch.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import faith.mihrab.watch.ui.theme.MihrabBlack
import faith.mihrab.watch.ui.theme.MihrabSecondaryText
import faith.mihrab.watch.ui.theme.MihrabWhite

@Composable
fun QiblaCompassScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MihrabBlack),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Qibla",
                color = MihrabWhite,
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                text = "Compass coming in Session 5",
                color = MihrabSecondaryText,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}
