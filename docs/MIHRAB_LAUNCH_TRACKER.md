# MIHRAB — LAUNCH TRACKER

**Updated August 2026 · supersedes the July 2026 tracker entirely.**
**This tracker answers "what is LEFT." For "what EXISTS," read `MIHRAB_ECOSYSTEM_STATE.md` first — they are complements, not duplicates.**

Status tracker, not a re-plan. For *facts* defer to the canonical docs.

---

## ⛔ THE CRITICAL PATH HAS CHANGED

**The old path was: Shop live → IAP → submission. It is out of date.**

```
⭐ THE SHOP IS LIVE. IAP IS WIRED — the app has sent the Supabase
  user id since 30 July, and the hook's warning comment is two days
  OLDER than its own fix.

⛔ WHAT ACTUALLY STANDS BETWEEN MIHRAB AND A STORE IS PAPERWORK
   NOBODY HAS STARTED.
```

**The real path:**

```
The launch lane  →  submission
       │
       └─ and one build arc that does NOT block it
```

---

## REPO STATE — verify with `git log --oneline -1`

| Repo | HEAD | Note |
|---|---|---|
| `mihrab-app` | **`165bff2`** | **NOT auto-deploy** — `flutter build web --release; cd build\web; vercel --prod` |
| `Mihrabportal` | **`b0864db`** | **Auto-deploys from `main` — merging IS deploying** |
| `Mihrabtv` | `28a4c6a` | four store packages built |
| `Mihrabwatch` | `b1b19f4` | `.aab` 13.23 MB, signed |
| Supabase | `aegbwuevbvtkicycfhrq` | migrations by hand, always |

**⚠️ `feat/markaz-clean` (`30ad4f8`) is ABANDONED, deliberately.** Superseded by the Pattern; kept on the remote for its measurements.

---

## ⛔ 1. THE LAUNCH LANE — nothing here has been started

**This is the whole remainder. None of it is code.**

```
├── ⛔ 14 DECLARATIONS in Play Console → App content
│   ⭐ FOUR NEED A MANIFEST AUDIT FIRST: location · foreground
│     service · full-screen intent · exact alarm.
│     THESE ARE LEGALLY BINDING STATEMENTS. Do not answer them
│     from memory.
│
├── ⛔ THE STORE LISTING — from nothing. Title, short description,
│   full description, in 12 languages.
│
├── ⛔ THE FEATURE GRAPHIC — 1024×500. Does not exist. Must be made.
│
├── ⛔ SCREENSHOTS — recapture. The app changed beyond recognition
│   this rotation.
│   ⚠️ AND THE OLD SET SHOWS AN "Advertisement" BAR, which
│     contradicts the no-ads declaration. That contradiction must
│     not reach a reviewer.
│
├── LG's 400×400 icon
└── AMAZON — a name mismatch, twice. Needs their support.
```

**⭐ Reviewer access already exists: `review@apexiomltd.com`, granted to 2031.**

---

## ⚠️ 2. VERIFY BEFORE PRODUCTION — two things, both cheap

```
⭐ THE RTDN TOPIC IN PLAY CONSOLE. Four searches in the
  convert-hook logs settle it:

     entitlement.written        a real purchase landed
     verify.unresolvable_user   the refusal fired
     gate.google_unconfigured   env vars unset
     gate.google_admitted       ZERO HITS means the topic was
                                never wired

  ⚠️ NOT ANSWERABLE UNTIL SOMEBODY CAN ACTUALLY BUY. The app is in
    internal testing.

⭐ AND: DOES A PILGRIM'S FAMILY SEE HIS UMRAH?
  Six people hold entitlements through him and have no journey
  membership. The reading says they see nothing — no group, no
  itinerary, no geofence, no arrival takeover.
  ⛔ NOBODY HAS MEASURED IT.
```

---

## ⚠️ 3. CODE ITEMS — small, and none blocks submission

```
├── ⛔ THE PWA GATE — "Web access is available exclusively for
│   Family Gift Package members." A product removed for legal
│   reasons, and it SURVIVED A FRESH DEPLOY.
│   ⭐ PARKED DELIBERATELY: "the PWA is only the restricted shadow
│     of the native." Password resets now land there.
│
├── ⚠️ flutter_native_splash FAILS THE RELEASE BUILD. `flutter
│   clean` fixes it and it recurs.
│
├── ⚠️ THE GROUP-SIZE OVER-CAP — an agency lowering `group_size`
│   below the joined count is accepted SILENTLY. The client guard
│   compares arrays that are always empty on edit, so it is a no-op
│   on exactly the path where it matters. Result: "12 / 5 members",
│   zero seats, every add fails, no escape.
│
├── The `mihrab.app` addresses in the app's own legal pages
├── Name capitalisation at sign-up
└── The `localhost:*` entries in the Supabase redirect allowlist
```

---

## ⭐ 4. THE BUILD ARC — and it does NOT block launch

**The Abstract Pattern is complete. What follows is the six shapes, then the app.**

```
STAGE ONE — the shapes
  1. ⭐ THE DATA LIST          serves six verticals
  2. ⭐ THE APP'S CATEGORIES   serves every religious type
  3. the scheduled reminder    four
  4. the self-recorded track   two
  5. willingness by duration   markaz

STAGE TWO — the app becomes the portal
  6. ⛔ DOES THE APP KNOW ITS USER IS A PARTNER?  — blocks 7-9
  7. the partner section       worship first, business below
  8. the partner writes        ⚠️ the photo is the real weight
  9. the customer reads        native, not a WebView

⭐ SEE MIHRAB_MASTER_PLAN_PATTERN AND THE IMPLEMENTATION PLAN.
```

**⚠️ AND THERE ARE NO SESSION COUNTS HERE, DELIBERATELY.** Two estimates were wrong this month — one said two sessions and became one; another said four and produced work that was thrown away. **Both were made before anyone read the code.**

---

## ✅ 5. DONE THIS ROTATION

```
⭐ THE ABSTRACT PATTERN — four sessions. A partner type is a row;
  a feature is a value; a label is the owner's word; and the owner
  has a workshop.

⭐ THE APP STOPPED DEPENDING ON ANYONE
  ├── the Quran ships in the APK, byte-verified, 16 integrity tests
  ├── prayer times computed on the phone
  │   ⛔ AND 47.3% OF PRAYER BEGINNINGS WERE BEING DISPLAYED BEFORE
  │      THE PRAYER ENTERED. Fixed by construction.
  └── the Mushaf reading mode, offline, as the default

⭐ ACCOUNT DELETION — the screen the legal pages promised twice and
  that did not exist

⭐ THE MAIL — Contact Us sends, DNS on Cloudflare, branded auth
  emails, and the app's password reset finally has a destination

⭐ EVERY COMMISSION owner-set, forward-only by trigger
⭐ THE INFLUENCER VERTICAL and the shop's link protection
⭐ THE TRIAL WATCH WINDOW — an agency keeps sight of pilgrims for
  30 days after his trial ends
⭐ THE UMRAH AGENCY AUDITED — 18 of 21 files byte-unchanged
```

---

## 📦 6. PARKED — deliberate, with reasons

```
├── AUDIO TO R2 — the CDN is live at quran.mihrab.faith.
│   ⚠️ EveryAyah's licence is UNREAD. Two sources already
│     disqualified on exactly that ground.
│
├── TRANSLATIONS OFFLINE — permitted, via Quran Foundation's
│   Content Sync API. ⛔ Needs the authenticated endpoint and a
│   backend proxy first. tanzil's translations are NON-COMMERCIAL.
│
├── THE KFGQPC PAGE FONTS — three measured blockers: no licence
│   grant, no subsetting permitted (59-208 MB), and glyph data that
│   carries no line breaks.
│
├── APPLE — Codemagic CI removes the Mac from the critical path.
│   Blocked on the £79 and an Apple ID region mismatch.
│
├── THE MADRASAH ENGINE — the Auto Tajweed MM's work.
│   ⭐ ONE THING MUST BE AGREED BETWEEN SEATS FIRST: the shape of an
│     assessment record. The assessment IS the profile.
│
├── THREE UNDECIDED SHAPES — a one-to-one assignment (tariqah), a
│   group inside a group (scouts), a guardian (scouts).
│
└── TAKABALA ALLAH — post-scale. Its engine is built and waiting.
```

---

## ❌ 7. REMOVED — so it is never rebuilt

```
✖ THE MUAMALAT ENGINE — the in-app portal kills it. "There is no
  bridge; both read the same Supabase." Its plan was deleted.

✖ THE PER-AYAH AUDIO SLICING — Mihrab never cuts a recitation.
  ⭐ Akhi's ruling: "I didn't mean avoid per-ayah. I meant avoid
    doing it MYSELF." An authentic per-ayah source is correct.

✖ THE MUSHAF PAGE MODE — three measured blockers on the fonts.
  ⭐ REPLACED BY the flowing surah, which needs nothing we do not
    own.

✖ "commerce: false" — THE WORD ITSELF. Mihrab always sells; only
  the commission varies. That word removed the QR from the one
  portal built to promote Mihrab.

✖ THE MARKAZ ROSTER, the portal group form, the per-group join
  code — the markaz creates NOTHING. The mechanism is the family's
  with one number changed.

✖ "U2b — Umrah live map (flutter_map+OSM)" — shipped, migrated to
  Google Maps. Closed.

✖ "Geo-Masjid Mode" — abandoned and torn down. Replaced by the
  time-based Prayer Mode, which shipped.
```

---

**The architecture is finished and the app is independent. What stands between Mihrab and being alive is a lane of paperwork nobody has started — fourteen declarations, a listing, a graphic and a set of screenshots. Everything else can wait. 🕋**
