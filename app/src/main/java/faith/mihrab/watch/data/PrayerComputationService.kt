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
 * `now` is injected on every query rather than read from the clock inside, so the window logic
 * is deterministic and unit-testable without mocking Android.
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

    /** Chronological order (locked), sunrise included — the full astronomical set. */
    fun asList(): List<PrayerEntry> = listOf(
        PrayerEntry("fajr", fajr),
        PrayerEntry("sunrise", sunrise),
        PrayerEntry("dhuhr", dhuhr),
        PrayerEntry("asr", asr),
        PrayerEntry("maghrib", maghrib),
        PrayerEntry("isha", isha),
    )

    /**
     * The five salah, chronological. Sunrise is astronomy, not a prayer: it anchors no window
     * and is never the next prayer. This is the only place the exclusion is expressed — Prayer
     * Home, the Tile and the Complication all inherit it through [PrayerComputationService.findWindow].
     */
    fun salah(): List<PrayerEntry> = listOf(
        PrayerEntry("fajr", fajr),
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

/**
 * The prayer window the wearer is currently inside: the salah that opened it and the one that
 * closes it. Five windows a day. The night window behaves like every other one — it is simply
 * the longest, and after midnight its [previous] is yesterday's Isha.
 */
data class PrayerWindow(
    val previous: NextPrayer,
    val next: NextPrayer,
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

    /**
     * The window containing [now]. One [computeForDate] in the common case, two at the two
     * edges of the night window (before Fajr needs yesterday; after Isha needs tomorrow).
     */
    fun findWindow(
        lat: Double,
        lng: Double,
        method: String,
        juristic: String,
        timezone: String,
        now: Instant = Instant.now(),
    ): PrayerWindow {
        val zone = ZoneId.of(timezone)
        val today = now.atZone(zone).toLocalDate()

        fun compute(date: LocalDate) = computeForDate(date, lat, lng, method, juristic, timezone)

        val todaySalah = compute(today).salah()
        val index = todaySalah.indexOfFirst { it.time.isAfter(now) }

        return when {
            index > 0 -> PrayerWindow(
                previous = todaySalah[index - 1].asNextPrayer(),
                next = todaySalah[index].asNextPrayer(),
            )
            // Before today's Fajr — the night window opened with yesterday's Isha.
            index == 0 -> PrayerWindow(
                previous = compute(today.minusDays(1)).salah().last().asNextPrayer(),
                next = todaySalah.first().asNextPrayer(),
            )
            // All of today's salah have passed — the night window closes on tomorrow's Fajr.
            else -> PrayerWindow(
                previous = todaySalah.last().asNextPrayer(),
                next = compute(today.plusDays(1)).salah().first().asNextPrayer(),
            )
        }
    }

    /**
     * The salah that closes the current window. No isTomorrow flag, no "tomorrow" label — the
     * UI trusts the countdown (master §8).
     */
    fun findNextPrayer(
        lat: Double,
        lng: Double,
        method: String,
        juristic: String,
        timezone: String,
        now: Instant = Instant.now(),
    ): NextPrayer = findWindow(lat, lng, method, juristic, timezone, now).next

    private fun ComputedPrayerTimes.PrayerEntry.asNextPrayer() = NextPrayer(name, time)

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
