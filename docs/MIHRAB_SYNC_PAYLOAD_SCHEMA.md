# MIHRAB — `device_pairings.sync_payload` Schema (v1)
## The Data Contract Between Phone, Wear OS, Apple Watch, and TV

**Status:** RATIFIED — locked schema for Phase 5b Wear OS data sync (and forward-compatible for Apple Watch)
**Author:** Brain (Claude Opus 4.7)
**Date:** May 15, 2026
**Companion to:** `MIHRAB_MASTER_PLAN_ADDENDUM.md` Correction 5 (Watch is companion-only)

---

## 1. PURPOSE

The `device_pairings.sync_payload` JSONB column is the **single source of truth** for what the phone tells every paired device about prayer state, qibla direction, location, and configuration.

It is **written by the phone** (mihrab-app Flutter) and **read by all paired devices**:
- Mihrabwatch (Wear OS Kotlin)
- Mihrabwatch-apple (Apple Watch SwiftUI — future)
- Mihrabtv (React)

The payload is **passive** — paired devices never compute prayer times locally. They only render whatever the phone last pushed. This keeps device-side code minimal and ensures consistency across all paired surfaces.

---

## 2. ARCHITECTURE OVERVIEW

```
┌─────────────────────────────────────────────────────────────────────┐
│                                                                     │
│   PHONE (mihrab-app Flutter — master device)                        │
│   ├─ Calculates prayer times (Aladhan API)                          │
│   ├─ Calculates qibla bearing (local math)                          │
│   ├─ Resolves location display name                                 │
│   └─ Writes sync_payload to Supabase                                │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
                              │
                              ▼ writes JSONB
┌─────────────────────────────────────────────────────────────────────┐
│                                                                     │
│   SUPABASE — device_pairings table                                  │
│   ├─ One row per paired device                                      │
│   ├─ sync_payload column = JSONB                                    │
│   └─ Realtime channel emits on UPDATE                               │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
                              │
                              ▼ reads JSONB (poll + realtime)
        ┌─────────────────┬───────────────┬────────────────┐
        ▼                 ▼               ▼                ▼
   Wear OS Watch    Apple Watch        TV             (future)
   (Kotlin)         (SwiftUI)          (React)
   - Prayer Home    - Prayer Home      - Prayer screen
   - Qibla          - Qibla            - Pairing screen
   - Tile           - Complications
   - Complication
```

### When the phone WRITES sync_payload

| Trigger | Frequency | Reason |
|---|---|---|
| Pairing completion | Once per pair event | Bootstrap initial payload for new device |
| Daily rollover (00:00 local) | Once per day | New day = new prayer times |
| App resume after >1 hour | On-demand | Catch missed daily rollover |
| Location change | Per change | Prayer times + qibla change with location |
| Calculation method change | Per change | User reconfigures Asr juristic or method |
| Manual "Sync now" tap | Per tap | User-initiated refresh |

### When devices READ sync_payload

| Device | Pattern |
|---|---|
| Wear OS | Realtime subscription via Supabase channel + poll on app foreground |
| Apple Watch | WCSession.transferUserInfo from phone (preferred) + Supabase fallback |
| TV | Realtime subscription via Supabase channel |

---

## 3. THE SCHEMA (v1)

### Full example payload

```json
{
  "schema_version": 1,
  "last_updated": "2026-05-15T10:30:00Z",
  "date": "2026-05-15",
  "timezone": "Asia/Bangkok",
  "locale": "en",
  
  "location": {
    "display_name": "Bangkok, Thailand"
  },
  
  "calculation": {
    "method": "MWL",
    "juristic": "Shafi"
  },
  
  "prayers": [
    { "name": "fajr",    "time": "2026-05-14T22:42:00Z" },
    { "name": "sunrise", "time": "2026-05-14T23:58:00Z" },
    { "name": "dhuhr",   "time": "2026-05-15T05:30:00Z" },
    { "name": "asr",     "time": "2026-05-15T08:45:00Z" },
    { "name": "maghrib", "time": "2026-05-15T11:22:00Z" },
    { "name": "isha",    "time": "2026-05-15T12:35:00Z" }
  ],
  
  "next_prayer": {
    "name": "maghrib",
    "time": "2026-05-15T11:22:00Z"
  },
  
  "qibla": {
    "bearing_degrees": 294,
    "cardinal": "West-Northwest"
  }
}
```

Total size: ~700 bytes uncompressed. Well within JSONB practical limits.

---

## 4. FIELD-BY-FIELD SPEC

### Top-level fields

| Field | Type | Required | Description |
|---|---|---|---|
| `schema_version` | integer | YES | Locked at `1` for this version. Bumped on breaking changes. |
| `last_updated` | ISO 8601 string (UTC) | YES | When the phone wrote this payload. Used to detect staleness. |
| `date` | ISO date string | YES | The LOCAL date these prayer times apply to (e.g., `"2026-05-15"`). Devices compare to local date to detect "yesterday's data". |
| `timezone` | IANA timezone string | YES | The phone's current timezone (e.g., `"Asia/Bangkok"`). Devices use this to render UTC times in local form. |
| `locale` | ISO 639-1 code | YES | The phone's current locale (e.g., `"en"`, `"ar"`, `"ms"`). Reserved for future use — payload itself contains no localized strings. |

### `location` object

| Field | Type | Required | Description |
|---|---|---|---|
| `display_name` | string | YES | Human-readable location name (e.g., `"Bangkok, Thailand"`). For UX display only. |

**Privacy decision:** raw coordinates (lat/lng) are NOT included in the payload. Devices don't need them — qibla bearing is pre-computed. Reduces data exposure on paired devices, especially TVs which may be in semi-public locations.

### `calculation` object

| Field | Type | Required | Description |
|---|---|---|---|
| `method` | string | YES | Calculation method code: `"MWL"`, `"ISNA"`, `"Egyptian"`, `"Makkah"`, `"Karachi"`, etc. |
| `juristic` | string | YES | Asr juristic method: `"Shafi"` (standard) or `"Hanafi"` (later Asr). |

**Why include this?** Watch / TV may show a tiny "MWL · Shafi" label for transparency. No calculation happens on devices — this is metadata only.

### `prayers` array

Exactly **6 entries**, in this order: `fajr`, `sunrise`, `dhuhr`, `asr`, `maghrib`, `isha`. Order is locked so devices can index by position if needed.

Each entry:

| Field | Type | Required | Description |
|---|---|---|---|
| `name` | enum string | YES | One of: `"fajr"`, `"sunrise"`, `"dhuhr"`, `"asr"`, `"maghrib"`, `"isha"`. Locale-independent identifier. |
| `time` | ISO 8601 string (UTC) | YES | The exact prayer time in UTC. Devices convert to local using `timezone` field. |

**Why include `sunrise`?** It's not a prayer but Muslims often display it for context (forbidden prayer times around sunrise). Some watch users want it on the All Prayers list view.

**Why locale-independent prayer names?** Phone, Wear OS, Apple Watch, TV all have their own localized prayer-name strings. Sending `"fajr"` lets each device translate to its locale. Avoids coordination and reduces payload size.

### `next_prayer` object (pre-computed pointer)

| Field | Type | Required | Description |
|---|---|---|---|
| `name` | enum string | YES | Same enum as in `prayers[].name`. The next upcoming prayer. |
| `time` | ISO 8601 string (UTC) | YES | The next prayer's UTC time. Devices use this for countdown math. |

**Why pre-compute this?** Devices shouldn't iterate through `prayers[]` to find "the next one whose time is after now". Phone is authoritative — it figures out next prayer once and pushes the answer. Devices just render.

**Edge cases the phone handles:**
- If current time is after Isha → `next_prayer` points to TOMORROW's Fajr (payload also includes tomorrow's Fajr time, OR the phone writes a new payload at midnight).
- If a prayer is currently in progress → `next_prayer` is the prayer AFTER the current one.

### `qibla` object

| Field | Type | Required | Description |
|---|---|---|---|
| `bearing_degrees` | integer (0–359) | YES | Compass bearing to Mecca from the user's location. Computed phone-side from user lat/lng + Mecca (21.4225°N, 39.8262°E). |
| `cardinal` | string | YES | Human-readable cardinal direction (e.g., `"West-Northwest"`, `"East"`). Pre-computed for display. |

**Why pre-compute the cardinal?** Mapping degrees → cardinal name (16-point) is a lookup table. Phone does it once; devices avoid duplicating the table.

---

## 5. VERSIONING POLICY

### Forward compatibility

Devices MUST check `schema_version` on read:

```pseudo
if payload.schema_version > SUPPORTED_VERSION:
    show "Update your phone app to see the latest prayer info"
    fall back to showing whatever fields the device DOES understand
```

### Additive changes (NO version bump)

These do NOT bump `schema_version`:
- New optional fields added to existing objects
- New optional top-level fields
- New optional elements in `prayers[]` (e.g., adding `imsak` someday)

### Breaking changes (version bump)

These DO bump `schema_version`:
- Renaming or removing existing fields
- Changing field types
- Changing the order or required entries in `prayers[]`
- Changing the meaning of an existing enum value

### Migration discipline

- v1 → v2: phone writes payloads in BOTH v1 and v2 format for 2 release cycles
- This gives old watch apps time to update via App Store / Play Store
- After 2 cycles, drop v1 writes; old apps see "Update required" message

---

## 6. WRITE PATH (PHONE-SIDE RESPONSIBILITY)

The phone-side mihrab-app Flutter code is responsible for:

### A. When to write

The phone writes the payload to `device_pairings.sync_payload` for ALL of the user's paired devices on:

1. **Pairing completion** — when a new device pairs, immediately bootstrap with current state
2. **Daily rollover** — at 00:00 local time (or on app foreground after a day boundary)
3. **Location change** — when user updates their location in settings
4. **Calculation method change** — when user changes method or juristic method
5. **App foreground after >1h** — catch missed rollovers
6. **Manual sync** — if a future "Sync now" UI is added

### B. SQL pattern (writes to ALL paired devices for user)

```sql
UPDATE device_pairings
SET sync_payload = $1::jsonb,
    updated_at = NOW()
WHERE user_id = $2;
```

This single UPDATE pushes the same payload to all paired devices (Wear OS, Apple Watch, TV). Each device's Realtime subscription fires automatically.

### C. Payload construction (Flutter pseudo-code)

```dart
Map<String, dynamic> buildSyncPayload({
  required UserLocation location,
  required CalculationMethod method,
  required JuristicMethod juristic,
  required Locale locale,
}) {
  final times = PrayerTimesService.calculateForToday(
    location: location,
    method: method,
    juristic: juristic,
  );
  
  final nextPrayer = times.next(DateTime.now().toUtc());
  final qiblaBearing = QiblaCalculator.bearing(
    fromLat: location.lat,
    fromLng: location.lng,
  );
  
  return {
    'schema_version': 1,
    'last_updated': DateTime.now().toUtc().toIso8601String(),
    'date': DateTime.now().toLocal().toIso8601String().substring(0, 10),
    'timezone': DateTime.now().timeZoneName, // e.g., "Asia/Bangkok"
    'locale': locale.languageCode,
    'location': {
      'display_name': location.displayName,
    },
    'calculation': {
      'method': method.code,
      'juristic': juristic.code,
    },
    'prayers': times.toList().map((p) => {
      'name': p.id,
      'time': p.utcTime.toIso8601String(),
    }).toList(),
    'next_prayer': {
      'name': nextPrayer.id,
      'time': nextPrayer.utcTime.toIso8601String(),
    },
    'qibla': {
      'bearing_degrees': qiblaBearing.degrees.round(),
      'cardinal': qiblaBearing.cardinalName,
    },
  };
}
```

---

## 7. READ PATH (WATCH-SIDE RESPONSIBILITY)

### Wear OS (Kotlin Compose) — replaces hardcoded `NextPrayerProvider`

```kotlin
@Serializable
data class SyncPayload(
    val schema_version: Int,
    val last_updated: String,
    val date: String,
    val timezone: String,
    val locale: String,
    val location: Location,
    val calculation: Calculation,
    val prayers: List<Prayer>,
    val next_prayer: NextPrayer,
    val qibla: Qibla,
)

class SyncPayloadRepository(private val supabase: SupabaseClient) {
    fun observe(pairingId: String): Flow<SyncPayload?> = flow {
        // Initial read
        val initial = supabase.from("device_pairings")
            .select("sync_payload")
            .eq("id", pairingId)
            .single()
            .decodeSingle<SyncPayloadWrapper>()
        emit(initial.sync_payload)
        
        // Subscribe to realtime updates
        supabase.channel("device_pairings:id=eq.$pairingId")
            .postgresChange<SyncPayload>(Event.UPDATE) { payload ->
                emit(payload.new.sync_payload)
            }
    }
}
```

The watch then consumes the payload in:
- `PrayerHomeScreen` — reads `next_prayer.name`, `next_prayer.time`, computes countdown locally
- `QiblaCompassScreen` — reads `qibla.bearing_degrees` (uses device compass for direction, payload for target)
- `NextPrayerProvider` (used by Tile + Complication) — reads `next_prayer.name`, `next_prayer.time`, formats short/long countdown

### Apple Watch (SwiftUI) — same shape, different ingestion

```swift
struct SyncPayload: Codable {
    let schema_version: Int
    let last_updated: String
    let date: String
    let timezone: String
    let locale: String
    let location: SyncLocation
    let calculation: SyncCalculation
    let prayers: [SyncPrayer]
    let next_prayer: SyncNextPrayer
    let qibla: SyncQibla
}

// Apple Watch receives via WCSession.transferUserInfo() from iPhone
// (faster than Supabase polling and works offline once paired)
```

### TV (React) — already exists, will be updated

```typescript
interface SyncPayload {
  schema_version: number;
  last_updated: string;
  date: string;
  timezone: string;
  locale: string;
  location: { display_name: string };
  calculation: { method: string; juristic: string };
  prayers: Array<{ name: string; time: string }>;
  next_prayer: { name: string; time: string };
  qibla: { bearing_degrees: number; cardinal: string };
}
```

---

## 8. EDGE CASES

### A. No payload yet (NULL)

- Watch shows "Waiting for sync from phone..." with a subtle pulse animation
- Tile and complication fall back to "—:—" placeholder
- After 30 seconds without payload, show "Pair your phone with Mihrab to see prayer times"

### B. Stale payload (date mismatch)

- Compare `payload.date` to device's local date
- If `payload.date < today`: show with a "Last updated [time]" indicator, no error
- If `payload.date < yesterday`: show "Open Mihrab on your phone to refresh"

### C. Schema version mismatch

- If `payload.schema_version > device_supported_version`: show "Update Mihrab Watch to see all features"
- Continue rendering whatever fields ARE understood (graceful degradation)
- If `payload.schema_version < device_supported_version`: render normally (devices support old payloads forever)

### D. Missing fields (corruption / partial payload)

- Each device implements defensive deserialization
- Missing `next_prayer` → use first future prayer from `prayers[]`
- Missing `qibla` → hide Qibla screen, show "Sync your phone" on its tab
- Missing `prayers[]` → fall back to empty list with placeholder UI

### E. Time zone mismatch (user traveled)

- Watch's local timezone differs from `payload.timezone`
- Watch renders UTC times using ITS OWN local timezone
- Display a subtle banner: "Prayer times calculated for [payload location]. Open phone to refresh."
- After phone-side refresh, payload comes through with updated location + qibla

---

## 9. LOCKED DECISIONS

These decisions are **ratified** and should not be re-litigated in future sessions without strong cause:

| # | Decision | Rationale |
|---|---|---|
| 1 | All timestamps stored as UTC ISO 8601 | Universal, timezone-safe, parsable everywhere |
| 2 | Separate `timezone` field instead of zoned timestamps | Smaller payload, easier parsing, explicit user TZ |
| 3 | Pre-computed `next_prayer` pointer | Devices don't compute prayer logic — phone is authoritative |
| 4 | Pre-computed qibla `cardinal` name | Avoid 16-point lookup table on every device |
| 5 | Locale-independent prayer name enums | Each device localizes its own strings |
| 6 | NO raw coordinates in payload | Privacy + devices don't need them |
| 7 | Same payload for all device types | Simpler — each device picks what it needs |
| 8 | JSONB column (not separate columns) | Schema evolution without migrations |
| 9 | `schema_version` field for forward compat | Allows breaking changes with discipline |
| 10 | Phone writes to ALL paired devices in single UPDATE | One operation, Realtime fans out automatically |
| 11 | Watch never modifies payload | Read-only, no write-back from devices |
| 12 | Sunrise included in `prayers[]` | Useful for context (forbidden times), some users want on watch list |

---

## 10. DEVICE-SPECIFIC USAGE NOTES

### Wear OS — minimum fields used (Phase 5b)

- `next_prayer.name` + `next_prayer.time` → Prayer Home countdown ring + Tile + Complication
- `qibla.bearing_degrees` → Qibla Compass arrow rotation target
- `location.display_name` → Optional caption on Prayer Home

Other fields (`prayers[]`, `calculation`, `locale`, etc.) are present but unused in Phase 5b. Reserved for future watch screens.

### Apple Watch — fields used (Phase 5c)

Same as Wear OS. Apple Watch reads via WCSession primarily; Supabase Realtime is fallback.

### TV — fields used (existing, will be updated)

- All `prayers[]` for the full schedule view
- `next_prayer` for the hero card countdown
- `location.display_name` for the header
- `calculation` for the metadata label

---

## 11. WHAT THIS DOC DOESN'T COVER (deferred)

- **Authentication for Realtime channel** — handled by Supabase RLS (each device has a `pairing_token` JWT)
- **Conflict resolution** — phone is sole writer, no conflicts possible (single-writer, multi-reader)
- **Payload compression** — current size (~700 bytes) doesn't warrant it; revisit if it grows
- **Offline payload caching on devices** — each device caches last-known-good payload in DataStore / UserDefaults / localStorage
- **`device_specific_payload` column** — not added yet; revisit if device-specific data emerges (e.g., complication preferences)

---

## 12. IMMEDIATE NEXT ACTIONS

**Session 2 (Phone-side write — Claude Code, mihrab-app):**
- Wire `SyncPayloadService` that constructs the payload per spec
- Hook into the 6 triggers (pairing, daily, location, calc, app resume, manual)
- Add unit tests for payload construction
- Verify Supabase RLS allows the phone to UPDATE its own paired devices

**Session 3 (Watch-side read — Claude Code, Mihrabwatch):**
- Replace hardcoded `NextPrayerProvider.current()` with `SyncPayloadRepository.observe()`
- Wire `PrayerHomeScreen` to read from the repository as a Flow
- Wire `MihrabTileService` and `MihrabComplicationDataSourceService` to read latest payload
- Wire `QiblaCompassRepository` to read target bearing from payload (replaces hardcoded Bangkok 294°)
- Handle all 5 edge cases from Section 8

**Session 4 (E2E smoke test):**
- Phone app generates payload → Supabase update → Wear OS receives → Prayer Home shows real Maghrib time
- Verify across location change, daily rollover, manual sync

---

## DOCUMENT INFO

| Field | Value |
|---|---|
| Created | May 15, 2026 |
| Author | Brain (Claude Opus 4.7) |
| Status | RATIFIED — locked schema v1 |
| For | Phase 5b Wear OS data sync + Phase 5c Apple Watch + TV updates |
| Companion | `MIHRAB_MASTER_PLAN_ADDENDUM.md` (watch companion-only), `MIHRAB_WATCH_DESIGN_GUIDE.md` (visual spec), `MASTER_MODEL_APPLE_WATCH_BRIEF.md` (Apple Watch architecture) |
| Next | Session 2 prompt — phone-side write implementation |
