package faith.mihrab.watch.data

import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import kotlin.math.abs

class PrayerComputationServiceTest {

    /**
     * Isolates "adhan2 wired correctly" from "UI wired correctly" before Session 2.
     * Reference fetched once from Aladhan and hardcoded — the test is pure JVM, offline,
     * deterministic. The "MWL"/"Shafi" inputs hit real `when` branches, so the
     * `android.util.Log.w` fallbacks are never invoked (no mockable-android needed).
     */
    @Test
    fun computeForDate_Bangkok_MWL_Shafi_2026_06_15_matchesAladhanReference() {
        val result = PrayerComputationService().computeForDate(
            date = LocalDate.of(2026, 6, 15),
            lat = 13.7563,
            lng = 100.5018,
            method = "MWL",
            juristic = "Shafi",
            timezone = "Asia/Bangkok",
        )

        // Aladhan: GET /v1/timings/15-06-2026?latitude=13.7563&longitude=100.5018&method=3&school=0
        // method=3 = MWL (response meta confirms Fajr 18° / Isha 17°), school=0 = Shafi.
        // Aladhan Fajr 04:31 Asia/Bangkok (UTC+7, no DST) = 2026-06-14T21:31:00Z.
        val expectedFajrUtc = Instant.parse("2026-06-14T21:31:00Z")
        val deltaMinutes = abs(Duration.between(expectedFajrUtc, result.fajr).toMinutes())

        assertTrue(
            "Fajr should be within ±2 min of Aladhan reference. " +
                "delta=$deltaMinutes min, computed=${result.fajr}",
            deltaMinutes <= 2,
        )
    }
}
