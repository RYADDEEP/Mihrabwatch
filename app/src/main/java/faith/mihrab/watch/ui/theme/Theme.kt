package faith.mihrab.watch.ui.theme

import androidx.compose.runtime.Composable
import androidx.wear.compose.material3.ColorScheme
import androidx.wear.compose.material3.MaterialTheme

private val MihrabColorScheme = ColorScheme(
    primary = MihrabGold,
    primaryDim = MihrabGold,
    primaryContainer = MihrabSurface,
    onPrimary = MihrabBlack,
    onPrimaryContainer = MihrabGold,
    secondary = MihrabGoldBright,
    secondaryDim = MihrabGoldBright,
    secondaryContainer = MihrabSurface,
    onSecondary = MihrabBlack,
    onSecondaryContainer = MihrabWhite,
    tertiary = PrayerFajr,
    tertiaryDim = PrayerFajr,
    tertiaryContainer = MihrabSurface,
    onTertiary = MihrabBlack,
    onTertiaryContainer = MihrabWhite,
    surfaceContainerLow = MihrabBlack,
    surfaceContainer = MihrabSurface,
    surfaceContainerHigh = MihrabSurfaceActive,
    onSurface = MihrabWhite,
    onSurfaceVariant = MihrabSecondaryText,
    outline = MihrabSecondaryText,
    outlineVariant = MihrabTertiaryText,
    background = MihrabBlack,
    onBackground = MihrabWhite,
    error = MihrabAlertRed,
    onError = MihrabBlack,
    errorContainer = MihrabSurface,
    onErrorContainer = MihrabAlertRed,
)

// Watch is always dark — no light theme variant per design guide commandment #2.
@Composable
fun MihrabWatchTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = MihrabColorScheme,
        typography = MihrabTypography,
        content = content,
    )
}
