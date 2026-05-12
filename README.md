# Mihrab Watch

Wear OS companion for the Mihrab Islamic worship app.

## Tech stack

- **Kotlin** 2.2.20
- **Android Gradle Plugin** 9.2.1
- **Compose for Wear OS** 1.6.1 (Material 3)
- **Horologist** 0.6.15 (Google's Wear Compose helpers)
- **ProtoLayout** 1.4.0 (Tile rendering)
- **Watchface Complications** 1.3.0
- Min SDK 33 · Target SDK 35 · JVM 17

## Role

Mihrab Watch is a **read-only mirror** of phone-side state, synced via Supabase
`device_pairings.sync_payload` (wiring lands in Session 3). The Wear OS app
exists separately from the Flutter phone app because Wear OS Tiles and
Complications are Kotlin-only APIs.

Standalone manifest: the watch boots offline using device compass and the
last-synced data. Phone enriches but is not required.

## Build

```powershell
./gradlew assembleDebug
```

Produces `app/build/outputs/apk/debug/app-debug.apk`.

## Run

Open the project in Android Studio (Iguana or newer), connect a Wear OS
emulator (API 33, Large Round 454×454 recommended) or a paired physical watch,
and click ▶.

## Status

**Phase 5 of the Mihrab roadmap.**

Session 1 complete — scaffold only. Foundation in place for:

- Session 2: Aladhan prayer times.
- Session 3: Supabase `device_pairings` sync + QR pairing flow.
- Session 4: Notification UI + prayer alert handling.
- Session 5: Magnetometer-driven Qibla compass.
- Session 6: Real Tile + Complication rendering.

## Design

Canonical visual spec: [`docs/MIHRAB_WATCH_DESIGN_GUIDE.md`](docs/MIHRAB_WATCH_DESIGN_GUIDE.md)

Key design commandments:

- Pure black `#000000` background (OLED battery optimization).
- Dark mode only — no light theme variant.
- Gold accent `#D4A537` for brand identity and active states.
- Sky-based prayer color palette (Fajr blue, Dhuhr noon yellow, Maghrib coral, etc.).
- Typography floor: 13sp. Never smaller.
- 3-second glanceability rule: complete value in ≤3 seconds.

## License

Copyright © 2026 Apexiom Ltd, UK. All rights reserved. Proprietary and
confidential — see [`LICENSE`](LICENSE).
