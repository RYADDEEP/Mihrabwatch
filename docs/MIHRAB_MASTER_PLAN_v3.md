# MIHRAB — MASTER PLAN v3.0
## Mihrab • Smartual Ecosystem
## "Where Smart Technology Meets Spiritual Practice"
## Apexiom Ltd (UK, Co. No. 17040459 · D-U-N-S 234590660) · June 2026

> **This is the single source of truth.** When any other document contradicts this one, **this wins.**
> v3.0 folds in and supersedes: **Master Plan v2.0**, the **Master Vision** (delta + session plan), the **Master Vision Corrections**, and the **Portal Current→Target Map** — plus everything v2.0 already replaced (Master Plan v1, Addendum, Strategics, Family Entitlement, Strategy 20, Unified Ecosystem).

> **Read this first.** The worship app is largely built. The **ecosystem around it** is the bulk of the remaining work — and, by strategy, **must be live at launch** (see Part 13). This is not a build-from-zero plan; it is what Mihrab *is* and what is left to finish.

---

# TABLE OF CONTENTS

1. What Mihrab Is
2. The Form — One App · One Portal Codebase · Per-Service Faces
3. The App — Free + Premium
4. The Portal — The Ecosystem Platform
5. The Service Portals (Shop · Books · Umrah · Masjid)
6. The Four Circles — Family · Masjid · Learning · Muamalat
7. The Partner Model
8. The Trust System
9. Prayer Mode
10. Umrah / Hajj Group + The Travel Vertical
11. The Business Model
12. Technical Architecture
13. Current State & Launch Path
14. The Expansion Model
15. Future Vision
16. Design Tokens
17. Binding Rules (Final)
18. Document Info

---

# PART 1: WHAT MIHRAB IS

A **mihrab** is the niche in a mosque wall that marks the qibla — the focal point that orients the worshipper toward prayer. That is the whole product: a quiet, beautiful companion that orients a Muslim's day toward worship.

```
IDENTITY
├── Premium-by-presence — value through what it is, not through withholding
├── Gentle guidance — it reminds with words, never enforcement, never shame
├── Worship-first technology — technology serves the worship, never the reverse
└── No uninvited OS manipulation — with one user-enabled exception (Prayer Mode, Part 9)
```

**Smartual** = **Smart** + **Spiritual**. The brand — **MIHRAB SMARTUAL PRODUCTS** — is the promise that smart technology can serve spiritual practice without cheapening it. Not a prayer-tracker that judges, not a gamified habit app, not a phone-nanny. A sanctuary.

```
QURANIC INSPIRATION
وَذَكِّرْ فَإِنَّ الذِّكْرَىٰ تَنفَعُ الْمُؤْمِنِينَ
"And remind, for indeed the reminder benefits the believers."
(Qur'an 51:55)
```

**Product:** Multi-platform Islamic worship companion + the commerce/community ecosystem around it.
**Market:** Muslims worldwide (v1 emphasis: Malaysia + global-by-design).
**Owner:** Apexiom Ltd (UK holding company).

---

# PART 2: THE FORM

Three things, one circuit. The app is the heart; every path from it leads out to the partner platform and the customer-facing service portals.

```
ONE APP  (worship + connector)
├── Free (with ads) + Premium (IAP only)
├── Does two things: WORSHIP and CONNECT to the ecosystem
├── Multi-device: phone, tablet, desktop/laptop (PWA), watch, TV, car, Kids Mode
└── The app is the CIRCUIT — it draws users toward the portals

ONE PORTAL CODEBASE  (Mihrabportal — one app, many faces by hostname + config)
├── PARTNER side  (portal.mihrab.faith): one portal, the sidebar adapts by partner type + tier
├── CUSTOMER side: SEPARATE clean per-service faces — never one mixed marketplace:
│     shop.mihrab.faith · books.mihrab.faith · umrah.mihrab.faith · masjid.mihrab.faith
├── Each face shows ONLY its own nav + categories; cross-links are buttons, never mixed navs
└── Same wallet, same tiers, same verification across every partner type
```

**One codebase truth:** every surface above is the **same React/Vite/Radix application** (the `Mihrabportal` repo), split by **hostname + per-type config**. `portal.*` is the partner/management surface; `shop.* / books.* / umrah.* / masjid.*` are the public/customer surfaces. The same two-side pattern powers the four Circles (Part 6).

---

# PART 3: THE APP — FREE + PREMIUM

Two tiers. This is the **compliance backbone** — the app is free, Premium is purchased only through in-app purchase, and the web commerce is a separate product. Get this exactly right.

## Free (with ads)
```
✅ Prayer times — all 6 (incl. Sunrise), with countdown
✅ Quran browser + audio — 3 reciters ONLY (Alafasy, Abdul Basit, Sudais)
✅ Morning & Evening Awrad ONLY
✅ Qibla compass · Tasbih counter · Hijri calendar
✅ 99 Names — "Name of the Day" ONLY
⚠️ Ads (AdMob)
❌ No premium themes, no custom adhan, no multi-device
❌ No Family Circle, no Kids Mode, no Umrah Group
```

## Premium (IAP ONLY — **$24.99/mo · $79.99/yr (full-price markets) · NO lifetime** — regional pricing varies, see the regional pricing strategy doc)
```
Everything in Free, PLUS:
✅ Ad-free  ·  All 18 reciters  ·  All Awrad categories  ·  Full 99 Names browser
✅ Premium themes + custom adhan (6 styles)  ·  Smart notifications + cloud sync
✅ Multi-device: TV + Watch + Car + Desktop PWA
✅ Family Circle — 6 members (2 parents + 4 children)
✅ Kids Mode — Juz Amma, prayer stars, 99 Names, Tasbih  ·  Family Dashboard + Graduate + PIN
✅ Connected Devices management
✅ Umrah / Hajj Group (join as member, follow the guide)
✅ Prayer Mode — the full-screen themed prayer face  ·  My Masjid (iqama times from a followed masjid)
```

**Premium is ONE entitlement.** Monthly / yearly are **billing options, not tiers.** The binary check in code is `isPremium` — no `isFamily`, no `isFamilyGift`, no `hasGiftEntitlement`.

**LOCKED — what no longer exists:** no **lifetime** plan; no Family Gift; no offline license; no out-of-app activation; no "Redeem Gift" / "Claim Your Gift" anywhere. Google/Apple take **15%** (Small Business Program).

**Family Sharing (how a family gets Premium):**
- **Adult family members** → **Google / Apple Family Sharing** (platform-managed — the subscription is shared at the store level).
- **Young children (no account)** → **Mihrab invite codes** ("**Join Family Circle**") — anonymous auth + role assignment. The invite code is a **family-management tool, not a payment bypass.**

**Multi-device model (LOCKED):** the **phone is the source of truth** (the only writer). TV, Watch, Car, and the desktop PWA are **read-only readers** that sync from the phone via `sync_payload` (v1.1, carries `display_language`). A Free user has the phone only and never pairs a device.

**Review prompt:** **Google's native in-app review for all users.** The old custom/hybrid review card is removed.

---

# PART 4: THE PORTAL — THE ECOSYSTEM PLATFORM

`portal.mihrab.faith` is one platform serving **every** partner type. The sidebar adapts to the partner's **type** and **tier**; everything underneath — wallet, verification, tiers, payouts — is shared.

```
PARTNER TYPES (one portal codebase, labels adapt by type):
├── Owner / Admin       — platform operator
├── Shop                — Islamic retail (oils, itr, clothing, prayer essentials, food, etc.)
├── Bookshop            — VERIFIED Islamic bookshops only (books, Quran sets, study materials)
├── Travel Agency       — Umrah/Hajj groups, tours, hotels, flights, eSIM (Part 10)
└── Masjid              — content: khutba, dars, dawa, announcements + iqama times (Part 6)

NOT partner types:
├── Halal Restaurant    — MAP GUIDE ONLY (discoverable on the map; no ordering, no delivery)
└── Smartphone Shop     — REFERRAL ONLY (introduces the app; not a portal partner)
```

**Two-side pattern (the portal's core mechanic):** every service has a **manage side** (create listings/content/controls) and a **browse/use side** (discover, follow, buy, enroll). Reused everywhere — the four Circles (Part 6) and every customer portal (Part 5).

**Mobile-first & responsive (foundational):** both surfaces are **mobile-first and fully responsive.** The portal is one reusable codebase every vertical and Circle inherits — the base must be sound on mobile or every vertical inherits the breakage.

**Tier system (pay-for-service): Standard / Silver / Gold / Power.** A partner **tops up his wallet to switch on his shop's services** — buying visibility and reach, not paying a flat subscription and not surrendering a cut of sales. **Standard** = listed/searchable · **Silver** = + in the in-app branded-verified-shops bar · **Gold** = + push to his network · **Power** = + sell eSIM + Travel (regulated — verify before unlock). The wallet is a **meter for services used**, not a clock; every shop starts on a **free trial**, then top-up keeps it live. (Full model: Part 7.)

---

# PART 5: THE SERVICE PORTALS

Each service has its **own clean customer face** — same `Mihrabportal` codebase, different hostname + config. **Never one mixed marketplace with tabs.**

```
shop.mihrab.faith   — PRODUCTS (LIVE). Categories: Oils & Natural Health, Prayer Essentials,
                       Islamic Clothing, Quran & Books, Food & Beverages, Fragrances,
                       Home & Decor, Gifts & Occasions. Nav: Products · Stores · Track order · My orders
books.mihrab.faith  — BOOKS (verified Islamic bookshops)
umrah.mihrab.faith  — TRAVEL (Umrah groups, tours, hotels, flights, eSIM — Part 10)
masjid.mihrab.faith — MASJID (imam content + iqama times + follow)
```

**Ranking:** **geo-priority** (local stores first, via the partner's verified GPS) + **tier-priority** (Silver/Gold/Power rank higher). **Localization:** 12 languages, RTL via Radix `dir` + logical CSS. **Schema:** `product_categories` (stable `name_key`), `products`, `shop_orders`, SECURITY DEFINER order functions, tier-cap trigger, compliance firewall (Premium IAP pricing never appears on shop pages).

**eSIM and Travel are NOT standalone categories.** They live inside the **travel vertical** (Part 10). **Charity** and **Finance** are **future** — not built. **Courses** live in the **Learning Circle** (Part 6). **Halal Restaurants** are a **map guide** only.

**Mihrab is NOT a payment processor for physical goods.** For Products / Books, the **customer pays the store directly** — Mihrab is the trust layer and the storefront, not the merchant of record (Part 8, Part 11).

---

# PART 6: THE FOUR CIRCLES

All four Circles are the **same portal two-side pattern** with different labels. Build one well; the others are re-skins.

```
FAMILY CIRCLE (الأسرة) — Premium
├── App dashboard stays as-is (Kids Mode, PIN, Graduate untouched)
├── Portal adds CONTENT SHARING: hikam, cards, video, audio
├── Parent side (more controls) + Member side (share + react)
└── Model: 6 members = 2 Parents + 4 flexible Children (each = Teen child_adult, or Kids child_kid)
            Graduate promotes Kid → Teen (parent action only)  ·  ONE Family PIN (LOCKED)

MASJID CIRCLE (المسجد)
├── Imam side (portal): content (khutba/dars/dawa), announcements, masjid info, IQAMA TIMES
├── Worshipper side: follow the masjid, receive content, "My Masjid" iqama card in app, locator
├── Hooks: IQAMA TIMES (mosque-specific start times — the #1 hook) · QR at the masjid door ·
│          imam-first acquisition (one imam → 100–500 congregants)
└── Pattern: shop name → masjid name · partner → imam · products → content

LEARNING CIRCLE (التعلم)
├── BOOKSHOP + COURSES (bookshop lives HERE — it is about knowledge)
├── Teacher/seller side (portal) + Student side (browse, enroll, track)
└── Bookshop runs on the SAME commerce back-end as the shop (listing/orders/wallet) — that is
    the back-end layer and does not change the circle. Courses are future.

MUAMALAT CIRCLE (المعاملات)
└── SHOP + UMRAH/TRAVEL (commerce dealings). Bookshop is NOT here — it is Learning.
```

The masjids a user can follow and receive content from come from the **Masjid Circle** (registered masjids). *(Prayer Mode, Part 9, is unrelated to this — it is time-based, involves no masjid at all, registered or otherwise, and uses no location.)*

---

# PART 7: THE PARTNER MODEL

"Partner," never "reseller" (except where **Shop** / **Bookshop** is a partner *type* label). A partner has up to two separate relationships with Mihrab: he can **run a shop**, and he can **introduce people to Premium**. They are paid for differently.

## His shop — pay-for-service (not a sale-cut, not a subscription)

A partner **tops up his wallet to switch on his shop's visibility and reach** — buying a *service*, not handing over a percentage of sales and not paying a flat fee.

- **Not a per-sale commission.** Mihrab only sees marketplace sales; a partner could close deals offline to dodge a cut — so there is no sale-tax, and nothing to dodge.
- **Not a "snoring" subscription.** The wallet is a **meter for services used** (sitting in the bar, firing pushes), not a clock that drains by date.
- **The tier features are the value** — the partner feels them by *using* them. Every shop starts on a **free trial**, then top-up keeps it live.
- Tiers: **Standard** (listed) → **Silver** (+ branded-shops bar) → **Gold** (+ push to his network) → **Power** (+ sell eSIM + Travel; regulated — verify before unlock).

**Native promo ≠ ad (LOCKED):** the branded-verified-shops bar is **native promo — a feature, not an ad.** It promotes real verified Islamic shops, curated and relevant, and taps through to the marketplace. **Premium removes third-party AdMob ads; it does NOT remove the native marketplace.**

## Introducing Premium — the Partnership Program (four roles)

Each role is earned through **Get Verified**; each share is **paid by Mihrab from its own margin** — never from the wallet, never from customer funds.

| Role | Who | Earns |
|------|-----|-------|
| **Partner** | Shop / bookshop / travel owner — promotes Mihrab hands-on | **15%** on Premium he introduces |
| **Representative** | Master partner / network builder | **8%** of his network's revenue |
| **Affiliate** | Vetted Islamic content creator with real reach (verified, not open) | **6%** on Premium he introduces |

**Affiliate criteria:** settable follower threshold (≈ **10,000+** to start) · Islamic content creator, not generic influencer · identity verified · admin-approved (human) · revocable.


**Single override only (LOCKED):** the Representative earns from *his own* network — the only override level. Never stacked. This keeps the program a clean referral, never a pyramid.

**Attribution must be rock-solid (binding):** because this carries **real money to real partners from week one**, tracking who introduced whom cannot be fragile. The QR/link is backed by a **typed-in Partner Code** fallback — not an afterthought.

**Partner-facing UI language (LOCKED):** the portal calls these **"Introductions"** (not "referrals") and **"Earnings"** (not "commission"). **"Customer"** is reserved for **actual shop buyers**; people introduced to Premium are **Introductions** (they become *Mihrab's* subscribers). Underlying mechanism is a referral commission; the *labels* are partner-dignified.

---

# PART 8: THE TRUST SYSTEM

The ecosystem is trustworthy because partners are **real, verified, and territorially protected** — and approvals are made by humans, not scripts.

```
GEO-VERIFICATION:
├── Official registered business name + physical GPS location, verified
├── Verification radius 200m (verified_lat/lng, verification_radius_m)
├── Territorial exclusivity 2km (exclusivity_radius_m = 2000) — no competitor within radius
├── "Authorized Mihrab Store" badge (geo_verified flag)
└── Effects: legal protection, territorial exclusivity, gray-market prevention, customer trust, data

HUMAN-VERIFIED ONBOARDING (no automated approvals):
├── Partner submits a Google Business listing URL
├── Admin reviews: registration + Maps match + 2km exclusivity → manual approval
└── Account Status (e.g. "Pending") reflects this Get-Verified gate

THE WALLET AS TRUST LAYER:
└── Transparent balance + history; funds services and earnings
```

---

# PART 9: PRAYER MODE

The one place Mihrab is permitted to touch the device — and only because the user explicitly enables it.

```
PRAYER MODE — the one sanctioned OS-level action

A pure opt-in personal courtesy: the phone falls quiet for the prayer, then restores itself.
Mosque adab expanded from a PLACE to the PRAYER MOMENT — in a masjid it is the preserved benefit,
at home it is barakah, at work it is a moment of focus. The prayer is sacred wherever the user is.

HOW IT WORKS
├── A "Prayer Mode" toggle in settings, OFF by default. Enabling it shows an education screen framed
│   as RESPECT, not enforcement, and requests the DND permission (Android) once.
├── At each prayer time the phone AUTO-SILENCES for a USER-TUNABLE WINDOW, then restores:
│   the window spans [prayer time − BEFORE minutes] to [prayer time + AFTER minutes], both set by the
│   user. Before covers arriving and settling; after covers the salah and the dhikr that follows.
├── Silent-or-Vibrate is the user's choice.
├── A PRAYER NOTIFICATION fires at the prayer time itself, in one of two faces:
│   ├── heads-up notification — the default, FREE
│   └── full-screen themed takeover — PREMIUM (visible-but-locked for free users)
├── Android: programmatic DND (one-time user-granted permission). iOS: unbuilt in v1.
├── It NEVER touches locally-playing media, and it silences Mihrab's OWN adhan within the window.
└── Restores by WINDOW (time), not by location. NO location permission is used.

WHY THIS DOESN'T BREAK "NO OS MANIPULATION"
It is user-enabled adab, not enforcement. The user explicitly enables it and grants the permission;
nothing happens uninvited; the user controls it entirely; it restores itself. This is the ONE
sanctioned OS-level action in the product — everything else is remind-with-words.

GATING (Feature Matrix Decision 14)
The COURTESY is FREE — the full auto-silence window, Silent-or-Vibrate, and the heads-up face.
The BEAUTY is PREMIUM — the full-screen themed face only.
Nobody pays to silence their phone for salah. The reasoning generalizes: gate aesthetic elevation,
never gate worship function. (Free users also keep the separate pre-prayer reminder feature, so no
free user loses prayer awareness.)

WHAT THIS REPLACED
"Geo-Masjid Mode" — silence triggered by DETECTING entry into a masjid via the global OSM mosque map
— was ABANDONED and torn down. The free public Overpass API rate-limits (429) at production scale,
and owning a world mosque dataset meant storage, refresh, and coverage-gap maintenance for a narrower
benefit. The prayer MOMENT, not the building, was always the sacred trigger — and the app already
owns the prayer times.
```

---

# PART 10: UMRAH / HAJJ GROUP + THE TRAVEL VERTICAL

A Premium feature *and* a partner business — and a strong viral loop (one group ≈ 10 new users). **Full vertical is day one** (the travel agency needs it from its first group — Part 13).

```
THE UMRAH GROUP SYSTEM (Premium feature):
Agency creates a group → members join via code/QR → the app becomes the guide.
├── BEFORE: schedule, checklist, group chat
├── DURING: live GPS tracking, distance alerts, step-by-step itinerary, checkpoints
├── AFTER:  post-trip summary (guide / agency / family keepsake)
└── BUILD:  12 screens (3 portal + 5 member + 4 guide) + 6 Supabase tables

THE TRAVEL AGENCY AS A PARTNER (commerce, same pattern as the shop):
├── Lists its full range through the portal: umrah, tours, hotels, flights
├── Same wallet tiers (Standard listing → Silver bar → Gold push)
└── Distributes Mihrab's eSIM (also keeps pilgrims' data live for the GPS tracking)

FLIGHTS & HOTELS — WHITE-LABEL OTA (LOCKED):
├── Mihrab invests ONCE in a white-label OTA — NOT built from scratch, Mihrab does NOT sell direct
├── Mihrab provides its agencies ACCESS; agencies pay a deposit via the SAME wallet and sell
└── Mihrab is the enabler, the agency is the seller — the trust-platform model holds
```

---

# PART 11: THE BUSINESS MODEL

Two completely separate revenue systems — and that separation is the legal safety valve.

```
REVENUE STREAMS:
├── App: Premium IAP — $24.99/mo · $79.99/yr (no lifetime). Google/Apple take 15%
├── Commerce: shop (products) · books · travel (umrah/tours/hotels/flights/eSIM via white-label OTA)
├── Partner services: pay-for-service wallet top-ups (visibility tiers) — Mihrab's recurring B2B revenue
└── Partner earnings: 15% on Premium introductions (paid from margin); Representative 8% of network

IAP-BYPASS SAFETY (why Google/Apple cannot claim bypass):
├── The app is FREE on Google Play; Premium goes through IAP (15% to the store)
├── shop.mihrab.faith is a LIVE, SEPARATE web commerce platform selling PRODUCTS — not app licenses
└── The Shop being live before Google Play submission is the evidence
```

This is why **the Shop must be live before Google Play submission** — it demonstrates a genuine ecosystem of web services and commerce, not an IAP side-door.

---

# PART 12: TECHNICAL ARCHITECTURE

```
REPOS:
├── mihrab-app   — Flutter           — phone + PWA + Kids + Car + Family
├── Mihrabportal — React/Vite/Radix  — portal.* + shop./books./umrah./masjid.* (hostname + config)
├── Mihrabtv     — React/Vite        — Samsung TV + Tizen .wgt
└── Mihrabwatch  — Kotlin Compose    — Wear OS (local computation via adhan-kotlin)
   (Apple Watch — SwiftUI — FUTURE, Premium-exclusive companion)

BACKEND:   Supabase — PostgreSQL, Auth, RLS, Realtime, Storage, pg_cron
HOSTING:   Vercel  ·  PAYMENTS: Google Play Billing + Apple StoreKit 2 (IAP)
LANGUAGES: 12 — EN, AR, MS, ID, FR, TR, UR, FA, HI, RU, SW, BN  (RTL: AR, UR, FA)

DATA FLOW: Phone = source-of-truth writer → sync_payload (v1.1, display_language)
           → TV / Watch / Car / Desktop PWA = read-only readers

EXTERNAL APIS (canonical list: MIHRAB_API_SOURCES.md):
├── Prayer times: Aladhan (api.aladhan.com); watch computes locally
├── Quran: Quran.com API v4 (api.quran.com/api/v4) — translation IDs verified per language
├── Qibla: local device-compass (no API)  ·  Maps/locator: OpenStreetMap / Leaflet
└── Travel flights/hotels: white-label OTA (Mihrab licenses; agencies access via wallet)
```

**Operational invariants (LOCKED):** Supabase migrations are applied **manually by the owner** in the SQL Editor; every function uses `SET search_path = public, pg_temp`; `Mihrabportal` and `Mihrabtv` are GitHub-integrated (push → auto-deploy); `mihrab-app` is **manual CLI deploy only** (`flutter build web --release` → `vercel --prod`).

---

# PART 13: CURRENT STATE & LAUNCH PATH

**The honest picture.** The **worship app core** is largely built (audio/reciters, prayer times, Quran, Awrad, Qibla, Tasbih, Kids Mode, Family Circle, Car, Wear OS, 12 languages). The **shop** is live for products. But the **ecosystem** is partly built and partly ahead: the **travel vertical (Umrah/Hajj) is COMPLETE**, the **portal reframe shipped**, and **Prayer Mode shipped**. What remains for launch is the **partner earning model** (IAP-gated), the **Masjid and Learning Circles**, **Apple Watch**, and the device revisions — and **it must be live at launch.**

> **Live state is NOT recorded here** — it rots. For what exists across every surface: `MIHRAB_ECOSYSTEM_STATE.md`. For what remains and in what order: `MIHRAB_LAUNCH_TRACKER.md`. This Part carries only the launch *strategy*, which does not change with the sprint.

**Why day-one (the strategic spine — binding):** Mihrab's go-to-market is **offline physical-shop distribution** via a representative who already has a shop network, onboarding shops **in waves from week one**. The launch **is** the sales pitch: a shop must hold, on day one, both a **differentiated ecosystem** (worth recommending over free apps) and its **own earnings** for promoting. Launching with shops promoting but without the earnings live is **self-demolish**. So the full build precedes the store, on purpose.

```
REMAINING WORK (phased — ~20–23 sessions / ~2.5–3 weeks):
├── Cleanup        — strip Family Gift / license refs (app + portal); fix loose ends
├── Portal reframe — Reseller→Partner; New Sale→New Introduction; Claims→My Introductions;
│                     Wallet → service tiers + earnings; remove Travel/eSIM from shop nav (~2 sessions)
├── Portal config  — partner_type config + per-service hostnames (shop/books/umrah/masjid)
├── Partner money  — Introduction/earnings engine (Partner QR + code, attribution, payout) + tiers
├── Masjid Circle  — schema + imam side + worshipper side + iqama times + locator
├── Learning       — bookshop labels + (courses future)
├── Family content — portal content sharing
├── Umrah Group    — schema + portal + 9 app screens + live GPS + white-label OTA access + eSIM
├── Prayer Mode     — ✅ SHIPPED (time-based prayer-window auto-silence + two faces)
├── Feature gating — Free vs Premium in code
├── IAP            — Google Play Billing ($24.99/mo · $79.99/yr) + Family Sharing + receipts
├── AdMob          — real ad-unit IDs
├── Marketing      — store listing assets
└── 🚀 Submission  — Google Play (Small Business Program, 15%)

OFF CRITICAL PATH: Samsung Tizen TV (Sessions 2–5); Apple Watch (SwiftUI) → iOS → Apple IAP.
ENROLLMENT: Apple Developer approved (C689X85U9P) — £79 payment blocked (UK mobile-number
validation / authorization); Apple support pending.
```

---

# PART 14: THE EXPANSION MODEL

The portal's two-side pattern is a **template.** Adding a vertical is mostly adding a label.

```
add a label  →  add a vertical:
├── "shop name" → "masjid name"   = Masjid Circle
├── "partner"   → "imam"          = Imam dashboard
├── "products"  → "content"       = khutba / dars / dawa
├── "shop"      → "agency"        = Travel / Umrah groups
└── "shop"      → "teacher"       = Learning Circle (courses)
```

Every new customer portal and every new Circle reuses the same codebase, wallet, verification, and tiers — which is why the ecosystem grows without re-architecting.

---

# PART 15: FUTURE VISION

```
POST-LAUNCH:
├── Tizen TV completion · Apple Watch (SwiftUI) · iOS App Store · Apple IAP
├── Courses (Learning Circle) · Charity · Finance · Halal-restaurant map — activate when ready
└── Deeper Masjid Circle + Family content

SMARTUAL HARDWARE (separate products, sold separately — FUTURE):
├── Mushaf Glasses — Quran in view
├── Smartual Ring  — discreet haptic dhikr/prayer reminder
└── Smartual Khimar — wearable haptic reminder (reminds gently; never takes over the phone)

ROADMAP: v3.0 (this document) → full ecosystem live → hardware line → global partner network
```

Smartual hardware **reminds**, it does not enforce — consistent with Part 1.

---

# PART 16: DESIGN TOKENS

```
CORE COLORS (LOCKED):
├── Background (solid):  #145240        — solid, not gradient (cream cards cover 70%+ of screen)
├── Hero prayer card:    #1A6B52 → #0F4D3A  (linear 180° gradient; 20px radius)
├── Gold accent ★:       #D4A537        — unified across ALL platforms
│   ├── Light #E8BC4A · Dark #B8943D · Glow rgba(212,165,55,0.4)
├── Cream cards:         #F5EFE6  (secondary #EDE5D8 · lighter #FDFBF7 · darker #E5DDD0)
└── Greens:              darkest #051812 · dark #0A2E22

PRAYER ROW COLORS (sky-based — the row recolors through the day):
├── Default  #F5EFE6 (text #2D3B35)   ├── Asr      #D4A537 (text #FFFFFF)
├── Fajr     #7B9EA8 (text #FFFFFF)   ├── Maghrib  #D47A5A (text #FFFFFF)
├── Sunrise  #E8BC7A (text #2D3B35)   └── Isha     #8B7AA8 (text #FFFFFF)
├── Dhuhur   #E8D44A (text #2D3B35)

TEXT:  dark-on-cream #2D3B35 · light-on-green #FFFFFF · secondary #5C6B65

TYPOGRAPHY:
├── UI:             Inter (400/500/600/700)
├── Arabic Quran:   Amiri Quran / Scheherazade New (24px mobile, 48px TV, line-height 2.0)
├── Arabic titles:  Noto Naskh Arabic / Amiri (20px, 600)
└── Display numbers: Outfit (700, tabular)

PLATFORM NOTES:  TV = dark glassmorphism · Gold = active nav / countdown rings / focus / premium badges.
```

---

# PART 17: BINDING RULES (FINAL)

**Foundational locked decisions (carried forward):**
- **Identity & design:** solid `#145240` background; unified gold `#D4A537`; green hero card with gold ring; sky-based prayer rows; Inter UI. (Part 16)
- **Content & language:** 12 languages; Awrad from Hisnul Muslim; authentic sources with full diacritics; **AI translates UI strings only — never Qur'an, Hadith, Dua, or Awrad.**
- **Architecture:** phone is the source-of-truth writer; TV/Watch/Car/PWA are read-only readers; Supabase backend; migrations applied manually by the owner.
- **Commerce posture:** pay-for-service, never inventory; Premium is IAP — no licenses to hold.

**Numbered binding rules (final):**

| # | Rule |
|---|------|
| 15 | Bilingual pickers LTR-locked on every platform (Surah / Language / Reciter / Translation) |
| 16 | Radix UI primitives need `dir={i18n.dir()}` on root (Tabs/Select/DropdownMenu/Popover/Toast) |
| 17 | Mihrabwatch scope is final (Prayer Home, Qibla, Pairing + Tile + Complication) |
| 18 | `sync_payload` v1.1 carries `display_language` |
| 19 | **Premium is IAP-only — monthly $24.99 / yearly $79.99, NO lifetime. No offline license, no Family Gift, no Redeem-Gift flow anywhere** |
| 20 | Kids Tasbih: serial chain, one gold per tap, 33 = celebration |
| 21 | Kids: no in-app exit on a child's device — only the parent Graduate flow |
| 22 | Adults sign up; Kids use anonymous auth (COPPA) |
| 23 | Religious terms: transliterate. Brand names: Latin |
| 24 | Placeholder convention HYBRID (`.replaceAll` Flutter / native typed React / `%s` Watch) |
| 25 | TV = installed app; browser = desktop companion only |
| 26 | Tizen 2.x OUT OF SCOPE; target Tizen 4.0+ |
| 27 | Kids check-in: app-open, batched, midnight reset, re-prompt on skip |
| 28 | Kids Reconnect: reuse the same slot |
| 29 | **Partner model: shop monetized by pay-for-service wallet top-up (not a sale-cut, not a subscription). Premium introductions earn — Partner 15%, Representative 8% of network, Affiliate 6%. NOT inventory, NOT offline licensing** |
| 30 | Brand = MIHRAB SMARTUAL PRODUCTS |
| 31 | **Mihrab is a trust platform, NEVER a payment processor for physical goods (customer pays the store directly)** |
| 32 | **Prayer Mode (opt-in; the courtesy FREE, the themed face Premium): at each prayer time the phone auto-silences for a USER-TUNABLE window — [prayer − before] to [prayer + after] — then restores. Silent-or-Vibrate; a prayer notification in one of two faces (heads-up FREE / full-screen themed PREMIUM). Android programmatic DND; iOS unbuilt in v1. TIME-based, never location-based — uses no location permission. Never touches local media; also silences Mihrab's own adhan in the window. This is the ONE sanctioned OS-level action.** |
| 33 | **Shop must be LIVE before Google Play submission (IAP-bypass safety valve)** |
| 34 | **No unilateral architectural changes: propose → consult Master Model → implement only after agreement** |
| 35 | **Native promo ≠ ad: the branded-verified-shops bar is a feature. Premium removes third-party AdMob ads, NOT the native marketplace** |
| 36 | **Single override only: the Representative earns from his own network — no deeper stacking. Never MLM/pyramid** |
| 38 | **Mobile-first, fully responsive portal — both surfaces. Every vertical inherits the base, so the base must be sound on mobile** |
| 39 | **Ecosystem ready on DAY ONE: the offline physical-shop distribution model requires the full ecosystem (shop + earnings + circles + travel) live at launch. The launch is the partner's sales pitch — a half-built proposition is self-demolish** |
| 40 | **Separate clean per-service customer portals (shop / books / umrah / masjid) — one codebase + config + hostname. NEVER one mixed marketplace with tabs** |
| 41 | **eSIM + Travel are NOT standalone categories — they live in the travel vertical. Flights/hotels via a WHITE-LABEL OTA Mihrab provides access to (agencies deposit via wallet and sell); Mihrab is the enabler, not the seller** |
| 42 | **Bookshop lives in the Learning Circle (knowledge), built on the commerce back-end.** |
| 43 | **Partner-facing UI: "Introductions" (not referrals), "Earnings" (not commission); "Customer" = actual shop buyers only. Attribution is robust — QR/link + typed Partner Code backup** |
| 44 | **Family Sharing: adults via Google/Apple Family Sharing; young children via Mihrab invite codes ("Join Family Circle") — a family tool, not a payment bypass. Review prompt = Google native for all** |
| 45 | **No masjid as an institution in ANY money flow — no masjid partner type, no masjid QR, no masjid bank account, no revenue share to a mosque. A masjid may be a place the product respects, never a party the product pays. Rationale (binding doctrine): where a false narrative could never be outrun, avoid the exposure ENTIRELY rather than manage it — clean by construction, not defended by explanation. This governs every future feature touching money, institutions, or trust.** |

---

# PART 18: DOCUMENT INFO

| Field | Value |
|-------|-------|
| Title | MIHRAB — Master Plan v3.0 · "Mihrab • Smartual Ecosystem" |
| Status | **DEFINITIVE — single source of truth.** |
| Folds in & supersedes | Master Plan v2.0, Master Vision, Master Vision Corrections, Portal Current→Target Map (and all docs v2.0 already replaced) |
| Owner | Apexiom Ltd (UK, Co. No. 17040459 · D-U-N-S 234590660) |
| Brand | MIHRAB SMARTUAL PRODUCTS |
| Date | June 2026 |
| Rule | When any other document contradicts this one, **this wins** |

> وَذَكِّرْ فَإِنَّ الذِّكْرَىٰ تَنفَعُ الْمُؤْمِنِينَ — *"And remind, for indeed the reminder benefits the believers."* (51:55)
