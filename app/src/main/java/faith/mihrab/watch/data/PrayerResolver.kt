package faith.mihrab.watch.data

import android.util.Log
import java.time.Duration
import java.time.Instant

/**
 * Single source of truth for window resolution (master §2, §8).
 *
 * Prayer Home, the Tile and the Complication all resolve through here — in three separate
 * processes — so the ring and the RANGED_VALUE gauge can never disagree at the same moment.
 * [resolveNextPrayer] is expressed in terms of [resolveWindow] for the same reason: one code
 * path, so the "next prayer" and the window that closes on it cannot drift apart.
 */
private const val TAG = "PrayerResolver"

/**
 * The window the wearer is inside, computed locally from the payload's coordinates.
 *
 * Returns null when the payload cannot support a computation: no lat/lng, no
 * method/juristic/timezone, or the computation throws. A null window means the window is
 * *unknown* — not empty and not complete — and the surfaces render it as no arc at all.
 */
fun resolveWindow(
    payload: SyncPayload,
    service: PrayerComputationService = PrayerComputationService(),
    now: Instant = Instant.now(),
): PrayerWindow? {
    val lat = payload.location?.latitude
    val lng = payload.location?.longitude
    if (lat == null || lng == null) {
        Log.d(TAG, "window=null (lat/lng null)")
        return null
    }
    val method = payload.calculation?.method
    val juristic = payload.calculation?.juristic
    val timezone = payload.timezone
    if (method.isNullOrBlank() || juristic.isNullOrBlank() || timezone.isNullOrBlank()) {
        Log.d(TAG, "window=null (lat/lng present, method/juristic/timezone missing)")
        return null
    }
    return runCatching {
        service.findWindow(lat, lng, method, juristic, timezone, now)
    }.getOrElse { t ->
        Log.w(TAG, "window compute failed", t)
        null
    }
}

/**
 * The salah closing this window, or the phone-pushed `next_prayer` when the window is unknown.
 * Callers that already hold a window use this directly rather than resolving twice.
 */
fun PrayerWindow?.nextOrFallback(payload: SyncPayload): SyncNextPrayer? =
    this?.next?.let { SyncNextPrayer(name = it.name, time = it.time.toString()) }
        ?: payload.nextPrayer

/**
 * The locally computed next prayer, falling back to the phone-pushed `next_prayer` when the
 * window cannot be computed. Returns SyncNextPrayer? so all consumers stay a drop-in.
 */
fun resolveNextPrayer(
    payload: SyncPayload,
    service: PrayerComputationService = PrayerComputationService(),
    now: Instant = Instant.now(),
): SyncNextPrayer? = resolveWindow(payload, service, now).nextOrFallback(payload)

/**
 * Fraction of the current prayer window elapsed — the Prayer Home ring and the Complication's
 * RANGED_VALUE gauge.
 *
 * Anchored to the window (previous salah → next salah), never to when the phone last pushed,
 * so the same remaining time draws the same arc on every watch in the same window.
 *
 * Null means unknown: no window, or a degenerate one. Callers must render that as no arc —
 * neither empty nor full, both of which would be a claim the watch cannot make.
 */
fun windowProgress(window: PrayerWindow?, now: Instant): Float? {
    if (window == null) return null
    val total = Duration.between(window.previous.time, window.next.time).toMillis()
    if (total <= 0L) return null
    val elapsed = Duration.between(window.previous.time, now).toMillis().coerceAtLeast(0L)
    return (elapsed.toFloat() / total.toFloat()).coerceIn(0f, 1f)
}
