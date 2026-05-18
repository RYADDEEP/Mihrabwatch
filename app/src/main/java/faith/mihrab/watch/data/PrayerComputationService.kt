package faith.mihrab.watch.data

import android.util.Log
import com.batoulapps.adhan2.CalculationMethod
import com.batoulapps.adhan2.CalculationParameters
import com.batoulapps.adhan2.Coordinates
import com.batoulapps.adhan2.Madhab
import com.batoulapps.adhan2.PrayerTimes
import com.batoulapps.adhan2.data.DateComponents
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.time.ExperimentalTime
import kotlin.time.toJavaInstant

/**
 * Option B local prayer-time computation (docs/MIHRAB_SMARTWATCH_MASTER_APPROACH.md §2, §8).
 *
 * The watch computes prayer times locally from phone-synced settings (lat/lng, method,
 * juristic, timezone) instead of reading precomputed times from the payload, so it stays
 * accurate with no phone dependency. Stateless and side-effect free — callers cache if needed.
 *
 * Session 1 of 3: this service is dead code until Session 2 wires the UI/Tile/Complication.
 */

data class ComputedPrayerTimes(
    val fajr: Instant,
    val sunrise: Instant,
    val dhuhr: Instant,
    val asr: Instant,
    val maghrib: Instant,
    val isha: Instant,
    val computedDate: LocalDate,
    val timezone: String,
) {
    data class PrayerEntry(val name: String, val time: Instant)

    /** Chronological order (locked) — `findNextPrayer` relies on this for its forward scan. */
    fun asList(): List<PrayerEntry> = listOf(
        PrayerEntry("fajr", fajr),
        PrayerEntry("sunrise", sunrise),
        PrayerEntry("dhuhr", dhuhr),
        PrayerEntry("asr", asr),
        PrayerEntry("maghrib", maghrib),
        PrayerEntry("isha", isha),
    )
}

/** Always a real prayer name + real instant — post-Isha this is tomorrow's Fajr (master §8). */
data class NextPrayer(
    val name: String,
    val time: Instant,
)

// adhan2 exposes kotlin.time.Instant, still experimental in Kotlin 2.2.20 — opt in at the
// single boundary where it is converted to java.time.Instant (computeForDate).
@OptIn(ExperimentalTime::class)
class PrayerComputationService {

    fun computeForDate(
        date: LocalDate,
        lat: Double,
        lng: Double,
        method: String,
        juristic: String,
        timezone: String,
    ): ComputedPrayerTimes {
        val coordinates = Coordinates(lat, lng)
        // CalculationParameters is an immutable data class — madhab is a val, not assignable.
        val params = mapMethodToParams(method).copy(madhab = mapJuristic(juristic))
        val dateComponents = DateComponents(date.year, date.monthValue, date.dayOfMonth)
        val times = PrayerTimes(coordinates, dateComponents, params)

        // adhan2 exposes kotlin.time.Instant; convert at the boundary to java.time.Instant
        // (the type the rest of the watch codebase uses — see SyncPayloadFormat.kt).
        return ComputedPrayerTimes(
            fajr = times.fajr.toJavaInstant(),
            sunrise = times.sunrise.toJavaInstant(),
            dhuhr = times.dhuhr.toJavaInstant(),
            asr = times.asr.toJavaInstant(),
            maghrib = times.maghrib.toJavaInstant(),
            isha = times.isha.toJavaInstant(),
            computedDate = date,
            timezone = timezone,
        )
    }

    fun findNextPrayer(
        lat: Double,
        lng: Double,
        method: String,
        juristic: String,
        timezone: String,
    ): NextPrayer {
        val zone = ZoneId.of(timezone)
        val now = Instant.now()
        val today = LocalDate.now(zone)

        val todayTimes = computeForDate(today, lat, lng, method, juristic, timezone)
        todayTimes.asList().firstOrNull { it.time.isAfter(now) }
            ?.let { return NextPrayer(name = it.name, time = it.time) }

        // All of today's prayers have passed → return tomorrow's Fajr as a real instant.
        // No isTomorrow flag, no "tomorrow" label — the UI trusts the countdown (master §8).
        val tomorrowTimes = computeForDate(today.plusDays(1), lat, lng, method, juristic, timezone)
        return NextPrayer(name = "fajr", time = tomorrowTimes.fajr)
    }

    private fun mapMethodToParams(method: String): CalculationParameters =
        when (method.uppercase()) {
            "MWL", "MUSLIM_WORLD_LEAGUE", "MUSLIMWORLDLEAGUE" ->
                CalculationMethod.MUSLIM_WORLD_LEAGUE.parameters
            "ISNA", "NORTH_AMERICA", "NORTHAMERICA" ->
                CalculationMethod.NORTH_AMERICA.parameters
            "EGYPTIAN", "EGYPT" ->
                CalculationMethod.EGYPTIAN.parameters
            // Sync schema writes "Makkah" for the Umm al-Qura method.
            "MAKKAH", "UMM_AL_QURA", "UMMALQURA" ->
                CalculationMethod.UMM_AL_QURA.parameters
            "KARACHI" ->
                CalculationMethod.KARACHI.parameters
            "DUBAI" ->
                CalculationMethod.DUBAI.parameters
            "QATAR" ->
                CalculationMethod.QATAR.parameters
            "KUWAIT" ->
                CalculationMethod.KUWAIT.parameters
            "SINGAPORE" ->
                CalculationMethod.SINGAPORE.parameters
            "TURKEY", "DIYANET" ->
                CalculationMethod.TURKEY.parameters
            "MOONSIGHTING", "MOON_SIGHTING_COMMITTEE", "MOONSIGHTINGCOMMITTEE" ->
                CalculationMethod.MOON_SIGHTING_COMMITTEE.parameters
            else -> {
                Log.w("PrayerComputationService", "Unknown method: $method, defaulting to MWL")
                CalculationMethod.MUSLIM_WORLD_LEAGUE.parameters
            }
        }

    private fun mapJuristic(juristic: String): Madhab =
        when (juristic.lowercase()) {
            "hanafi" -> Madhab.HANAFI
            "shafi", "shafii", "shafi'i", "standard" -> Madhab.SHAFI
            else -> {
                Log.w("PrayerComputationService", "Unknown juristic: $juristic, defaulting to Shafi")
                Madhab.SHAFI
            }
        }
}
