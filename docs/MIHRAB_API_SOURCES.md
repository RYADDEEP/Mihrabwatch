# MIHRAB — API SOURCES OF TRUTH
## LOCKED — Do NOT change without Ryad's explicit approval
## Last Verified: May 2, 2026

---

## PURPOSE

This file exists because incorrect API assumptions caused real damage during development. Every model, every session, every Claude Code prompt MUST reference this file before making ANY decision about external APIs — especially the Quran.

**Rule: If you're about to write a URL, an API endpoint, or a translation ID — CHECK THIS FILE FIRST.**
**Rule: NEVER assume an API source. VERIFY against this file.**
**Rule: The Quran is the word of Allah. No assumptions, no shortcuts, no improvisation.**

---

## QURAN ARABIC TEXT — SHIPPED LOCALLY (PRIMARY)

```
SOURCE: Tanzil Project — Uthmani, version 1.1 (tanzil.net)
ASSET:  assets/tanzil/quran-uthmani.xml
STATUS: VERIFIED (August 2026) — shipped VERBATIM, byte-identical to download
SHA-256: 8c5aeae20363a98f6963720d29fce040ca8b56a8e75f8b564c257fce7f6d0417
READ BY: lib/features/quran/data/quran_local_source.dart
USED BY: Mobile app (Flutter) — reader, audio screen, Kids Mode, surah browser

⭐ THIS IS THE PRIMARY SOURCE OF QURAN ARABIC. It serves ALWAYS — online and
   offline both. The Quran.com API below is never asked for Arabic, and its
   `text_uthmani` is never displayed while this asset is available.

CONTENTS: 114 suras, 6,236 ayahs, pause marks, sajdah signs, tatweel below
          superscript alefs. No rub-el-hizb, no sequential tanweens.

NUMBERING: matches AyahCalculator and Quran.com exactly (Hafs/Kufi). Verified
           for all 114 suras. Bookmarks, positions and audio URLs are safe.

LICENCE: Creative Commons Attribution 3.0. Commercial use permitted.
⛔ CONDITION OF USE: the source (Tanzil Project) must be clearly indicated and
   a link made to tanzil.net. This is implemented in About via the i18n keys
   `AboutUs_Sources_Title` and `AboutUs_Quran_Source` (all 12 locales).
   Removing it removes our right to ship the text.

⛔ THE TEXT IS NEVER MODIFIED. Proof runs on every `flutter test`:
   test/quran_integrity_test.dart — file digest, extracted-text digest,
   codepoint inventory, and mark counts. Regenerate constants ONLY when
   deliberately adopting a new Tanzil release: tool/quran_digest.py.

⛔ TANZIL TRANSLATIONS ARE NOT SHIPPED. tanzil.net/trans states they are "for
   non-commercial purposes only". Mihrab is a paid app, so translations and
   tafsir stay on the APIs below. Do not bundle them without written
   permission from Tanzil.
```

---

## QURAN TEXT API — FIRE EXTINGUISHER + TRANSLATIONS

```
SOURCE: Quran.com API v4
BASE URL: https://api.quran.com/api/v4
STATUS: VERIFIED (May 2026)
USED BY: Mobile app (Flutter), TV app (React)

ENDPOINTS:
├── GET /chapters → All 114 surahs with metadata
├── GET /quran/verses/uthmani?chapter_number=N → Arabic text (Uthmani script)
├── GET /quran/translations/{ID}?chapter_number=N → Translation by ID
├── GET /quran/translations/57?chapter_number=N → Transliteration (English)

TRANSLATION ID: 20 (Saheeh International English) — DEFAULT
TRANSLITERATION ID: 57 — CONSTANT (only English transliteration available)

⚠️ TWO ROLES, BOTH STILL LIVE:
   1. TRANSLATION + TRANSLITERATION — the everyday role. We do not ship these
      (see the licence note above), so this is where they come from. If the
      call fails the reader shows Arabic only and NO error is surfaced.
   2. FIRE EXTINGUISHER for Arabic — used only if the shipped Mushaf is
      missing, corrupt, or fails its structural checks. Never removed, never
      disabled, and hopefully never used.

⚠️ LOCKED — do NOT change without Ryad's approval
```

---

## QURAN TRANSLATION IDS (VERIFIED against api.quran.com/api/v4/resources/translations)

Mihrab supports 12 languages. Arabic needs no translation (it IS the Quran).

```
VERIFIED TRANSLATION IDS (May 2, 2026):
├── EN (English):    ID 20  — Saheeh International
├── ID (Indonesian): ID 33  — Indonesian Islamic Affairs Ministry (Kemenag)
├── UR (Urdu):       ID 97  — Tafheem e Qur'an (Maududi)
├── TR (Turkish):    ID 77  — Diyanet
├── FR (French):     ID 31  — Muhammad Hamidullah
├── RU (Russian):    ID 45  — Elmir Kuliev
├── FA (Persian):    ID 29  — Hussein Taji Kal Dari
├── HI (Hindi):      ID 122 — Maulana Azizul Haque al-Umari
├── MS (Malay):      ID 39  — Abdullah Muhammad Basmeih
├── SW (Swahili):    ID 49  — Ali Muhsin Al-Barwani
├── BN (Bengali):    ID 161 — Taisirul Quran (Tawheed Publication)
├── AR (Arabic):     N/A    — No translation needed

⚠️ THESE IDS ARE VERIFIED. Do NOT guess. Do NOT change without re-verifying.
⚠️ Previous wrong IDs caused Russian text to appear for French, German for Hindi, etc.
```

---

## QURAN AUDIO CDN

```
SOURCE: islamic.network CDN
URL PATTERN: https://cdn.islamic.network/quran/audio/{bitrate}/{identifier}/{globalAyahNumber}.mp3
STATUS: VERIFIED (May 2026)
USED BY: Mobile app (Flutter), TV app (React)

GLOBAL AYAH NUMBERS: 1-6236
├── Surah 1 (Al-Fatihah): ayahs 1-7
├── Surah 2 (Al-Baqarah): ayahs 8-293
├── ... (cumulative)
├── Surah 114 (An-Nas): ayahs 6231-6236

CONVERSION: globalAyahNumber = sum(verses_count of surahs 1 to N-1) + ayahNumber

⚠️ NEVER use everyayah.com — that was incorrectly used in an earlier session
⚠️ LOCKED — do NOT change CDN without Ryad's approval
```

---

## QURAN RECITERS (18 — VERIFIED against islamic.network CDN, May 2026)

```
MURATTAL:
├── ar.alafasy          — Mishary Rashid Alafasy        — 128 kbps
├── ar.husary           — Mahmoud Khalil Al-Husary      — 128 kbps
├── ar.minshawi         — Mohamed Siddiq Al-Minshawi    — 128 kbps
├── ar.mahermuaiqly     — Maher Al-Muaiqly              — 128 kbps
├── ar.hudhaify         — Ali Al-Hudhaify               — 128 kbps
├── ar.muhammadayyoub   — Muhammad Ayyoub               — 128 kbps
├── ar.muhammadjibreel  — Muhammad Jibreel              — 128 kbps
├── ar.ahmedajamy       — Ahmad Al-Ajmi                 — 128 kbps
├── ar.abdullahbasfar   — Abdullah Basfar               — 64 kbps
├── ar.hanirifai        — Hani Ar-Rifai                 — 64 kbps

MUJAWWAD:
├── ar.husarymujawwad       — Al-Husary (Mujawwad)          — 128 kbps
├── ar.abdulbasitmurattal   — Abdul Basit (Murattal)        — 64 kbps
├── ar.abdulsamad           — Abdul Basit Abd us-Samad       — 64 kbps
├── ar.minshawimujawwad     — Al-Minshawi (Mujawwad)        — 64 kbps

CLASSIC / HARAMAIN:
├── ar.abdurrahmaansudais   — Abdul Rahman Al-Sudais        — 64 kbps
├── ar.saoodshuraym         — Saud Al-Shuraim               — 64 kbps
├── ar.shaatree             — Abu Bakr Al-Shatri            — 128 kbps
├── ar.aymanswoaid          — Ayman Sowaid                  — 64 kbps

SOURCE FILES:
├── Mobile: lib/features/quran/services/quran_reciters_service.dart
├── Mobile: lib/features/quran/models/quran_reciter.dart
├── TV: src/data/reciters.ts

⚠️ Bitrate varies per reciter — NEVER assume 128 for all
⚠️ LOCKED — do NOT add/remove reciters without verifying against CDN
```

---

## PRAYER TIMES API

```
SOURCE: Aladhan API
BASE URL: https://api.aladhan.com/v1
STATUS: VERIFIED (April 2026)
USED BY: Mobile app (Flutter — via prayer providers), TV app (React — direct)

ENDPOINT: GET /timings/{DD-MM-YYYY}?latitude={lat}&longitude={lng}&method={method}&school={school}

CALCULATION METHODS:
├── 0: Shia Ithna-Ashari
├── 1: University of Islamic Sciences, Karachi
├── 2: Islamic Society of North America (ISNA)
├── 3: Muslim World League (MWL) ← DEFAULT
├── 4: Umm Al-Qura University, Makkah
├── 5: Egyptian General Authority of Survey
├── 7: Institute of Geophysics, University of Tehran
├── 8: Gulf Region
├── 9: Kuwait
├── 10: Qatar
├── 11: Majlis Ugama Islam Singapura
├── 12: UOIF (France)
├── 13: JAKIM (Malaysia)
├── 14: Tunisia
├── 15: Turkey (Diyanet)

JURISTIC SCHOOLS:
├── 0: Shafi'i ← DEFAULT
├── 1: Hanafi

RESPONSE INCLUDES:
├── timings: Fajr, Sunrise, Dhuhr, Asr, Maghrib, Isha
├── date.gregorian: day, weekday, month, year
├── date.hijri: day, month (en/ar), year

⚠️ LOCKED — do NOT change API source
```

---

## SUPABASE BACKEND

```
PROJECT: mihrab
URL: https://aegbwuevbvtkicycfhrq.supabase.co
ANON KEY: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImFlZ2J3dWV2YnZ0a2ljeWNmaHJxIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzU2MDA2NDMsImV4cCI6MjA5MTE3NjY0M30.VtG__gnRkUAF4gI0EWnjlerMzOSH7sA193TDND1VOE8
STATUS: LIVE, HEALTHY

TABLES: 20+
FUNCTIONS: 7+
TRIGGERS: 3+
RLS POLICIES: 30+
EDGE FUNCTIONS: 2 (send-desktop-access-email, send-push)

KEY TABLES:
├── entitlements — tier column (NOT type): 'individual_monthly', 'individual_yearly', 'family_lifetime'
│     ⚠️ tier values reflect the v1.1 model — the commercial model is under revision per the regional pricing canon
├── tv_pairings — includes sync_payload JSONB column
├── devices — unique constraint on (user_id, device_identifier)
├── prayer_completions — tracks prayer status per user
├── family_accounts, family_members — family system

CRITICAL BUGS FIXED:
├── entitlements uses 'tier' column, NOT 'type' (commit 977cf81)
├── .isFilter('removed_at', null) for supabase_flutter 2.8.5 (NOT .is_())
├── Recursive RLS on family_members fixed with separate policies
```

---

## EMAIL SERVICE

```
SERVICE: Resend
API KEY: stored as the Supabase secret RESEND_API_KEY (never committed) — rotate at resend.com
SENDING DOMAIN: mihrab.faith (VERIFIED)
FROM ADDRESS: Mihrab <noreply@mihrab.faith>
SUPABASE EDGE FUNCTION: send-desktop-access-email

DNS RECORDS (Namecheap):
├── DKIM: TXT — resend._domainkey → verified ✅
├── SPF: TXT — send → v=spf1 ... ~all ✅
├── MX: send → feedback-smtp...amazonses.com ✅
├── DMARC: TXT — _dmarc → v=DMARC1; p=quarantine; adkim=s; aspf=s; ✅
```

---

## DEPLOYMENT

```
PLATFORMS:
├── Portal:   portal.mihrab.faith  (Vercel — Mihrabportal repo)
├── Web App:  app.mihrab.faith     (Vercel — mihrab-app/build/web)
├── TV App:   tv.mihrab.faith      (Vercel — Mihrabtv repo)
├── Mobile:   Samsung Galaxy A07 5G + Chrome
├── Legal:    ryaddeep.github.io/mihrab-legal

VERCEL ACCOUNT: ryaddeep's projects (Hobby plan)
DOMAIN: mihrab.faith (Namecheap, $13.18)

SUPABASE REDIRECT URLS:
├── http://localhost:*
├── https://portal.mihrab.faith
├── https://app.mihrab.faith
├── https://tv.mihrab.faith
```

---

## RULES FOR ALL MODELS

1. **NEVER assume an API source** — verify against this file
2. **NEVER change a LOCKED value** without Ryad's explicit approval
3. **NEVER guess a translation ID** — use the verified table above
4. **NEVER use everyayah.com** — the correct CDN is cdn.islamic.network
5. **NEVER confuse `type` with `tier`** on the entitlements table
6. **The Quran demands accuracy** — verify everything, assume nothing
