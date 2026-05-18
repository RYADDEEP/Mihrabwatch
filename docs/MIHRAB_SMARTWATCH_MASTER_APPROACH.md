# MIHRAB SMARTWATCH — MASTER APPROACH v2
## Local Computation Architecture (Option B)
## Last Updated: May 18, 2026 (corrected)

---

## PURPOSE

This document replaces the previous SmartWatch sync-dependent architecture with **Option B: Watch computes prayer times locally.** Every AI model working on Mihrabwatch must read this file before writing any code. **THIS FILE supersedes all prior watch architecture decisions.**

---

## THE ARCHITECTURAL PIVOT

### BEFORE (v1 — Phone-Dependent, REPLACED)

```
Phone computes prayer times → writes to Supabase → Watch reads times
    ↓
Problem: If user doesn't open phone → Watch shows stale data
Problem: "now" / "in 0m" displayed for hours until phone re-syncs
Problem: User obligated to open app daily — unprofessional
```

### AFTER (v2 — Local Computation, CURRENT)

```
Phone syncs SETTINGS to Supabase (lat/lng, method, juristic, timezone)
    ↓
Watch reads SETTINGS via Realtime subscription
    ↓
Watch computes prayer times LOCALLY using adhan-kotlin library
    ↓
Watch auto-advances through prayers throughout the day
    ↓
No phone dependency for daily prayer display
Watch is self-sufficient as long as settings don't change
```

### Why This Change

| Question | Answer |
|----------|--------|
| What if user doesn't open phone for 3 days? | **v1:** Watch shows 3-day-old times. **v2:** Watch computes fresh times daily — always accurate |
| What if user travels to a new city? | **v1:** Stale until phone syncs. **v2:** Stale until phone syncs (same — settings change requires phone) |
| What if prayer time passes mid-day? | **v1:** Stuck on "in 0m" until phone re-syncs. **v2:** Watch auto-advances to next prayer locally |
| Does watch need internet for prayer times? | **v1:** Yes (reads from Supabase). **v2:** No — computes locally from cached settings |
| Does watch need phone to be alive? | **v1:** Yes. **v2:** No — only needs phone when SETTINGS change (rare) |

---

## WHAT'S ALREADY BUILT (DO NOT REDO)

From the `MIHRABWATCH_PROJECT_REPORT.md` (May 17-18, 2026), main HEAD `e6c29df`:

| Component | Status | Files | Keep? |
|-----------|:------:|-------|:-----:|
| Repo + Kotlin Compose scaffold | ✅ Shipped | Project structure | ✅ KEEP |
| device_pairings migration | ✅ Shipped | Supabase + mihrab-app + Mihrabtv | ✅ KEEP |
| Pairing flow (code entry → pair → DataStore) | ✅ Shipped | PairingScreen.kt, PairingRepository.kt, PairingDataStore.kt | ✅ KEEP |
| Prayer Home screen (gold ring, 3 items) | ✅ Shipped | PrayerHomeScreen.kt | ✅ KEEP — **MODIFY data source** |
| Qibla Compass screen | ✅ Shipped | QiblaCompassScreen.kt, QiblaCompassRepository.kt | ✅ KEEP — **MODIFY bearing source** |
| Tile service | ✅ Shipped | MihrabTileService.kt | ✅ KEEP — **MODIFY data source** |
| Complication service | ✅ Shipped | MihrabComplicationDataSourceService.kt | ✅ KEEP — **MODIFY data source** |
| SyncPayloadRepository (Realtime + cache) | ✅ Shipped | SyncPayloadRepository.kt, SyncPayloadCache.kt | ✅ KEEP — **MODIFY what it reads** |
| SyncPayload.kt data classes | ✅ Shipped | SyncPayload.kt | ✅ KEEP — **ADD lat/lng fields** |
| SyncPayloadFormat.kt (UTC→local, countdown) | ✅ Shipped | SyncPayloadFormat.kt | ✅ KEEP |
| Pairing screen (28sp, auto-refresh) | ✅ Shipped | PairingScreen.kt | ✅ KEEP — no changes |
| MihrabWatchApp.kt (router) | ✅ Shipped | MihrabWatchApp.kt | ✅ KEEP |

**All infrastructure is reusable.** The pivot is ADD + MODIFY, not rebuild.

---

## WHAT CHANGES (Option B Implementation)

### 1. ADD: adhan-kotlin Library

Add `adhan-kotlin` (or `adhan` for Kotlin) to `app/build.gradle.kts`:

```kotlin
implementation("com.batoulapps.adhan:adhan2:x.y.z")
```

This library computes prayer times from coordinates + calculation method + juristic school. Pure math — no API calls, no internet needed. ~50KB.

### 2. ADD: PrayerComputationService.kt (NEW)

```
File: app/src/main/java/faith/mihrab/watch/data/PrayerComputationService.kt
```

```kotlin
class PrayerComputationService {
    
    fun computeForDate(
        date: LocalDate,
        lat: Double,
        lng: Double,
        method: String,     // "MWL", "ISNA", "Egyptian", etc.
        juristic: String,   // "Shafi" or "Hanafi"
        timezone: String    // "Asia/Bangkok"
    ): ComputedPrayerTimes {
        val coordinates = Coordinates(lat, lng)
        val params = mapMethodToParams(method)
        params.madhab = if (juristic == "Hanafi") Madhab.HANAFI else Madhab.SHAFI
        
        val dateComponents = DateComponents(date.year, date.monthValue, date.dayOfMonth)
        val times = PrayerTimes(coordinates, dateComponents, params)
        
        return ComputedPrayerTimes(
            fajr = times.fajr,
            sunrise = times.sunrise,
            dhuhr = times.dhuhr,
            asr = times.asr,
            maghrib = times.maghrib,
            isha = times.isha,
            computedDate = date,
            timezone = timezone
        )
    }
    
    fun findNextPrayer(
        lat: Double, lng: Double,
        method: String, juristic: String, timezone: String
    ): NextPrayer {
        val zone = ZoneId.of(timezone)
        val now = Instant.now()
        val today = LocalDate.now(zone)
        
        // 1. Compute today's times
        val todayTimes = computeForDate(today, lat, lng, method, juristic, timezone)
        
        // 2. Find first future prayer in today's list
        val nextToday = todayTimes.asList().firstOrNull { it.time.isAfter(now) }
        
        if (nextToday != null) {
            return NextPrayer(name = nextToday.name, time = nextToday.time)
        }
        
        // 3. All today's prayers passed (post-Isha) → compute TOMORROW's Fajr
        val tomorrow = today.plusDays(1)
        val tomorrowTimes = computeForDate(tomorrow, lat, lng, method, juristic, timezone)
        return NextPrayer(name = "fajr", time = tomorrowTimes.fajr)
    }
}
```

**Key: `findNextPrayer()` ALWAYS returns a real time and a real name.** No vague labels. No "tomorrow" text. No `isTomorrow` flag. The watch ALWAYS shows a prayer time with a countdown — whether it's today's Asr or tomorrow's Fajr.

### 3. MODIFY: SyncPayload.kt — Add lat/lng

```kotlin
@Serializable
data class SyncLocation(
    @SerialName("display_name") val displayName: String? = null,
    val lat: Double? = null,    // NEW — for local computation
    val lng: Double? = null     // NEW — for local computation
)
```

### 4. MODIFY: Phone-side sync_payload (mihrab-app)

The phone's `buildSyncPayload()` must include `lat` and `lng` in the location object for watch pairings. The `prayers[]` array can STILL be included (TV needs it), but the watch will IGNORE it and compute locally.

```json
{
    "schema_version": 1,
    "location": {
        "display_name": "Bangkok, Thailand",
        "lat": 13.7563,
        "lng": 100.5018
    },
    "calculation": {"method": "MWL", "juristic": "Hanafi"},
    "timezone": "Asia/Bangkok",
    "prayers": [...],
    "next_prayer": {...},
    "qibla": {"bearing_degrees": 294, "cardinal": "West-Northwest"}
}
```

**TV continues to read `prayers[]` and `next_prayer`.** Watch reads `lat`, `lng`, `method`, `juristic`, `timezone` and computes locally. Same payload, different consumers.

### 5. MODIFY: PrayerHomeScreen.kt — Read from Local Computation

```
BEFORE:
val nextPrayer = payload.next_prayer  // from phone
val time = convertUtcToLocal(nextPrayer.time, payload.timezone)

AFTER:
val settings = payload.location (lat/lng) + payload.calculation (method/juristic) + payload.timezone
val nextPrayer = prayerService.findNextPrayer(lat, lng, method, juristic, timezone)
// Returns real time — whether today's Asr or tomorrow's Fajr
// Auto-advances throughout the day — no phone dependency
```

### 6. MODIFY: Tile + Complication — Read from Local Computation

Same change as PrayerHomeScreen — read from `PrayerComputationService` instead of `payload.next_prayer`. Cache computed times in DataStore for cross-process access.

### 7. ADD: Daily Recomputation Trigger

At midnight local time, recompute prayer times for the new day:

```kotlin
LaunchedEffect(Unit) {
    while (true) {
        val now = LocalDateTime.now(zone)
        val midnight = now.toLocalDate().plusDays(1).atStartOfDay(zone)
        val delayMs = Duration.between(now, midnight).toMillis()
        delay(delayMs)
        // New day — recompute triggers automatically via findNextPrayer()
        // which always checks today's date
    }
}
```

### 8. Post-Isha Behavior

When all today's prayers have passed (after Isha), `findNextPrayer()` computes tomorrow's Fajr and returns its REAL time:

```
Post-Isha at 21:30 — watch shows:
┌──────────────────────────────────┐
│          [Gold Ring]             │
│                                  │
│           04:35                  │  ← REAL tomorrow's Fajr time
│           FAJR                   │  ← Prayer name
│         in 7h 5m                │  ← REAL countdown to tomorrow's Fajr
│                                  │
└──────────────────────────────────┘
```

**The watch ALWAYS shows a real prayer time with a real countdown.** No vague labels. No "tomorrow" text. At 21:30, "FAJR in 7h 5m" is self-explanatory — the user understands it's tomorrow's Fajr without needing a label.

At midnight, the daily recomputation fires. But functionally nothing changes — `findNextPrayer()` already computed tomorrow's Fajr. The recomputation just refreshes the internal date reference.

---

## WHAT STAYS UNCHANGED

| Component | Why No Change |
|-----------|--------------|
| Pairing flow | Watch still pairs via code entry → DataStore |
| Realtime subscription | Still needed — listens for SETTINGS changes (location, method) |
| SyncPayloadRepository | Still reads payload — but focus shifts to settings fields |
| DataStore cache | Still caches data — now caches settings + computed times |
| Visual design (gold ring, OLED black, 3-item layout) | Design doesn't change — only data source changes |
| Qibla bearing from payload | KEEP — phone still pre-computes bearing (or watch could compute from lat/lng) |
| Tile + Complication structure | Same services — different data source |
| MihrabWatchApp.kt router | No navigation changes |

---

## PRIVACY REVISION

| Decision | v1 (Old) | v2 (Current) |
|----------|---------|-------------|
| "No lat/lng on devices" | Applied to ALL devices (TV, Watch, Car) | **REVISED: Watch gets lat/lng. TV does NOT.** Watch is a personal device (on user's wrist). TV is shared (visible to room). Different privacy needs. |

---

## SESSION PLAN (3 Sessions)

### Session 1: Add adhan-kotlin + PrayerComputationService

**Mihrabwatch repo, Opus 4.7, High, Plan Mode ON**

| Task | Details |
|------|---------|
| Add `adhan-kotlin` dependency | build.gradle.kts |
| Create `PrayerComputationService.kt` | Takes date/lat/lng/method/juristic/timezone → returns 6 prayer times |
| Create `ComputedPrayerTimes` data class | Holds the 6 times + computed date + timezone |
| Create method mapping | "MWL" → CalculationMethod.MUSLIM_WORLD_LEAGUE, etc. |
| Create `findNextPrayer()` | Scans today's times, if all past → computes tomorrow's Fajr with real time |
| Unit test (optional) | Verify computation matches Aladhan API for Bangkok |

### Session 2: Wire PrayerHome + Tile + Complication to Local Computation

**Mihrabwatch repo, Opus 4.7, High, Plan Mode ON**

| Task | Details |
|------|---------|
| Modify PrayerHomeScreen | Read from PrayerComputationService instead of payload.next_prayer |
| Modify Tile | Read from locally computed times (DataStore cached) |
| Modify Complication | Same as Tile |
| Add midnight recomputation trigger | LaunchedEffect waiting until midnight → refresh date |
| Add minutely countdown refresh | Existing pattern — just swap data source |
| Post-Isha | `findNextPrayer()` returns tomorrow's Fajr with real time + real countdown |

### Session 3: Phone-side lat/lng Addition (mihrab-app)

**mihrab-app repo, Opus 4.7, Medium, No Plan Mode**

| Task | Details |
|------|---------|
| Modify `buildSyncPayload()` | Include `lat` and `lng` in location object |
| Keep `prayers[]` and `next_prayer` | TV still needs them — don't remove |
| Test | Fresh pair → verify lat/lng in Supabase → watch computes locally |

---

## VERIFICATION PLAN

After all 3 sessions:

| # | Test | Expected |
|---|------|----------|
| 1 | Watch paired, phone synced settings | Watch shows correct prayer times (computed locally) |
| 2 | Wait for prayer time to pass | Watch auto-advances to next prayer (no phone needed) |
| 3 | Wait for all prayers to pass (post-Isha) | Watch shows tomorrow's Fajr with REAL time and REAL countdown |
| 4 | Wait for midnight | Watch recomputes — date reference refreshes |
| 5 | Phone offline / closed for 24 hours | Watch still shows correct times (computed from cached settings) |
| 6 | Change location on phone | Watch receives new lat/lng via Realtime → recomputes with new location |
| 7 | Change calc method on phone | Watch receives new method → recomputes |
| 8 | Tile shows correct data | Locally computed, not from payload |
| 9 | Complication shows correct data | Locally computed, not from payload |
| 10 | Qibla bearing correct | From payload (or locally computed from lat/lng) |

**Test #5 is THE critical test** — proves the watch is truly self-sufficient.

---

## WHAT THE NEW BRAIN NEEDS TO KNOW

1. **Watch computes locally.** The `adhan-kotlin` library does the math. No Aladhan API calls on the watch. No internet needed for prayer times.

2. **Phone syncs SETTINGS, not times.** The phone writes lat/lng + method + juristic + timezone. The watch uses these to compute. TV still reads `prayers[]` from the same payload.

3. **Everything infrastructure is built.** Pairing, Realtime, DataStore, screens, Tile, Complication — all working. The pivot only changes the DATA SOURCE inside existing screens.

4. **3 sessions, 1-2 days.** Session 1 (add library + computation service), Session 2 (wire screens), Session 3 (phone adds lat/lng).

5. **Post-Isha: watch computes tomorrow's Fajr with real time.** `findNextPrayer()` ALWAYS returns a real prayer name + real time + real countdown. No vague labels. No "tomorrow" text. The function checks today first, then tomorrow if all today's prayers have passed.

6. **Privacy revised for watch only.** Watch gets lat/lng (personal device). TV does NOT (shared device).

7. **The current code on main (`e6c29df`) works** — it just depends on phone for prayer data. After the pivot, it computes locally. Same screens, same design, different data source.

---

## FILE READING ORDER FOR NEW BRAIN

```
CRITICAL:
1. THIS FILE (MIHRAB_SMARTWATCH_MASTER_APPROACH.md) — architecture + sessions
2. MIHRAB_WATCH_DESIGN_GUIDE.md — visual spec (unchanged)
3. MIHRABWATCH_PROJECT_REPORT.md — what's built, file paths, verified behavior
4. MIHRAB_CLAUDE_CODE_PROMPT_FORMAT.md — prompt structure

MUST READ:
5. MIHRAB_HANDOVER_MAY2026.md — full project state
6. MIHRAB_PRE_LAUNCH_ROADMAP.md — locked task order

REFERENCE:
7. MIHRAB_SYNC_PAYLOAD_SCHEMA.md — payload schema (being extended with lat/lng)
8. MIHRAB_AI_OPERATING_PROTOCOL.md — behavioral rules
```

---

## DOCUMENT INFO

| Field | Value |
|-------|-------|
| Created | May 18, 2026 |
| Author | Master Model (Claude Opus 4.6) |
| Replaces | Previous sync-dependent watch architecture |
| Status | ACTIVE — v2 local computation is the current spec |
| Key change | Watch computes prayer times locally via adhan-kotlin |
| Post-Isha | Watch computes tomorrow's Fajr — ALWAYS real time, NEVER vague labels |
| Sessions remaining | 3 (add library + wire screens + phone lat/lng) |
| Repos affected | Mihrabwatch (Sessions 1-2) + mihrab-app (Session 3) |

---

*The watch doesn't wait for the phone. The watch computes.*
*One sync of settings. Self-sufficient for life.*
*Always a real prayer time. Always a real countdown.*
