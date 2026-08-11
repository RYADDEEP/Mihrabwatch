package faith.mihrab.watch.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * The window maths behind the Prayer Home ring and the Complication's RANGED_VALUE gauge.
 *
 * Pure JVM, no mocking: `findWindow` takes an injected `now`, and the known "MWL"/"Shafi"
 * inputs hit real `when` branches so the `android.util.Log.w` fallbacks are never invoked.
 * `resolveWindow` is deliberately not tested here — it logs on every path, which this project
 * has no test mocking configured for.
 *
 * Times are computed rather than hardcoded: the absolute values are already pinned against an
 * Aladhan reference by [PrayerComputationServiceTest]. What is asserted here is the *window*
 * — which prayer opens it and which closes it — including the two edges that cross midnight.
 */
class PrayerWindowTest {

    private val service = PrayerComputationService()
    private val lat = 13.7563
    private val lng = 100.5018
    private val method = "MWL"
    private val juristic = "Shafi"
    private val timezone = "Asia/Bangkok"
    private val zone: ZoneId = ZoneId.of(timezone)
    private val date: LocalDate = LocalDate.of(2026, 6, 15)

    private fun times(on: LocalDate) =
        service.computeForDate(on, lat, lng, method, juristic, timezone)

    private fun windowAt(now: Instant) =
        service.findWindow(lat, lng, method, juristic, timezone, now)

    // --- sunrise is not a prayer ------------------------------------------------

    @Test
    fun salah_excludesSunrise_andStaysChronological() {
        assertEquals(
            listOf("fajr", "dhuhr", "asr", "maghrib", "isha"),
            times(date).salah().map { it.name },
        )
    }

    @Test
    fun window_betweenFajrAndSunrise_closesOnDhuhrNotSunrise() {
        val today = times(date)
        val now = today.fajr.plus(Duration.ofMinutes(30))
        // Fixture guard: this instant must genuinely sit before sunrise, or the test proves nothing.
        assertTrue("expected $now to precede sunrise ${today.sunrise}", now.isBefore(today.sunrise))

        val window = windowAt(now)
        assertEquals("fajr", window.previous.name)
        assertEquals("dhuhr", window.next.name)
        assertEquals(today.fajr, window.previous.time)
        assertEquals(today.dhuhr, window.next.time)
    }

    @Test
    fun findNextPrayer_neverReturnsSunrise() {
        val today = times(date)
        val justAfterFajr = today.fajr.plus(Duration.ofMinutes(30))
        assertEquals(
            "dhuhr",
            service.findNextPrayer(lat, lng, method, juristic, timezone, justAfterFajr).name,
        )
    }

    // --- the ordinary windows ---------------------------------------------------

    @Test
    fun window_justAfterDhuhr_isDhuhrToAsr() {
        val today = times(date)
        val window = windowAt(today.dhuhr.plusSeconds(60))
        assertEquals("dhuhr", window.previous.name)
        assertEquals("asr", window.next.name)
        assertEquals(today.dhuhr, window.previous.time)
        assertEquals(today.asr, window.next.time)
    }

    @Test
    fun window_justAfterMaghrib_isMaghribToIsha() {
        val today = times(date)
        val window = windowAt(today.maghrib.plusSeconds(60))
        assertEquals("maghrib", window.previous.name)
        assertEquals("isha", window.next.name)
    }

    // --- the night window, which crosses midnight twice -------------------------

    @Test
    fun window_afterMidnightBeforeFajr_isYesterdayIshaToTodayFajr() {
        val today = times(date)
        val yesterday = times(date.minusDays(1))
        val now = today.fajr.minus(Duration.ofHours(2))
        // Fixture guard: still the same local calendar date, i.e. genuinely after midnight.
        assertEquals(date, now.atZone(zone).toLocalDate())

        val window = windowAt(now)
        assertEquals("isha", window.previous.name)
        assertEquals(yesterday.isha, window.previous.time)
        assertEquals("fajr", window.next.name)
        assertEquals(today.fajr, window.next.time)
    }

    @Test
    fun window_afterIsha_isTodayIshaToTomorrowFajr() {
        val today = times(date)
        val tomorrow = times(date.plusDays(1))
        val now = today.isha.plusSeconds(60)
        // Fixture guard: still the same local calendar date, i.e. before midnight.
        assertEquals(date, now.atZone(zone).toLocalDate())

        val window = windowAt(now)
        assertEquals("isha", window.previous.name)
        assertEquals(today.isha, window.previous.time)
        assertEquals("fajr", window.next.name)
        assertEquals(tomorrow.fajr, window.next.time)
    }

    @Test
    fun window_nightIsTheLongestOfTheDay() {
        val today = times(date)
        val tomorrow = times(date.plusDays(1))
        val night = Duration.between(today.isha, tomorrow.fajr)
        val daytime = today.salah()
            .zipWithNext { a, b -> Duration.between(a.time, b.time) }
        assertTrue(
            "night $night should exceed every daytime window $daytime",
            daytime.all { night > it },
        )
    }

    // --- windowProgress ---------------------------------------------------------
    // Note the signature: (window, now). `last_updated` is not an input and cannot be, which
    // is what makes the same remaining time draw the same arc on every watch.

    @Test
    fun windowProgress_isNullWhenTheWindowIsUnknown() {
        assertNull(windowProgress(null, Instant.parse("2026-06-15T09:00:00Z")))
    }

    @Test
    fun windowProgress_isNullForADegenerateWindow() {
        val t = Instant.parse("2026-06-15T09:00:00Z")
        assertNull(windowProgress(PrayerWindow(NextPrayer("isha", t), NextPrayer("fajr", t)), t))
    }

    @Test
    fun windowProgress_runsZeroToOneAcrossTheWindow() {
        val open = Instant.parse("2026-06-15T12:00:00Z")
        val close = open.plus(Duration.ofHours(4))
        val window = PrayerWindow(NextPrayer("dhuhr", open), NextPrayer("asr", close))

        assertEquals(0f, windowProgress(window, open)!!, TOLERANCE)
        assertEquals(0.25f, windowProgress(window, open.plus(Duration.ofHours(1)))!!, TOLERANCE)
        assertEquals(0.5f, windowProgress(window, open.plus(Duration.ofHours(2)))!!, TOLERANCE)
        assertEquals(1f, windowProgress(window, close)!!, TOLERANCE)
    }

    @Test
    fun windowProgress_clampsOutsideTheWindow() {
        val open = Instant.parse("2026-06-15T12:00:00Z")
        val close = open.plus(Duration.ofHours(4))
        val window = PrayerWindow(NextPrayer("dhuhr", open), NextPrayer("asr", close))

        assertEquals(0f, windowProgress(window, open.minus(Duration.ofHours(1)))!!, TOLERANCE)
        assertEquals(1f, windowProgress(window, close.plus(Duration.ofHours(1)))!!, TOLERANCE)
    }

    private companion object {
        const val TOLERANCE = 1e-4f
    }
}
