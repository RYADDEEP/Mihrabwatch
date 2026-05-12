package faith.mihrab.watch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import faith.mihrab.watch.ui.theme.MihrabWatchTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MihrabWatchTheme {
                MihrabWatchApp()
            }
        }
    }
}
