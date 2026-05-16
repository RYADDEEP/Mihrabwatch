package faith.mihrab.watch.data

import android.content.Context
import faith.mihrab.watch.R
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Pure rendering helpers shared by Prayer Home, the Tile, and the Complication. All payload
 * times are UTC ISO 8601 (schema decision #1); the watch converts to local for display using
 * the payload's `timezone`, falling back to the watch's own zone when absent or invalid
 * (traveler edge case, schema §8.E).
 */

private val LocalTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

fun prayerNameResId(name: String?): Int? = when (name?.lowercase()) {
    "fajr" -> R.string.prayer_fajr
    "sunrise" -> R.string.prayer_sunrise
    "dhuhr" -> R.string.prayer_dhuhr
    "asr" -> R.string.prayer_asr
    "maghrib" -> R.string.prayer_maghrib
    "isha" -> R.string.prayer_isha
    else -> null
}

fun localizedPrayerName(context: Context, name: String?): String =
    prayerNameResId(name)?.let(context::getString)
        ?: name?.replaceFirstChar { it.uppercase() }.orEmpty()

fun resolveZone(timezone: String?): ZoneId =
    timezone?.let { runCatching { ZoneId.of(it) }.getOrNull() } ?: ZoneId.systemDefault()

fun parseInstant(iso: String?): Instant? =
    iso?.let { runCatching { Instant.parse(it) }.getOrNull() }

fun formatLocalTime(iso: String?, timezone: String?): String? =
    parseInstant(iso)?.atZone(resolveZone(timezone))?.format(LocalTimeFormatter)

/**
 * Ring fill = fraction elapsed between when the phone wrote the payload and the next prayer.
 * Uses only Phase 5b–permitted fields (`last_updated`, `next_prayer.time`).
 */
fun ringProgress(lastUpdatedIso: String?, nextIso: String?, now: Instant): Float {
    val start = parseInstant(lastUpdatedIso) ?: return 0f
    val end = parseInstant(nextIso) ?: return 0f
    val total = Duration.between(start, end).toMillis()
    if (total <= 0L) return 1f
    val elapsed = Duration.between(start, now).toMillis().coerceAtLeast(0L)
    return (elapsed.toFloat() / total.toFloat()).coerceIn(0f, 1f)
}

fun remainingMillis(nextIso: String?, now: Instant): Long? =
    parseInstant(nextIso)?.let { Duration.between(now, it).toMillis() }

fun countdownShort(remainingMs: Long?): String {
    if (remainingMs == null) return "—"
    if (remainingMs <= 0L) return "now"
    val minutes = (remainingMs / 60_000L).coerceAtLeast(1L)
    val hours = minutes / 60L
    val mins = minutes % 60L
    return if (hours >= 1L) "${hours}h ${mins}m" else "${mins}m"
}

fun countdownLong(remainingMs: Long?): String {
    if (remainingMs == null) return "—"
    if (remainingMs <= 0L) return "now"
    val minutes = (remainingMs / 60_000L).coerceAtLeast(1L)
    val hours = minutes / 60L
    val mins = minutes % 60L
    return if (hours >= 1L) "in ${hours}h ${mins}m" else "in ${mins}m"
}

/** Flat next-prayer view for the Tile + Complication (separate processes, read from cache). */
data class NextPrayerView(
    val name: String,
    val time: String,
    val countdownShort: String,
    val countdownLong: String,
)

fun nextPrayerView(
    context: Context,
    payload: SyncPayload?,
    now: Instant = Instant.now(),
): NextPrayerView {
    val next = payload?.nextPrayer
    val name = next?.name
        ?.let { localizedPrayerName(context, it) }
        ?.takeIf { it.isNotBlank() }
        ?: "—"
    val time = formatLocalTime(next?.time, payload?.timezone) ?: "—:—"
    val remaining = remainingMillis(next?.time, now)
    return NextPrayerView(
        name = name,
        time = time,
        countdownShort = countdownShort(remaining),
        countdownLong = countdownLong(remaining),
    )
}
