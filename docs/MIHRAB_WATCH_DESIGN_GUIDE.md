# MIHRAB WATCH — DESIGN GUIDE (CANONICAL)
## Source of Truth for Wear OS + Apple Watch Visual Design

**Status:** ACTIVE — canonical design spec for Phase 5 SmartWatch and future Apple Watch phase
**Origin:** Master Model design session, refined in Figma Make file `vNoaM3PWOVFVZywzXU51dD`
**Promoted to project doc:** May 12, 2026 (from `src/imports/mihrab_watch_figma_final_guide.md` in the Smartwatchmode-main repo export)
**Supersedes:** `mihrab_watch_figma_complete_guide.md` (older 8-screen version that included dropped Tasbih view)

**For Brain:** Reference this file when writing Kotlin Compose Claude Code prompts in Phase 5 Sessions 4–6.
**For Claude Code:** Read this file at the start of any Mihrabwatch session — it is the visual ground truth.

---

# MIHRAB SMART WATCH — COMPLETE FIGMA MAKE GUIDE
## 6 Screens | Guided Generation Flow | Everything Explained

---

# PART 1: UNDERSTANDING THE SMART WATCH

## Read This Entire Section First — Do Not Skip

Before we create any screens, you must deeply understand what a smart watch IS and IS NOT. This will prevent design mistakes.

---

## 1.1 What is a Smart Watch?

```
A SMART WATCH IS:
═══════════════════════════════════════════════════════════════

✅ A GLANCE DEVICE
   └── User raises wrist, looks for 2-3 seconds, lowers wrist
   └── That's it. That's the entire interaction.

✅ A NOTIFICATION SURFACE
   └── Buzzes wrist → User glances → Gets info → Done
   └── Haptic feedback is the killer feature

✅ A SENSOR PLATFORM
   └── Compass (Qibla direction)
   └── GPS (location)
   └── Heart rate (not relevant for Mihrab)

✅ A COMPANION to the phone
   └── Not a replacement
   └── Syncs data from phone
   └── Minimal standalone capability
```

```
A SMART WATCH IS NOT:
═══════════════════════════════════════════════════════════════

❌ A TINY PHONE
   └── Cannot do phone tasks on smaller screen
   └── Different interaction model entirely

❌ A SUSTAINED INTERACTION DEVICE
   └── User cannot hold arm raised for minutes
   └── Arm fatigue is real

❌ A READING DEVICE
   └── No paragraphs, no articles, no long text
   └── Numbers and icons only

❌ A REPETITIVE TAP DEVICE
   └── Tapping 99 times on watch = terrible UX
   └── Small target, arm raised, breaks focus
```

---

## 1.2 The 3-Second Rule

```
THE FUNDAMENTAL WATCH LAW:
═══════════════════════════════════════════════════════════════

If a user cannot get complete value in 3 SECONDS or less,
the feature DOES NOT BELONG on a watch.

┌─────────────────────────────────────────────────────────────┐
│                                                             │
│   SECOND 1:  Raise wrist, screen wakes                      │
│   SECOND 2:  Eyes find information                          │
│   SECOND 3:  Brain processes, user understands              │
│   DONE:      Lower wrist, continue life                     │
│                                                             │
└─────────────────────────────────────────────────────────────┘

If your screen needs SECOND 4, 5, 6... it's wrong.
```

---

## 1.3 Why We REMOVED Tasbih Counter

I must explain this so you understand our design thinking:

```
TASBIH COUNTER ANALYSIS:
═══════════════════════════════════════════════════════════════

What Tasbih requires:
├── Tap screen 33, 66, or 99 times
├── Sustained focus on counting
├── Meditative, peaceful state
├── Comfortable body position
└── Large, easy tap target

What watch provides:
├── Tiny 1.5 inch screen
├── Minimum 44px touch targets (small)
├── Arm must be raised to see screen
├── Arm fatigue after 30 seconds
├── Easy to mis-tap
└── Screen may sleep mid-count

CONCLUSION: TERRIBLE MATCH.

The user should use:
├── Physical prayer beads (Sunnah!)
├── Mobile app (phone on table, tap comfortably)
└── NOT the watch

We are not removing Tasbih because we can't build it.
We are removing it because we CARE about the user experience.
A feature that frustrates users is worse than no feature.
```

---

## 1.4 What DOES Belong on Mihrab Watch?

```
FEATURE JUSTIFICATION:
═══════════════════════════════════════════════════════════════

✅ PRAYER HOME (Screen 1)
   WHY: Core use case. "What's the next prayer?" 
   TIME: 2 seconds to glance
   VERDICT: Perfect for watch

✅ PRAYER ALERT (Screen 2)
   WHY: Haptic buzz + glanceable notification
   TIME: 2 seconds to see "Maghrib in 5 min"
   VERDICT: This is what watches are MADE for

✅ QIBLA COMPASS (Screen 3)
   WHY: Uses watch compass sensor. User is moving/traveling.
   TIME: 3 seconds to see direction
   VERDICT: Sensor-based feature, belongs on watch

✅ PRAYER LIST (Screen 4)
   WHY: Secondary screen. User swipes to see all times.
   TIME: 3 seconds to scan
   VERDICT: Simple list, acceptable

✅ SETTINGS (Screen 5)
   WHY: Just haptic toggle. Everything else syncs from phone.
   TIME: Quick toggle
   VERDICT: Minimal, acceptable

✅ COMPLICATIONS (Screen 6)
   WHY: Native watch face feature. Always visible.
   TIME: 0 seconds (always on screen)
   VERDICT: Perfect for watch

❌ TASBIH COUNTER
   WHY: Requires 99 taps with arm raised
   TIME: 5-10 MINUTES
   VERDICT: Wrong device. Use phone.
```

---

## 1.5 The Mihrab Watch Philosophy

```
MIHRAB WATCH MANTRA:
═══════════════════════════════════════════════════════════════

"Prayer time at a glance. Alert on your wrist. Nothing more."

We do not try to put the entire app on the watch.
We put only what BELONGS on the watch.

The watch is a WINDOW into prayer times.
The phone is the FULL experience.
```

---

# PART 2: DESIGN SPECIFICATIONS

## 2.1 Canvas Size

```
PRIMARY CANVAS: Apple Watch 45mm
─────────────────────────────────────────────────────────────────
Width:   396px
Height:  484px
Shape:   Rounded rectangle
Corners: 32px radius
```

We design for Apple Watch first, then adapt for round Wear OS.

---

## 2.2 Colors — MEMORIZE THESE

```
COLORS (exact HEX values):
═══════════════════════════════════════════════════════════════

BACKGROUNDS:
├── Background:       #000000  ← Pure black (OLED battery)
├── Surface:          #1C1C1E  ← Cards, elevated elements
└── Surface Active:   #2C2C2E  ← Pressed states

TEXT:
├── Primary:          #FFFFFF  ← Main text (pure white)
├── Secondary:        rgba(235,235,245,0.6)  ← Labels
└── Tertiary:         rgba(235,235,245,0.3)  ← Hints, disabled

ACCENT:
├── Gold:             #D4A537  ← Brand accent, active states
├── Gold Bright:      #F5C842  ← Alerts, emphasis
├── Success Green:    #30D158  ← Completed prayers
└── Alert Red:        #FF453A  ← Missed prayers

PRAYER COLORS:
├── Fajr:     #8AABBA  (dawn blue)
├── Sunrise:  #F5C978  (warm yellow)
├── Dhuhr:    #F5D84A  (bright noon)
├── Asr:      #E8BC4A  (afternoon gold)
├── Maghrib:  #E8845A  (sunset coral)
└── Isha:     #A08BC8  (night purple)
```

---

## 2.3 Typography

```
TYPOGRAPHY SCALE:
═══════════════════════════════════════════════════════════════

Display XL:   48px Bold      ← Hero time (current prayer)
Display LG:   36px Bold      ← Large countdowns
Heading:      24px SemiBold  ← Prayer names
Body LG:      17px Medium    ← Primary labels
Body MD:      15px Medium    ← Secondary labels
Caption:      13px Regular   ← Hints (MINIMUM SIZE)

⚠️  NEVER go below 13px. Unreadable on watch.
```

---

## 2.4 Touch Targets

```
TOUCH TARGET RULES:
═══════════════════════════════════════════════════════════════

Minimum:      44px  ← Absolute minimum, hard to tap
Recommended:  48px  ← Good size
Comfortable:  56px  ← Easy to tap

Every button, every row, every tappable element: 44px MINIMUM.
Fat fingers + tiny screen = need big targets.
```

---

## 2.5 Spacing

```
SPACING:
═══════════════════════════════════════════════════════════════

Screen padding:     16px (all sides)
Card padding:       12px
Gap between items:  8px
Section gap:        16px
```

---

# PART 3: SCREEN LIST

We will create **6 screens** total:

| # | Screen | Size | Purpose |
|---|--------|------|---------|
| 1 | Prayer Home | 396×484 | Main glanceable screen |
| 2 | Prayer Alert | 396×484 | Notification when prayer approaches |
| 3 | Qibla Compass | 396×484 | Direction to Mecca |
| 4 | Prayer List | 396×484 | All 5 prayers + sunrise |
| 5 | Settings | 396×484 | Minimal config (haptic toggle) |
| 6 | Complications | Various | Watch face widgets |

---

# PART 4: CONFIRMATION CHECKPOINT

## Before We Begin Generating

Please confirm you understand by summarizing:

1. **The 3-second rule**: User must get value in 3 seconds or less
2. **Why no Tasbih**: Requires sustained interaction, wrong device
3. **Canvas**: 396×484px, pure black #000000 background
4. **Accent**: Gold #D4A537
5. **Minimum touch target**: 44px
6. **Minimum text size**: 13px

**Once you confirm understanding, say:**
"I understand the watch design philosophy. The watch is for glanceable information only, not sustained interaction. I'm ready to generate Screen 1 (Prayer Home). May I proceed?"

---

# PART 5: SCREEN PROMPTS

---

## SCREEN 1: PRAYER HOME

### Why This Screen Exists

This is THE reason someone installs Mihrab on their watch. They want to glance at their wrist and instantly know:
- What is the next prayer?
- What time is it?
- How long until the prayer?

That's it. Nothing else. One glance, one answer.

### Design Approach

```
PRAYER HOME PHILOSOPHY:
═══════════════════════════════════════════════════════════════

WRONG APPROACH (what I almost did):
├── Show current prayer
├── Show countdown
├── Show list of all prayers ← NO! Too much!
├── Show scroll indicator ← NO! 
└── Cognitive overload

CORRECT APPROACH:
├── ONE giant ring
├── ONE time
├── ONE prayer name
├── ONE countdown
└── NOTHING ELSE

The screen should feel EMPTY.
Empty = glanceable.
Cluttered = unusable.
```

### Prompt

```
Create an Apple Watch prayer home screen at 396×484px.

FRAME:
- Size: 396×484px exactly
- Background: #000000 (pure black, OLED optimized)
- Corner radius: 32px (Apple Watch shape)

SINGLE CENTERED ELEMENT:

Progress Ring (the ONLY visual element):
- Diameter: 280px (takes up most of screen)
- Position: Centered horizontally AND vertically
- Stroke width: 10px
- Track color: rgba(255,255,255,0.1)
- Progress fill: #D4A537 (gold)
- Progress amount: 70% filled (example)
- The ring represents time elapsed in current prayer period

Inside the Ring (vertically stacked, all centered):

Line 1 — Current Time:
- "05:42"
- Font: 48px Bold
- Color: #FFFFFF

Line 2 — Prayer Name:
- "FAJR"
- Font: 24px SemiBold
- Color: #D4A537 (gold)
- Margin top: 8px

Line 3 — Countdown:
- "in 23m"
- Font: 17px Medium
- Color: rgba(235,235,245,0.6)
- Margin top: 4px

NOTHING ELSE ON SCREEN.

No list of prayers.
No additional UI elements.
No scroll indicators.
No bottom bar.

The background is pure black.
The ring floats in the void.
Maximum contrast. Maximum glanceability.
```

### After Generating Screen 1

**Show me the result and say:**
"Screen 1 (Prayer Home) is complete. This screen shows only the essential information: current time, prayer name, and countdown. The user gets complete value in 2 seconds. May I proceed to Screen 2 (Prayer Alert)?"

---

## SCREEN 2: PRAYER ALERT

### Why This Screen Exists

This is the notification that appears when a prayer time is approaching (15 minutes, 5 minutes, or at prayer time). The watch buzzes, the user raises their wrist, sees this screen.

This is what watches are MADE for — haptic notification + glanceable info.

### Design Approach

```
PRAYER ALERT PHILOSOPHY:
═══════════════════════════════════════════════════════════════

CONTEXT:
├── Watch buzzes (haptic alert)
├── User raises wrist quickly
├── User sees: "Oh, Maghrib in 5 minutes"
├── User lowers wrist
├── Total time: 2 seconds

DESIGN GOALS:
├── Immediately obvious which prayer
├── Immediately obvious how much time
├── Easy to dismiss
├── Prayer color for visual recognition
```

### Prompt

```
Create an Apple Watch prayer alert screen at 396×484px.

FRAME:
- Size: 396×484px
- Background: #000000 (pure black)
- Corner radius: 32px

CONTENT (vertically centered in frame):

Prayer Icon:
- Simple mosque/minaret silhouette
- Size: 48px
- Color: #E8845A (Maghrib coral — matches prayer)
- Position: Centered, 120px from top

Prayer Name:
- "MAGHRIB"
- Font: 28px SemiBold
- Color: #FFFFFF
- Centered
- Margin top: 24px from icon

Alert Message:
- "in 5 minutes"
- Font: 20px Medium
- Color: #E8845A (prayer color)
- Centered
- Margin top: 8px

Prayer Time:
- "18:22"
- Font: 48px Bold
- Color: #FFFFFF
- Centered
- Margin top: 20px

Dismiss Button:
- Text: "Dismiss"
- Width: 180px
- Height: 48px
- Background: #1C1C1E
- Text: 17px Medium, #FFFFFF
- Radius: 12px
- Position: Centered, 50px from bottom

HAPTIC ANNOTATION (just a note):
- Add text note: "Strong haptic pulse on appearance"

The prayer color (#E8845A for Maghrib) creates instant visual recognition.
User sees coral color = knows it's Maghrib without reading.
```

### After Generating Screen 2

**Show me the result and say:**
"Screen 2 (Prayer Alert) is complete. This notification appears with a haptic buzz when prayer time approaches. The prayer color provides instant recognition. May I proceed to Screen 3 (Qibla Compass)?"

---

## SCREEN 3: QIBLA COMPASS

### Why This Screen Exists

Unlike Tasbih (which we removed), Qibla compass BELONGS on the watch because:
- It uses the watch's compass sensor
- User is moving/traveling and needs direction
- One glance shows the direction
- No sustained interaction needed

### Design Approach

```
QIBLA COMPASS PHILOSOPHY:
═══════════════════════════════════════════════════════════════

USE CASE:
├── User is traveling or in new location
├── User needs to find Qibla direction
├── User raises wrist, sees compass
├── Arrow points to Mecca
├── User turns body, finds direction
├── Done in 3-5 seconds

WHY IT WORKS ON WATCH:
├── Uses watch sensor (compass)
├── User is mobile (watch is on their wrist)
├── Glanceable result (arrow pointing direction)
├── No sustained interaction

TV doesn't have compass sensor.
Phone works but watch is already on wrist.
```

### Prompt

```
Create an Apple Watch Qibla compass screen at 396×484px.

FRAME:
- Size: 396×484px
- Background: #000000
- Corner radius: 32px

HEADER:
- "Qibla"
- Font: 20px SemiBold
- Color: #FFFFFF
- Centered, 24px from top

COMPASS (main element):
- Diameter: 240px
- Position: Centered, 70px from top
- Border: 2px solid rgba(255,255,255,0.15)
- Background: Transparent (#000000 shows through)

Compass Cardinal Points:
- "N" at top: 17px Bold, #FFFFFF
- "E" at right: 15px, rgba(235,235,245,0.5)
- "S" at bottom: 15px, rgba(235,235,245,0.5)
- "W" at left: 15px, rgba(235,235,245,0.5)
- Position each at edge of compass circle

Compass Tick Marks:
- Small lines every 30 degrees
- Color: rgba(255,255,255,0.2)
- Length: 8px

QIBLA ARROW (the key element):
- Shape: Triangle/arrow pointing outward
- Direction: ~294° (northwest, example for Bangkok)
- Length: 80px from center to tip
- Color: #D4A537 (gold)
- The arrow originates from center, points to Qibla
- Add subtle glow: 0 0 16px rgba(212,165,55,0.4)

Kaaba Icon (optional):
- Small Kaaba symbol at arrow tip
- Size: 20px
- Color: #D4A537

DIRECTION INFO (below compass):
- Degrees: "294°"
  - Font: 36px Bold
  - Color: #D4A537
- Direction name: "West-Northwest"
  - Font: 15px Medium
  - Color: rgba(235,235,245,0.6)
- Centered, 16px below compass

LOCATION (bottom):
- "📍 Bangkok"
- Font: 13px
- Color: rgba(235,235,245,0.3)
- Centered, 24px from bottom
```

### After Generating Screen 3

**Show me the result and say:**
"Screen 3 (Qibla Compass) is complete. This screen uses the watch's compass sensor to show direction to Mecca. The gold arrow is immediately visible. May I proceed to Screen 4 (Prayer List)?"

---

## SCREEN 4: PRAYER LIST

### Why This Screen Exists

This is a secondary screen. The user swipes from Prayer Home to see all prayer times for the day. It's a simple scannable list.

### Design Approach

```
PRAYER LIST PHILOSOPHY:
═══════════════════════════════════════════════════════════════

This is NOT the home screen.
User swipes here when they want to see full schedule.

DESIGN:
├── Simple vertical list
├── Each row: Prayer name + time
├── Color dot shows prayer identity
├── Current prayer highlighted
├── That's it
```

### Prompt

```
Create an Apple Watch prayer list screen at 396×484px.

FRAME:
- Size: 396×484px
- Background: #000000
- Corner radius: 32px
- Padding: 16px all sides

HEADER:
- "Prayer Times"
- Font: 17px Medium
- Color: rgba(235,235,245,0.6)
- Left-aligned
- Height: 32px

PRAYER LIST (below header):
- 6 rows total
- Gap between rows: 2px

Each Row:
- Height: 56px
- Full width (minus padding)
- Background: Transparent

Row Content (horizontal layout):
- Left: Color dot
  - Size: 10px diameter
  - Color: Prayer-specific color
  - Position: 16px from left, vertically centered
- Center: Prayer name
  - Font: 17px Medium
  - Color: #FFFFFF
  - Position: 36px from left
- Right: Time
  - Font: 17px Medium
  - Color: #FFFFFF
  - Position: Right-aligned, 16px from right

Row Data:
1. Fajr     05:42  dot:#8AABBA  ← CURRENT (highlighted)
2. Sunrise  06:58  dot:#F5C978  (text at 60% opacity)
3. Dhuhr    12:30  dot:#F5D84A
4. Asr      15:45  dot:#E8BC4A
5. Maghrib  18:22  dot:#E8845A
6. Isha     19:35  dot:#A08BC8

CURRENT PRAYER HIGHLIGHT (Fajr row):
- Background: #1C1C1E
- Left border: 3px solid #D4A537
- Radius: 12px

Dividers:
- 1px line between rows
- Color: rgba(255,255,255,0.05)

Sunrise row:
- Text at 60% opacity (it's not a prayer, just a time)
```

### After Generating Screen 4

**Show me the result and say:**
"Screen 4 (Prayer List) is complete. This secondary screen shows all prayer times. The current prayer is highlighted with gold accent. May I proceed to Screen 5 (Settings)?"

---

## SCREEN 5: SETTINGS

### Why This Screen Exists

The watch uses the Hybrid architecture — most settings sync from the mobile app. The watch only needs settings that are watch-specific:

- Haptic alerts (on/off)
- That's basically it

### Design Approach

```
SETTINGS PHILOSOPHY:
═══════════════════════════════════════════════════════════════

WRONG APPROACH:
├── Location setting ← NO, syncs from phone
├── Calculation method ← NO, syncs from phone
├── Language ← NO, syncs from phone
├── Full settings menu ← NO, too complex

CORRECT APPROACH:
├── Haptic toggle (watch-specific)
├── Link to complications
├── Show what's synced from phone (read-only)
├── Done

Minimal. The phone handles configuration.
```

### Prompt

```
Create an Apple Watch settings screen at 396×484px.

FRAME:
- Size: 396×484px
- Background: #000000
- Corner radius: 32px
- Padding: 16px all sides

HEADER:
- "Settings"
- Font: 20px SemiBold
- Color: #FFFFFF
- Left-aligned, 20px from top

SETTINGS CARDS:

Card 1 — Haptic Alerts (main setting):
- Background: #1C1C1E
- Radius: 16px
- Padding: 16px
- Height: 60px
- Margin top: 20px

Card 1 Content:
- Left: "Vibration" (17px Medium, #FFFFFF)
- Right: Toggle switch
  - Size: 50px × 28px
  - ON state: #D4A537 background, white circle on right
  - Radius: 14px (full)

Card 2 — Complications:
- Same card style
- Left: "Watch Face" (17px Medium, #FFFFFF)
- Right: "›" chevron (15px, rgba(235,235,245,0.4))
- Height: 56px
- Margin top: 8px

SYNCED INFO SECTION:
- Label: "From iPhone"
  - Font: 13px Medium
  - Color: rgba(235,235,245,0.4)
  - Margin top: 32px

Info Card:
- Background: rgba(255,255,255,0.05)
- Border: 1px dashed rgba(255,255,255,0.1)
- Radius: 12px
- Padding: 16px
- Margin top: 8px

Info Card Content (vertical list):
- "📍 Bangkok, Thailand" (15px, #FFFFFF)
- "Hanafi • MWL" (13px, rgba(235,235,245,0.5))
- "English" (13px, rgba(235,235,245,0.5))
- Line height: 24px

Note below card:
- "Change in Mihrab app"
- Font: 11px
- Color: rgba(235,235,245,0.3)
- Centered, margin top: 8px

VERSION (bottom):
- "v2.0.1"
- Font: 11px
- Color: rgba(235,235,245,0.2)
- Centered, 20px from bottom
```

### After Generating Screen 5

**Show me the result and say:**
"Screen 5 (Settings) is complete. This minimal settings screen only has watch-specific options. All other settings sync from the mobile app. May I proceed to Screen 6 (Complications)?"

---

## SCREEN 6: COMPLICATIONS

### Why This Screen Exists

Complications are widgets that appear on the watch face. They let users see prayer info without opening the app. This is native watch functionality.

We will create 2 complication sizes.

### 6A: Circular Complication

```
Create an Apple Watch circular complication at 84×84px.

FRAME:
- Size: 84×84px exactly
- Background: Transparent
- Shape: Circle

PROGRESS RING:
- Outer diameter: 84px
- Stroke width: 5px
- Track: rgba(255,255,255,0.15)
- Fill: #D4A537, 65% progress
- The ring shows time until next prayer

CENTER CONTENT:
- Background: #000000 circle, 70px diameter

Inside center (stacked vertically):
- Prayer name: "ASR"
  - Font: 14px Bold
  - Color: #FFFFFF
- Time: "15:45"
  - Font: 11px Medium
  - Color: #D4A537
- Gap: 2px between them
```

### 6B: Rectangular Complication

```
Create an Apple Watch rectangular complication at 180×50px.

FRAME:
- Size: 180×50px
- Background: #1C1C1E (or transparent, depending on watch face)
- Radius: 12px

LAYOUT (horizontal):

Left Section (icon):
- Prayer icon or colored dot
- Size: 28px
- Color: #E8BC4A (Asr color)
- Position: 12px from left, vertically centered

Center Section (text):
- Prayer name: "ASR"
  - Font: 15px Bold
  - Color: #FFFFFF
- Time: "15:45"
  - Font: 13px Medium
  - Color: rgba(235,235,245,0.7)
- Position: 48px from left
- Stacked vertically, 2px gap

Right Section (countdown):
- "2h 15m"
- Font: 13px Medium
- Color: #D4A537
- Position: 12px from right, vertically centered
```

### After Generating Screen 6

**Show me the result and say:**
"Screen 6 (Complications) is complete. Both circular and rectangular complications are ready for watch faces. All 6 watch screens are now complete. Would you like me to create round Wear OS adaptations?"

---

# PART 6: FINAL CHECKLIST

## All 6 Screens

| # | Screen | Size | Status |
|---|--------|------|--------|
| 1 | Prayer Home | 396×484 | ⏳ |
| 2 | Prayer Alert | 396×484 | ⏳ |
| 3 | Qibla Compass | 396×484 | ⏳ |
| 4 | Prayer List | 396×484 | ⏳ |
| 5 | Settings | 396×484 | ⏳ |
| 6 | Complications | 84×84 + 180×50 | ⏳ |

## What We Deliberately Excluded

| Feature | Reason |
|---------|--------|
| ❌ Tasbih Counter | Requires 99 taps with arm raised — wrong device |
| ❌ Quran Reader | Text too small, sustained reading — use phone |
| ❌ Awrad Reader | Same as above — use phone |
| ❌ Full Settings | Syncs from phone — Hybrid architecture |

## Key Design Rules Applied

1. ✅ 3-second glanceability on every screen
2. ✅ Pure black #000000 background (OLED)
3. ✅ Minimum 44px touch targets
4. ✅ Minimum 13px text
5. ✅ Gold #D4A537 accent throughout
6. ✅ Prayer colors for instant recognition
7. ✅ Minimal UI, maximum content

---

# READY TO BEGIN

**Please confirm your understanding, then we will generate Screen 1 (Prayer Home).**

After each screen, I will ask for your permission before proceeding to the next.

