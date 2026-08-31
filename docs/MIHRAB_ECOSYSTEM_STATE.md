# MIHRAB — ECOSYSTEM STATE
### The "where everything stands" snapshot, across every surface and vertical.

**Updated August 2026 · at the rotation that closed the Abstract Pattern.**
For *facts*, defer to the canonical docs (§9); this doc states **state + sequence**, not frozen numbers.

> **Why this doc carries no date in its filename:** the old one did, and it was flagged for deletion twice in error. A living doc does not carry a date in its name. The date lives on the line above, and moves when the doc moves.

> **What this doc is FOR:** it answers *"what is Mihrab right now?"* That is a different question from *"what's left?"* (the Launch Tracker) and *"what should Mihrab be?"* (v3). **Read this one first.**

---

## 1. WHAT / WHO / HOW

**Mihrab** — a multi-platform Islamic worship companion and ecosystem platform (mobile/PWA, Smart TV, Wear OS, car), freemium (Free + Premium, **IAP-only, no lifetime**), organised around four Circles: **Family, Masjid, Learning, Muamalat**. Owner: Ryad ("Akhi"), Apexiom Ltd (UK).

**Relay:** Master Model (ratifies architecture, Rule 34) → Master Brain (briefs → prompts) → Claude Code (executes). **Real validation is Akhi's on-device test** (Samsung A07 `R7AL10PDMAF`).

**Source of truth = `MIHRAB_MASTER_PLAN_v3`.**

---

## 2. REPOS & HEADs — verify with `git log --oneline -1`

| Repo | Stack | HEAD | Note |
|---|---|---|---|
| `mihrab-app` | Flutter | **`165bff2`** | **NOT auto-deploy** — `flutter build web --release; cd build\web; vercel --prod` |
| `Mihrabportal` | React/Vite | **`b0864db`** | **Auto-deploys from `main` — merging IS deploying** |
| `Mihrabtv` | React/Vite | `28a4c6a` | four store packages built |
| `Mihrabwatch` | Kotlin Compose | `b1b19f4` | `.aab` rebuilt 13.23 MB, signed |

**Supabase `aegbwuevbvtkicycfhrq`.** Migrations applied **by hand in the SQL Editor** — Claude Code never runs them.

**⚠️ One branch abandoned deliberately: `feat/markaz-clean` (`30ad4f8`).** Superseded by the Abstract Pattern. **Kept on the remote for its measurements; three surviving pieces ride into later sessions.**

---

## 3. ⭐ THE ABSTRACT PATTERN — the architecture, and the newest thing here

**One portal. Two instruments. A vertical is a row.**

```
✅ S1  THE REGISTRY   a partner type is a database row.
                      14 declaration sites → one.
✅ S2  SHOW / HIDE    a feature is a value. Guards: 1 → 20.
✅ S3  RENAME         the owner's word. 10 slots of 977 keys.
✅ S4  THE WORKSHOP   /owner/portal-types — create a type from a
                      form, based on an existing shape.
```

**⛔ AND THE BOUNDARY THAT GOVERNS EVERYTHING BUILT ON IT:**

```
⭐ THE OWNER CONTROLS A THING'S FACE — rename it, hide it, widen
  it, move it.
⛔ HE NEVER CONTROLS WHAT IT DOES — what a card counts, where a
   button leads, what a form writes.

"A card that can be pointed anywhere is a programming language."

⭐ ENFORCED IN SQL, not by convention: the update RPC RAISES on a
  `config` or `slug` key.
```

**⭐ AND THE LIMIT, STATED HONESTLY:** a vertical using existing screens needs no release. **A vertical needing a new kind of screen needs code, once.** Config cannot conjure a screen that was never built.

**⭐ "Customise" is a CATALOGUE, never a canvas** — a larger set of pre-built doors, never free composition.

---

## 4. STATE BY SURFACE

**App (`mihrab-app`)** — the worship core, 12 languages, Kids Mode, Family Circle, the Umrah pilgrim and guide experience, the Strong Messenger, Prayer Mode.

**⭐ AND THIS ROTATION MADE IT INDEPENDENT:**

```
⭐ THE QURAN SHIPS IN THE APK — Tanzil's Uthmani Mushaf, 6,236
  ayat, byte-verified, 16 permanent integrity tests. A fresh
  install that has never been online reads Quran.

⭐ PRAYER TIMES ARE COMPUTED ON THE PHONE — adhan_dart, 11 methods,
  both madhabs, a 1,344-point parity fixture. Aladhan is kept as a
  fire extinguisher, never removed.

⭐ THE MUSHAF READING MODE is the default — a surah as one flowing
  body of ornamented text, coloured harakat, entirely offline.
```

**Car = PARTIAL** (Android Auto compile-disabled; CarPlay absent). Live at `app.mihrab.faith` (manual deploy).

**⛔ AND THE PWA IS PARKED, DELIBERATELY.** It still shows a "Family Gift Package" gate for a product removed for legal reasons, and it **survived a fresh deploy** — so it is live code, not a stale build. Akhi's ruling: *"the PWA is only the restricted shadow of the native."* **Password resets now land there.**

**TV (`Mihrabtv`)** — four store packages built, none submitted.

**Watch (`Mihrabwatch`)** — Wear OS complete. Pairing repaired this rotation: `adhan2` pulled a newer `kotlinx-datetime` than supabase-kt was compiled against, and the crash was hidden by a missing token.

**Apple Watch** — absent. **Codemagic CI removes the Mac from the critical path;** blocked on the £79 and an Apple ID region mismatch.

**Portal (`Mihrabportal`)** — the Abstract Pattern, the influencer vertical, self-defined categories, owner-set commissions, and the trial watch window.

---

## 5. VERTICALS

**Shop / Bookshop** — live, config-driven.

**Umrah / Hajj** — ✅ **FEATURE-COMPLETE** and **audited this rotation.** Three Pattern sessions passed over its portal; **18 of 21 files under `src/shop-admin/` changed zero bytes.** The three that changed gained one string resolver each. **The phone was proved untouched from its own side.**

**⛔ AND ONE LIVE FAULT WAS FOUND AND FIXED:** the trial gate sat above every other check, so on day 15 an agency lost the live map — *where his pilgrims are, right now* — the roster, the checkpoints and Send Message. **Akhi extended three trials by hand in one evening.**

⭐ **Now: a 30-day watch window, read-only by construction** (the four writing tabs are never mounted), anchored on a past instant the partner cannot move.

**Tabligh** — the mechanism is settled and half-built. **One brother subscribes through the markaz QR and gets 15 seats instead of 6.** He sends the family invite code; each joins; **the markaz sees them appear and types nothing.**

**Madrasah** — specified, unbuilt. **The assessment IS the student profile** — name, level, where strong, where weak. **Its engine is the Auto Tajweed MM's work.**

**Prayer Mode** — ✅ shipped, Android-only in v1.

**Masjid Circle** — absent.

**Takabala Allah** — parked post-scale; its engine is built and waiting.

---

## 6. PARTNER-MONEY

**⭐ EVERY COMMISSION IS NOW A NUMBER THE OWNER TYPES.**

```
⛔ ONE LIVE MONEY LITERAL EXISTED: PARTNER_RATE = 0.15. It is gone.
   The representative's 8% was already dead code.

⭐ AND THE HOOK HAS NO FALLBACK NUMBER, deliberately: "a default
  there would be the literal I just removed, silently outranking
  whatever you'd typed." If a rate cannot resolve, the money waits.

⭐ FORWARD-ONLY BY TRIGGER — because service_role bypasses RLS, and
  service_role is what the hook runs as.
```

**⛔ COMMISSION, NOT COMMERCE — a correction that cost real damage:**

```
Mihrab is ALWAYS selling. The QR sells it. Only the SHARE varies.

⚠️ THE WRONG WORD — "commerce: false" — removed the QR and the
  introductions along with the wallet, from the one portal whose
  entire purpose is promoting Mihrab.

  Tabligh    none — the discount is the compensation
  Sufi       none
  ⭐ Islamic Centre  yes, at the influencer rate
  Dawah Street none — the da'i subscribes himself
```

**The influencer vertical is built** — its rate owner-set per influencer, and a physical partner's link protected by a daily cap and an education screen rather than a penalty.

**⚠️ AND THE MASJID PARTNER TYPE IS DROPPED from all money flows.** *"They're using masajid to take donations"* spreads faster than truth. **Clean by construction.** It survives in the registry as a legal-but-not-offered row.

---

## 7. ⭐ THE MAIL AND THE INFRASTRUCTURE

**Three faults were found and fixed this rotation, all of them silent:**

```
⛔ CONTACT US WAS A FAÇADE — six lines that showed a snackbar. The
   four text fields were never read.

⛔ mihrab.faith COULD NOT RECEIVE MAIL AT ALL. contact@ was in the
   live privacy policy and bounced.

⛔ AND THE APP'S PASSWORD RESET HAD NO DESTINATION — no redirectTo
   anywhere, and no deep link on either platform.
```

**Now:** DNS on Cloudflare, Email Routing live, `quran.mihrab.faith` serving from R2, branded auth emails, and Contact Us sending with an acknowledgement to the sender.

⚠️ **And `support@` was tried and reverted — it landed in junk twice.** `noreply@` reaches inboxes; the word is made untrue by a Reply-To and a visible address.

---

## 8. ⭐ ACCOUNT DELETION — a launch blocker closed

```
The privacy policy and the terms both promised "Settings → Account
→ Delete Account", twice each.

⛔ THAT SCREEN DID NOT EXIST. And the machinery did — built in
   April, with a nightly cron, and all three parts broken.
   cancel_account_deletion(uuid) took a user id, checked nothing,
   and PUBLIC could call it.
```

**Now: in-app deletion, a public page, a 30-day grace, and a prompt on sign-in that asks rather than silently cancelling.**

⭐ **And it says what no other app says: a Google Play subscription does NOT stop on deletion.** The Terms claimed otherwise and were corrected.

---

## 9. DOC STATUS / CANONICAL POINTERS

```
product   → MIHRAB_MASTER_PLAN_v3        pricing → MIHRAB_REGIONAL_PRICING_STRATEGY
APIs      → MIHRAB_API_SOURCES           prompts → MIHRAB_CLAUDE_CODE_PROMPT_FORMAT
behaviour → MIHRAB_AI_OPERATING_PROTOCOL comms   → MIHRAB_GANDALF_RULE (binding)
what's left → MIHRAB_LAUNCH_TRACKER

⭐ THE ARCHITECTURE  → MIHRAB_MASTER_PLAN_PATTERN · ABSTRACT_PATTERN
⭐ THE IN-APP SHIFT  → MIHRAB_IN_APP_PARTNER_PORTAL

⚠️ SUPERSEDED, kept as history:
   MIHRAB_FEATURE_VISIBILITY_MATRIX — visibility is now a feature
   set on portal_partner_types, set in /owner/portal-types

⛔ DELETED THIS ROTATION, and why:
   MIHRAB_GO_GREEN_DATA_INDEPENDENCE   its two sessions shipped;
                                       the rest was measurably wrong
   MIHRAB_REUSABLE_PORTAL_STRATEGY     the Pattern supersedes it
   MIHRAB_MUAMALAT_ENGINE_MASTER_PLAN  the in-app portal kills it —
                                       "there is no bridge; both
                                       read the same Supabase"
   MIHRAB_POLISH_QURAN_MOMENT_PLAN     those screens were rebuilt
```

---

## 10. ⭐ IMMEDIATE NEXT

**The architecture is done. What remains is one build arc and one lane of paperwork.**

```
THE BUILD ARC — the shapes, then the app
  1. ⭐ THE DATA LIST         serves six verticals
  2. ⭐ THE APP'S CATEGORIES  serves every religious type
  3. the scheduled reminder   four
  4. the self-recorded track  two
  5. willingness              markaz
  6. ⛔ DOES THE APP KNOW ITS USER IS A PARTNER?  — blocks 7-9
  7. the partner section
  8. the partner writes
  9. the customer reads

⛔ THE LAUNCH LANE — nobody has started it
  14 declarations in App content — four need a manifest audit first
  the store listing, from nothing
  the feature graphic 1024×500 — must be made
  screenshots — recapture; ⛔ the old set shows an "Advertisement"
    bar that contradicts the no-ads declaration
  LG's 400×400 icon · Amazon's name mismatch
```

**⚠️ AND TWO THINGS TO VERIFY BEFORE PRODUCTION:**

```
⭐ THE RTDN TOPIC IN PLAY CONSOLE — four log searches settle it,
  but not until somebody can actually buy. The app has sent the
  Supabase user id since 30 July.

⭐ AND: does a pilgrim's family see his Umrah? Six people hold
  entitlements through him and have no journey membership. The
  reading says they see nothing. NOBODY HAS MEASURED IT.
```

**PARKED, with reasons:** audio to R2 (the CDN is live; EveryAyah's licence unread) · translations offline (permitted via the Content Sync API, which needs the authenticated endpoint first) · the KFGQPC page fonts (three measured blockers) · Apple · Takabala Allah.

---

## 11. ⚠️ AND WHAT THIS ROTATION LEARNED THE HARD WAY

```
⛔ A SESSION WAS BUILT ON THE MM'S MISREADING AND THROWN AWAY.
   CC warned that the plan contradicted its own rules. The MM
   overrode it.

⛔ "commerce: false" REMOVED THE QR — a wrong word in a brief cost
   the one door through which the app gets installed.

⛔ AND A SPEC WAS READ, SUMMARISED, AND THEN LEFT OUT OF THE
   SESSION THAT NEEDED IT. Twice.

⭐ THE PATTERN IN ALL THREE: the MM decided before measuring, and
  CC's measurement was right every time.

⭐ AND ONE MECHANICAL LESSON: CC CANNOT SEE PROJECT FILES. It reads
  `docs/` inside the repo. A binding spec must be PUT THERE and
  pointed at — not pasted inline.
```

---

**Mihrab is a worship ecosystem across phone, watch, TV and car, with a commerce spine that is now configuration rather than code. The Quran and the prayer times belong to the app. The Umrah vertical is complete and audited. What stands between Mihrab and being alive is one build arc and a lane of paperwork nobody has started. 🕋**
