package faith.mihrab.watch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import faith.mihrab.watch.data.PairingDataStore
import faith.mihrab.watch.data.PairingRepository
import faith.mihrab.watch.ui.theme.MihrabWatchTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val pairingRepository = PairingRepository()
        val pairingDataStore = PairingDataStore(applicationContext)
        setContent {
            MihrabWatchTheme {
                MihrabWatchApp(
                    pairingRepository = pairingRepository,
                    pairingDataStore = pairingDataStore,
                )
            }
        }
    }
}
