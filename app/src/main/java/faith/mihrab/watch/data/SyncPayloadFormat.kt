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

// Ring progress lives in PrayerResolver.windowProgress — it is anchored to the prayer window,
// not to `last_updated`, so it is window maths rather than formatting.

fun remainingMillis(nextIso: String?, now: Instant): Long? =
    parseInstant(nextIso)?.let { Duration.between(now, it).toMillis() }

fun countdownShort(context: Context, remainingMs: Long?): String {
    if (remainingMs == null) return "—"
    val totalMinutes = if (remainingMs <= 0L) 0L else (remainingMs / 60_000L).coerceAtLeast(1L)
    val hours = (totalMinutes / 60L).toInt()
    val mins = (totalMinutes % 60L).toInt()
    return if (hours >= 1) {
        context.getString(R.string.watch_countdown_short_hm, hours, mins)
    } else {
        context.getString(R.string.watch_countdown_short_m, mins)
    }
}

/**
 * The long-form countdown. IT CARRIES NO LEADING WORD ANY MORE — same ruling as the phone,
 * same reason: "in" looks small in English and huge in other languages. Every locale had its
 * own prefix or suffix ("dalam", "через", "dans", "... sonra"), and each one pushed the
 * countdown wider on a surface that has no width to give.
 *
 * It delegates to [countdownShort] rather than carrying its own resources, so the app, the
 * Tile and the Complication cannot drift apart — there is one string and one formatter now.
 * The name and the [NextPrayerView.countdownLong] field are kept because the Tile bottom line
 * and the Complication LONG_TEXT title read them; only the text they get has changed.
 */
fun countdownLong(context: Context, remainingMs: Long?): String =
    countdownShort(context, remainingMs)

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
        countdownShort = countdownShort(context, remaining),
        countdownLong = countdownLong(context, remaining),
    )
}
