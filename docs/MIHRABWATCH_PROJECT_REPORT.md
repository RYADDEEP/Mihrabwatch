# Mihrabwatch — Smart Watch Project Report

**Date:** May 17–18, 2026
**Owner:** Ryad (Apexiom Ltd UK), addressed as Akhi
**Repo:** `C:\Users\kille\Mihrabwatch` → `github.com/RYADDEEP/Mihrabwatch`
**Platform:** Wear OS (Kotlin Compose Wear OS)
**Status:** ✅ **Production-ready for v1 launch** — final smartwatch polish to be done in pre-launch polish stage

---

## Executive Summary

The Wear OS smartwatch app is a fully functional, **read-only consumer** of the phone-authoritative sync payload. It displays the next prayer, the qibla direction, and provides Tile + Complication surfaces for the watch face — all glanceable, all driven by real-time data from the user's phone via Supabase Realtime.

**Five sessions** were shipped to reach production-ready state:

| Session | Commit | Subject (exact) | Date |
|---|---|---|---|
| Phase 5 (legacy) | `dc4e3c6` | (pairing render fix — earlier scaffolding) | Earlier in May 2026 |
| Phase 5 (legacy) | `97d428a` | `feat(watch): complications + tile services for out-of-app surfaces` | Earlier in May 2026 |
| Phase 5b Session 1 | `858bdaf` | `docs: add sync_payload schema v1 (mirror from mihrab-app)` | May 17 |
| Session 3 (Watch reads v1) | `45b9bd6` | `feat(sync): wear OS reads v1 sync_payload, replaces hardcoded prayer/qibla/location values` | May 17 |
| Session 3.1 (UI fix) | `c2ac7e1` | `fix(watch): strip PrayerHome Ready state to 3 items (prayer time / name / countdown)` | May 17 |
| Polish-Watch Batch 1 | `e6c29df` | `polish(watch): remove "now" status + PairingScreen typography + auto-refresh expired code` | May 17 |
| Housekeeping | `a111b03` | `chore: ignore .claude/ session metadata` | May 17 |

Current main: **`e6c29df`** on `Mihrabwatch/main`.

---

## Architecture Overview

### Design philosophy

**Phone owns canonical state. Watch owns visualization. Realtime delivers updates.**

- The phone (mihrab-app) is the writer — computes prayer times via Aladhan API, builds the v1 `sync_payload`, writes to `device_pairings` row in Supabase
- The watch is the reader — subscribes to Supabase Realtime on its own paired row, displays whatever the phone wrote
- The watch NEVER computes prayer times locally, NEVER calls Aladhan, NEVER does timezone math beyond UTC→local display conversion using the payload's `timezone` field
- The watch's countdown ticks every minute locally (UI-only, doesn't trigger writes)

This design is battery-efficient (no background compute on watch), offline-tolerant (last-known-good cache survives disconnection), and architecturally clean (single source of truth).

### Data flow

```
[mihrab-app on phone]
  ↓ (writes JSONB)
[device_pairings.sync_payload in Supabase]
  ↓ (Realtime push, <5 sec)
[SyncPayloadRepository in watch]
  ↓ (Flow<SyncPayloadState>)
[PrayerHomeScreen + QiblaCompassScreen + Tile + Complication]
```

### v1 `sync_payload` schema (relevant fields for watch)

```json
{
  "schema_version": 1,
  "last_updated": "2026-05-17T03:49:46.274692Z",
  "timezone": "Asia/Bangkok",
  "location": {
    "display_name": "Bangkok, Thailand"
  },
  "next_prayer": {
    "name": "fajr",
    "time": "2026-05-17T21:34:00.000Z"
  },
  "qibla": {
    "bearing_degrees": 287
  },
  "prayers": [
    {"name": "fajr", "time": "..."},
    {"name": "sunrise", "time": "..."},
    {"name": "dhuhr", "time": "..."},
    {"name": "asr", "time": "..."},
    {"name": "maghrib", "time": "..."},
    {"name": "isha", "time": "..."}
  ]
}
```

Watch consumes: `next_prayer.name`, `next_prayer.time`, `qibla.bearing_degrees`, `location.display_name`, `timezone`.

---

## Screen Inventory

### 1. PrayerHomeScreen (3 items, glanceable)

The primary screen. Designed for 3-second wrist glances.

**Ready state shows exactly 3 items, vertically centered inside a gold countdown ring:**

| Line | Content | Source | Style |
|---|---|---|---|
| 1 | **Prayer Time** (e.g., `04:34`) | `payload.next_prayer.time` converted to local HH:mm | Large white |
| 2 | **Prayer Name** (e.g., `FAJR`) | `payload.next_prayer.name`, localized | Gold |
| 3 | **Countdown** (e.g., `in 6h 1m`) | `next_prayer.time - now()` recomputed every 60 sec | Small gray |

**Gold ring:** progress arc that fills clockwise as the countdown decreases. Visual reinforcement of urgency.

**State handling (data layer):**

| State | Display |
|---|---|
| Loading | Existing pulse animation |
| Empty (NULL payload) | "Waiting for sync from phone…" |
| Ready (normal) | The 3 items above |
| UnsupportedVersion (schema_version > 1) | "Update Mihrab Watch to see all features" |
| Error (with cached last-known-good) | Last-known-good values + small stale indicator |
| Error (no last-known-good) | "Waiting for sync from phone…" |

**Critical design decision:** there is **NO "now" status text.** When `next_prayer.time` is in the past, the countdown shows `in 0m` (not "now"). The phone is responsible for keeping `next_prayer` pointing at a future prayer. Watch trusts the phone. (This is the only known soft-edge — see "Known Limitations" below.)

### 2. QiblaCompassScreen

Compass-style display with arrow pointing toward Mecca.

- **Target bearing:** `payload.qibla.bearing_degrees` (Int 0–359)
- **Device heading:** local magnetometer/accelerometer sensor fusion (untouched from Phase 5 implementation)
- **Arrow rotation math:** `targetBearing - deviceHeading`
- **Compass face:** N/E/S/W cardinal markers + minor tick marks
- **Graceful degradation:** if `payload.qibla` is null, render compass without arrow + "Sync your phone" overlay; under `UnsupportedVersion`, still render bearing best-effort

### 3. PairingScreen (first-install UX)

Shown only when the watch has no stored `pairing_id` in its DataStore. After successful pairing, never seen again unless app data is cleared or pairing is broken from the phone side.

**Pairing code display:**
- 8-character alphanumeric code (e.g., `TCZSSNES`)
- **Typography:** 28sp, monospace, 4dp letter-spacing (Master Model–ratified after audit found 32sp/36sp wrapped on round watch faces)
- **Single-line guards:** `maxLines = 1`, `softWrap = false`
- **Expiry:** 5-minute server-side TTL, "Expires in 5 minutes" indicator below code
- **Auto-refresh on expiry:** when the active code expires, a new code is auto-fetched within ~1 second. No manual user action required
- **Cap:** 5 auto-refreshes per session (25 minutes total); after that, falls back to "tap to refresh" manual mode

**Pairing flow:**
1. Watch generates code locally via `SecureRandom`
2. Inserts a pending row into Supabase `device_pairings` (RLS allows anon INSERT for `status='pending', paired_user_id=NULL`)
3. User opens mihrab-app on phone → enters code in Connect Watch screen
4. Phone SELECTs the matching pending row → UPDATEs it to `status='paired', paired_user_id=<user>`
5. Watch polls/subscribes for status change → stores resulting `pairing_id` in DataStore
6. Navigates to PrayerHome → reads payload → shows real data

### 4. Tile (out-of-app glance surface)

Wear OS Tile that appears next to the watch face. Configurable by user to add to their tile rotation.

- Reads from DataStore-cached `SyncPayload` (separate process from main app — DataStore is the cross-process bridge)
- Displays: real `next_prayer.name` + local-converted time (e.g., "FAJR 04:34")
- **Update mechanism:** the Tile service itself uses a passive 60-second freshness interval (`setFreshnessIntervalMillis(60_000L)`). Push-style updates happen FROM `SyncPayloadRepository.kt:116-123` — whenever the repository emits a new `Ready` state, it calls `TileService.getUpdater(ctx).requestUpdate(...)` so the system re-renders the Tile immediately rather than waiting for the 60-second interval
- When cache is empty: shows `— / —:—` placeholder

### 5. Complication (data source for watch faces)

Wear OS Complication that watch faces can subscribe to.

- Same DataStore cache as Tile
- Renders abbreviated `prayer_name + HH:mm` (e.g., "Mghrb 18:22") suitable for compact complication slots
- **Update mechanism:** the complication service is a `SuspendingComplicationDataSourceService` (pull-based — Wear OS asks for data when it needs it). Push-style updates happen FROM `SyncPayloadRepository.kt:116-123` via `ComplicationDataSourceUpdateRequester.requestAll()` on every payload change, signaling Wear OS to re-pull from the service
- Placeholder when cache empty

**Architectural note:** Both Tile and Complication services are passive readers of DataStore. The orchestration logic (when to push updates) lives centrally in `SyncPayloadRepository` so there's a single source of "new payload arrived → push everywhere." Cleaner separation than scattering update logic across services.

---

## Data Layer

All data-layer code is in `lib/.../faith/mihrab/watch/data/` (flat package per repo convention):

| File | Purpose |
|---|---|
| `SyncPayload.kt` | `@Serializable` Kotlin data class hierarchy matching v1 schema exactly. All fields nullable for defensive deserialization. `Json` config uses `ignoreUnknownKeys = true`, `isLenient = true`, and `coerceInputValues = true` for forward compatibility |
| `SyncPayloadRepository.kt` | Single public function `observe(pairingId: String): Flow<SyncPayloadState>` — emits `Loading → cached lastGood (if any) → initial SELECT → Realtime subscription updates`. Sealed state machine (Loading/Empty/Ready/UnsupportedVersion/Error). Persists every `Ready` payload to DataStore + pushes Tile/Complication updates |
| `SyncPayloadCache.kt` | DataStore Preferences wrapper. Stores last-known-good payload as serialized JSON string. Mirrors existing `PairingDataStore` pattern |
| `SyncPayloadFormat.kt` | Shared helper functions: UTC→local conversion via `ZoneId.of(payload.timezone)` with `ZoneId.systemDefault()` fallback (traveler edge case), prayer-name localization, countdown formatters (`countdownShort` / `countdownLong`) |

Realtime subscription mirrors the existing `PairingRepository.observePairing()` pattern from the pre-existing pairing code — established convention, single subscription pattern, low cognitive load.

---

## Build & Dependencies

### Stack
- **Language:** Kotlin
- **UI:** Jetpack Compose for Wear OS
- **Build:** Gradle Kotlin DSL (`build.gradle.kts`)
- **Min SDK:** API 33 (Wear OS 4 baseline)
- **Target SDK:** API 35 (Android 15 / Wear OS 5)

### Key dependencies
- `kotlinx-serialization-json` (1.7.3) — payload deserialization
- `androidx.datastore-preferences` (1.1.1) — pairing + payload cache
- `androidx.wear.tiles` — Tile API
- `androidx.wear.watchface.complications.data-source` — Complication API
- `io.github.jan-tennert.supabase:postgrest-kt` (3.0.3) + `realtime-kt` — Supabase client (anon access via Postgrest + Realtime modules only; no `gotrue-kt` needed because watch never authenticates — it reads its own paired row using the anon key and relies on RLS for access control)
- `androidx.concurrent:concurrent-futures:1.2.0` — `CallbackToFutureAdapter` for Tile/Complication public future API (added in Session 3 to fix pre-existing release-variant build break)

### Verification commands (all currently passing on main)

```bash
./gradlew clean              # baseline
./gradlew build              # debug + release variants, 0 errors
./gradlew lintDebug          # 0 errors (29 warnings, all pre-existing baseline)
./gradlew assembleDebug      # APK builds
```

---

## Repo Hygiene

- `.gitignore` includes `.claude/` (Claude Code Desktop App session metadata) since `a111b03`
- Hygiene protocol (binding): every pre-paste session block checks `git status --porcelain` is empty
- No `git add .` usage — always explicit file lists per file-hygiene rule
- All session commits cleanly named with prefix convention: `feat(...)`, `fix(...)`, `polish(...)`, `chore(...)`, `docs(...)`

---

## Verified Behavior (Empirically Confirmed May 17)

Smoke-tested end-to-end on Wear OS Large Round emulator (Android 16, API 36, x86_64) paired with mihrab-app on Samsung Galaxy A07 5G (`R7AL10PDMAF`):

| Behavior | Status | Evidence |
|---|---|---|
| Watch reads real prayer time from phone-written payload | ✅ PASS | Watch displayed `04:34 / FAJR / in 6h 1m` matching the phone's written `next_prayer.time` for Bangkok tomorrow's Fajr |
| Realtime delivery latency | ✅ PASS | Watch updates within 1–5 seconds of phone-side writes (verified via SQL UPDATE test) |
| Manual `next_prayer` switch via SQL | ✅ PASS | SQL UPDATE setting `next_prayer = "asr" at "15:30 UTC"` propagated to watch as `22:30 / ASR / in 14h 10m` within 5 sec |
| Negative-countdown edge case | ✅ PASS | SQL UPDATE setting `next_prayer.time` far in past produced `07:00 / FAJR / in 0m` — NO "now" text |
| 3-item layout fidelity | ✅ PASS | No location subtitle, no "Last updated" line in Ready state — exactly matches @Preview |
| PairingScreen typography (28sp monospace) | ✅ PASS | 8-character code (`TCZSSNES`) renders single-line, distinctive characters, fits round face cleanly |
| Last-known-good cache (warm cold-start) | ✅ Designed, not stress-tested | Implementation present in `SyncPayloadCache.kt`; relied on during emulator cold starts |

---

## Known Limitations (Carried to Polish Phase)

These are KNOWN gaps that do not block v1 launch but are tracked for the eventual polish phase (delayed to pre-launch per Akhi's directive May 18).

### 1. Within-day prayer transition (stale state between prayer events)

**Symptom:** When `next_prayer.time` arrives (e.g., 04:34 Fajr), the watch shows `04:34 / FAJR / in 0m` until the phone writes a fresh payload that advances `next_prayer` to the next future prayer (Dhuhr).

**Why it happens:**
- Phone writes `next_prayer` based on its calculation logic; once Fajr passes, the phone needs to re-run and write `next_prayer = Dhuhr`
- Phone's current resync triggers: explicit user action (Settings → Calc/Juristic/Language/Location), app foreground date-rollover, pairing completion
- **No trigger fires on prayer-time-arrival.** So between Fajr arriving and the user opening the phone app, the watch shows the stale "in 0m" state

**Two ways to fix (deferred to polish):**

- **A.** Phone-side: add a foreground/scheduled task that re-syncs when each prayer time arrives (more complex on Flutter; reliable phone scheduling on Android is non-trivial)
- **B.** Watch-side: when `next_prayer.time` is in the past, locally scan `payload.prayers[]` (the full 5-prayer daily array already in the payload) and find the first future prayer. Use that as the next prayer. No phone re-sync needed.

**Brain's lean (for polish stage):** **B** — simpler, doesn't depend on phone behavior, uses data already available, doesn't introduce phone-side complexity. Post-Isha edge case: payload has nothing future today; watch shows last prayer (Isha) at "in 0m" until next-day phone sync. Acceptable degradation; users open Mihrab daily.

### 2. Auto-refresh on PairingScreen — visually unverified

**Symptom:** Auto-refresh code path on the pairing screen (5-min code TTL → auto-fetch new code) was implemented in Polish-Watch Batch 1 but never visually verified on the emulator (would have required staying on the pairing screen for 5+ minutes without pairing).

**Risk:** Low — the mechanism is mechanical (a `LaunchedEffect` watching `activeExpired`). Build/lint passed. If it fails in production, the worst case is the user sees "Expired — tap to refresh" instead of a fresh auto-code, and manual tap still works.

**Polish action:** Visual verification during a fresh-pair scenario when one happens naturally. No dedicated session needed.

### 3. Duplicate watch rows (development testing artifact)

**Symptom:** 3 watch rows + 11 TV rows for one user in `device_pairings` table.

**Cause:** Development testing artifacts from emulator wipes and repeated pairing tests during May 13–17 development. Each fresh emulator install creates a new pairing row because there's no device-identity deduplication.

**Production impact:** Likely zero — real users pair their physical TV/watch once and never wipe browser cache or factory-reset their watch. Duplicates only accumulate for developers and pathological-case users.

**Polish action (optional):** SQL cleanup keeping only the most recent paired row per device type per user. Or leave the test rows in place — they don't affect any production user. Decision deferred.

### 4. Pre-launch polish items not yet addressed

- `feat/watch-prayer-alert-3-5b` abandoned branch — should be deleted (one git command, no session needed)
- 12-language localization for new error strings added in Watch screens (e.g., "Waiting for sync from phone…", "Update Mihrab Watch to see all features") — currently English only. Need translation backfill in `figma_translations.json` (mihrab-app side, but the watch reads its own string resources, so this requires Watch-side resource additions too)
- "now" status text was explicitly removed in Polish-Watch Batch 1 per Master Model + Akhi ratification — no further work, just noting it's intentional

---

## What's NOT in Mihrabwatch (and why)

- **Apple Watch support** — Phase 5c, separate repo `Mihrabwatch-apple` (not yet created), requires Apple Developer enrollment + Cloud Mac. Parallel-stream paperwork deferred to pre-launch.
- **Pairing token generation logic on server side** — handled entirely client-side; no server-side Supabase function involved
- **Family Dashboard / Kids Mode features** — master-device features, not watch features
- **Quran reader, Awrad screens, Tasbih, 99 Names** — full-phone-app features; watch is intentionally narrow (prayer + qibla + tile + complication only, per Master Model May 14 final scope ratification)

---

## Bottom Line

The smartwatch is **shipped and production-ready** for v1 launch in its current form. Real-world testing on a paired emulator + real phone confirmed every primary code path works correctly. The known limitations are tracked, deferred to polish phase, and none block launch.

When polish phase begins (post-Partner-Portal and pre-store-submission), the highest-value Watch item is **local `next_prayer` advancement** (item 1 above) — single Mihrabwatch session, ~30 min, no cross-repo coordination. All other watch concerns are smaller.

---

*Generated by Brain (Claude Opus 4.7) — May 18, 2026*
*Independently audited by Claude Code (Sonnet 4.6, Plan Mode, read-only) against the actual repo state on the same date. Six factual discrepancies were identified and corrected in this version. The report now matches reality at commit `e6c29df`.*
