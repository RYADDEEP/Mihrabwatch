package faith.mihrab.watch.ui.theme

import androidx.compose.ui.graphics.Color

// ---------------------------------------------------------------------------
// Mihrab Watch design tokens — canonical source: docs/MIHRAB_WATCH_DESIGN_GUIDE.md
// ---------------------------------------------------------------------------

// Backgrounds — OLED-optimized pure black is non-negotiable per design guide
val MihrabBlack = Color(0xFF000000)
val MihrabSurface = Color(0xFF1C1C1E)
val MihrabSurfaceActive = Color(0xFF2C2C2E)

// Text
val MihrabWhite = Color(0xFFFFFFFF)
val MihrabSecondaryText = Color.White.copy(alpha = 0.6f)
val MihrabTertiaryText = Color.White.copy(alpha = 0.3f)

// Accent
val MihrabGold = Color(0xFFD4A537)
val MihrabGoldBright = Color(0xFFF5C842)
val MihrabAlertRed = Color(0xFFFF453A)

// Prayer Home ring tokens.
// Alpha ruling: nothing below 22% carries information outdoors on OLED black. The track was
// 10% and the countdown 60% — both measured as lost or marginal in daylight.
val MihrabRingTrack = Color(0x38FFFFFF)
val MihrabRingCountdownText = Color(0xB3EBEBF5)

// Qibla compass ring — frame, never information. 30% is the floor at which it reads outdoors.
val MihrabCompassRing = Color(0x4DFFFFFF)

// Prayer colors — sky-based palette per design guide Section 2.2. Only Fajr survives: it is the
// theme's tertiary slot. The other five were never painted once the screens settled on gold and
// white, and the design guide remains the canonical record of the full palette.
val PrayerFajr = Color(0xFF8AABBA)
