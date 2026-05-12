package faith.mihrab.watch.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import faith.mihrab.watch.ui.theme.MihrabBlack
import faith.mihrab.watch.ui.theme.MihrabWhite

@Composable
fun PrayerListScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MihrabBlack),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "Prayer List",
            color = MihrabWhite,
            style = MaterialTheme.typography.titleLarge,
        )
    }
}
