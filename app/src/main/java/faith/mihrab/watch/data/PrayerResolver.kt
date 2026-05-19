package faith.mihrab.watch.data

import android.util.Log

/**
 * Single source of truth for the Option B fallback (master §2, §8). Returns the locally
 * computed next prayer when the payload carries lat/lng + method/juristic/timezone; else
 * falls back to phone-pushed payload.nextPrayer (current behavior). Until Session 3
 * populates lat/lng the fallback always fires → zero behavioral change this session.
 * Returns SyncNextPrayer? so all consumers stay a drop-in. Delete the fallback branch
 * once Session 3 guarantees lat/lng are always present.
 */
private const val TAG = "PrayerResolver"

fun resolveNextPrayer(
    payload: SyncPayload,
    service: PrayerComputationService = PrayerComputationService(),
): SyncNextPrayer? {
    val lat = payload.location?.latitude
    val lng = payload.location?.longitude
    if (lat == null || lng == null) {
        Log.d(TAG, "source=payload (lat/lng null)")
        return payload.nextPrayer
    }
    val method = payload.calculation?.method
    val juristic = payload.calculation?.juristic
    val timezone = payload.timezone
    if (method.isNullOrBlank() || juristic.isNullOrBlank() || timezone.isNullOrBlank()) {
        Log.d(TAG, "source=payload (lat/lng present, method/juristic/timezone missing)")
        return payload.nextPrayer
    }
    return runCatching {
        val computed = service.findNextPrayer(lat, lng, method, juristic, timezone)
        Log.d(TAG, "source=service lat=$lat lng=$lng method=$method juristic=$juristic")
        SyncNextPrayer(name = computed.name, time = computed.time.toString())
    }.getOrElse { t ->
        Log.w(TAG, "compute failed, falling back to payload.nextPrayer", t)
        payload.nextPrayer
    }
}
