package faith.mihrab.watch.data

/**
 * Provides next-prayer information for the watch's out-of-app surfaces
 * (Complications, Tile). Hardcoded for v1; Phase 6+ replaces with
 * dynamic data from paired phone via device_pairings.sync_payload.
 */
data class NextPrayer(
    val name: String,
    val time: String,
    val countdownShort: String,
    val countdownLong: String,
)

object NextPrayerProvider {
    fun current(): NextPrayer = NextPrayer(
        name = "Maghrib",
        time = "18:22",
        countdownShort = "20m",
        countdownLong = "in 20 minutes",
    )
}
