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
val MihrabSuccessGreen = Color(0xFF30D158)
val MihrabAlertRed = Color(0xFFFF453A)

// Prayer Home ring tokens — exact values from MIHRAB_WATCH_DESIGN_GUIDE.md Part 5 Screen 1
val MihrabRingTrack = Color(0x1AFFFFFF)
val MihrabRingCountdownText = Color(0x99EBEBF5)

// Prayer colors — sky-based palette, locked per design guide Section 2.2
val PrayerFajr = Color(0xFF8AABBA)
val PrayerSunrise = Color(0xFFF5C978)
val PrayerDhuhr = Color(0xFFF5D84A)
val PrayerAsr = Color(0xFFE8BC4A)
val PrayerMaghrib = Color(0xFFE8845A)
val PrayerIsha = Color(0xFFA08BC8)
