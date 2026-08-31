# Mihrab Claude Code Prompt Format — Canonical Template

**For:** Brain (current and future). This is the EXACT format used across Car Mode v1.0 (Sessions 1-7) and Kids Mode + Family Entitlement v1.0 (Sessions 1-10B + Polish). It is the canonical pattern Brain inherits for all Claude Code prompts going forward.

**Origin:**
- **v1.0 (May 2026):** Refined across 7 Car Mode sessions. Captured the discipline of audit-first execution, zero regressions, and reproducible verification.
- **v2.0 (May 11, 2026):** Refined further across 10 Kids Mode + Family Entitlement sessions + Polish. Added audit-first sub-section, pre-paste setup commands, locked-decisions section, group-based scope, migration patterns, and post-prompt action steps.

This file replaces all previous versions of this document.

---

## Format Structure (Two Layers)

Every Brain → Claude Code prompt has TWO layers:

```
LAYER 1 — Outer Wrapper (Brain → Akhi instructions)
────────────────────────────────────────────────────
What model to use, what effort level, Plan Mode on/off, folder location,
worktree state. Pre-paste git setup commands. Akhi reads this and configures
Claude Code accordingly.

LAYER 2 — The Prompt (Akhi → Claude Code)
─────────────────────────────────────────
The exact text Akhi pastes into Claude Code. Wrapped in a markdown code 
block so it copies as a single unit.

LAYER 3 — Post-Prompt Notes (Brain → Akhi reference)
────────────────────────────────────────────────────
What the session delivers, action steps for Akhi, what to do after Claude
Code finishes (apply migration, run smoke test, merge). Appears AFTER the
inner code block.
```

All three layers are in a single downloadable `.md` file delivered to Akhi.

---

## Canonical Template (v2.0)

Use this as the skeleton for every session prompt. Fill in the `[BRACKETS]`.

```markdown
# Session [N] — [Title]

**Open Claude Code session. Folder: `C:\Users\kille\mihrab-app`. [Model], [Effort], [Plan Mode]. Worktree off.**

[1-2 sentence context paragraph: what this session does, why it's needed, where it sits in the broader phase]

[For complex sessions: "Three [or N] things ship: 1. ... 2. ... 3. ..." bullet preview]

Estimated [N] min wall-clock.

---

## Pre-paste setup (Akhi runs in PowerShell, ~30 sec)

​```powershell
cd C:\Users\kille\mihrab-app
git checkout main
git pull
git checkout -b feat/[branch-name]
​```

Confirm:
- `git log --oneline -1` → shows `[parent-commit-hash]` ([description]) or descendant
- `git branch --show-current` → shows `feat/[branch-name]`

---

## The Prompt

​```
New Claude Code session, [Model], [Effort], [Plan Mode]:

> **CRITICAL: Work in `C:\Users\kille\mihrab-app` — Flutter repo. Run `git pull` first. Verify with `git log --oneline -3`. Latest commit on main should be `[hash]` ([description]) or descendant. Verify branch with `git branch --show-current` — should be `feat/[branch-name]`. Worktree must be unchecked.**
>
> Package name is `mihrab_app`.
>
> **Reference docs (READ FIRST in this order):**
> 1. `docs/[primary-strategy-doc].md` — CANONICAL — Section [X.X] [topic], Section [X.X] [topic]
> 2. `docs/[secondary-guide].md`
> 3. `docs/screen_build_template.md` — Mihrab theme/file/import conventions
>
> **Task: Session [N] — [precise task title]**
>
> [Brief context: what we're building, what was done before, why now]
>
> ## Locked decisions (from [source] — DO NOT renegotiate)
>
> 1. **[Decision title]** ([source doc Section X.X]). [Locked value with rationale.]
> 2. **[Decision title]** ([source doc Section X.X]). [Locked value.]
> [N. ...continue for each locked decision]
>
> ## Audit-first (BEFORE Plan Mode plan)
>
> Investigate and report findings in 2-3 lines each.
>
> 1. **[Audit question title].** [What to investigate. What Brain expects. What to report.]
> 2. **[Next audit question].** [Same structure.]
> [N. ...continue for each audit point — typically 6-8 for complex sessions]
>
> Output the [N] audit findings BEFORE the Plan Mode plan.
>
> ## Scope ([N] deliverable groups + [optional migration])
>
> [For simple sessions: use numbered scope items 1, 2, 3...]
> [For complex sessions (3+ files, multiple concerns): use GROUP A, GROUP B, GROUP C... organization]
>
> ### MIGRATION — [Title] [INCLUDE IF DB CHANGES]
>
> **NEW: `supabase/migrations/[YYYYMMDDHHMMSS]_[name].sql`**
>
> ​```sql
> -- [Migration purpose]
> -- Reference: docs/[strategy-doc].md Section [X.X]
> [SQL]
> ​```
>
> Akhi applies in Supabase SQL Editor.
>
> ### GROUP A — [Theme/concern]
>
> **NEW: `lib/features/[path]/[file].dart`**
>
> [Detailed description — what to build, how, with file paths and signatures]
>
> **UPDATED: `lib/features/[path]/[file].dart`**
>
> [Specific changes — line numbers if known, before/after snippets]
>
> ### GROUP B — [Next theme]
>
> [Same level of detail]
>
> ## Plan Mode workflow [INCLUDE ONLY IF PLAN MODE ON]
>
> After audit findings, output a complete plan covering all [N] groups + migration. Surface the following decisions for Akhi's review BEFORE executing:
>
> 1. **[Decision title]** — Brain leans [Option A]: [rationale]. Alternative: [Option B]: [trade-off]. Confirm.
> 2. **[Next decision]** — Brain leans [...]. Confirm or revise.
> [N. ...continue for each decision needing review]
>
> Wait for Akhi's approval before executing.
>
> ## DO NOT touch
>
> - [Specific files/features that should not regress]
> - [Architectural concerns reserved for future sessions]
> - [Previously-shipped work — name it explicitly]
>
> ## Verification
>
> 1. `flutter analyze` — 0 new errors. Pre-existing [N] lints unchanged.
> 2. `flutter build apk --release --dart-define=ENABLE_ANDROID_AUTO=true` — clean compile.
> 3. `flutter build apk --release` — clean. Size delta within ±[N] MB from current [exact baseline] MB.
> 4. `flutter build web --release` — clean dart2js.
> 5. [Manual / DHU test description if applicable]
> 6. [Migration file present + idempotent — if migration in scope]
>
> Manual smoke test (Akhi runs post-merge[, post-migration-apply]):
>
> 1. [Step 1 — what Akhi does + what they should see]
> 2. [Step 2 — what Akhi does + what they should see]
> [...continue as numbered steps, plain English, sequential]
>
> ## Branch + commit
>
> Stay on `feat/[branch-name]`. Single commit:
>
> `[type]([scope]): [imperative summary]`
>
> Commit body:
> - [Bullet describing what changed - specific, file-level]
> - [Next bullet]
> - [...continue]
> - Reference: docs/[strategy-doc].md Section [X.X]
> - Closes [Phase N] / Out of scope: [item] (deferred to v1.1)
>
> Push to feature branch. PR auto-updates.
>
> [If applicable: "**Do NOT merge** — Akhi runs smoke test, applies migration in Supabase, verifies, then merges."]
>
> **Commit and push when done.**
​```

---

## What Session [N] Delivers

| Group | Deliverable | Files (estimate) |
|---|---|---|
| Migration | [Description] | 1 new SQL |
| A | [Description] | [N] new, [N] updated |
| B | [Description] | [N] files |
| [...] | | |

~[N] file changes + [N] migration. Estimated [N]-[N] min wall-clock.

## Action Steps for Akhi

1. Pre-paste git setup (top of file)
2. Open Claude Code: **[Model], [Effort], [Plan Mode]**
3. Paste inner code block
4. Claude Code: [N] audit findings + plan with [N] decisions
5. Review plan — especially [most important decision]
6. Approve plan → Auto mode
7. Return with commit hash + APK delta from [baseline] MB baseline + [migration SQL ready to apply / audit findings]
8. [Apply migration in Supabase / Run smoke test / Merge directly]

## After this session merges

**Next:** [Next session or phase] — Brain writes that prompt when you're ready.
```

(In actual files, the triple-backticks wrap the inner prompt as a single code block. Above I escaped them with zero-width joiners — your downloaded version uses real backticks.)

---

## Variable Reference

### `[Model]` — match task complexity

| Use | Pick |
|-----|------|
| Single-file UI tweak, mechanical rename, simple bug fix, translation work | `Sonnet 4.6` |
| Multi-file refactor, new architecture, design ambiguity, audit-heavy investigation, schema design | `Opus 4.7` |

### `[Effort]`

| Use | Pick |
|-----|------|
| Most tasks | `Medium` |
| Schema design, multi-screen builds, complex debugging, architectural refactors, lifecycle flows | `High` |

### `[Plan Mode]`

| Use | Pick |
|-----|------|
| Well-specified mechanical tasks (translations, font cleanup, single-file fixes) | `no Plan Mode` |
| Multi-step refactors, design decisions to lock, investigation needed, schema changes | `Plan Mode ON` |

**Rule of thumb (v2.0):** if the session has audit-first questions OR locked decisions to communicate to Claude Code, use Plan Mode. Plan Mode is the contract that lets Brain review Claude Code's interpretation before code changes.

### Scope organization choice

| Type | Use |
|------|-----|
| Simple session (1-3 files, single concern) | Numbered scope: `### 1.`, `### 2.`, `### 3.` |
| Complex session (4+ files, multiple themes, migration) | GROUP-based: `### GROUP A — [Theme]`, `### GROUP B — [Theme]` |
| Mixed (migration + code) | Migration first as standalone section, then GROUP-based code |

### Verification ladder (always 4-6 steps)

```
1. flutter analyze          (0 new errors)
2. APK with flag            (clean compile)
3. APK without flag         (clean compile, size delta within budget)
4. Web build                (dart2js erasure check)
5. Migration idempotent     (if migration in scope)
6. Manual / smoke test      (Akhi runs after commit, numbered steps)
```

The size-delta budget tightens as the project grows. State the EXACT current baseline in the prompt (e.g., "75.7 MB baseline, ±0.4 MB budget"). Vague baselines invite drift.

### Commit message format

```
type(scope): imperative-mood summary in <72 chars

- Bullet describing what changed (specific, not generic)
- Bullet for next change
- Bullet for next change
- Reference: docs/[master-guide].md Section [X.X]
- Closes [Phase N] / Out of scope: [item] (deferred to v1.1)
```

`type` examples: `feat`, `fix`, `docs`, `refactor`, `chore`
`scope` examples: `car`, `kids`, `family`, `payments`, `auth`, `audio`

---

## Audit-First Pattern (v2.0 addition)

The audit-first sub-section is the single most valuable refinement from Phase 3. It cut implementation errors by catching missing assumptions BEFORE Plan Mode generated the plan.

### What audit-first does

Before Claude Code writes any plan, it investigates the codebase and reports findings on specific questions Brain asks. This catches:
- Functions that already exist (saves duplicate work)
- Schema constraints Brain forgot
- Existing patterns to mirror (vs reinvent)
- Broken state from previous sessions (Session 10A caught broken RPCs from Session 9)
- Missing data the spec assumes (Session 10B caught no display-name field and no streak tracking — both were in the strategy doc mockup but didn't exist in the schema)

### How to write audit questions

Each audit question has:
1. **Title** — what's being investigated
2. **What to look for** — specific files, patterns, or behaviors
3. **What Brain expects** — Brain's hypothesis (Claude Code confirms or corrects)
4. **What to report** — 2-3 lines per finding

Example (from Session 10B):

```
### Task 3 — UI warning audit

6. **"Cannot get renderObject of inactive element" warning origin.** Search the
   codebase for any setState or Navigator calls in the Kids↔Adult transition path 
   that might fire on a deactivated widget. Likely sites:
   - `kids_mode_wrapper.dart` (the early-return logic added across Sessions 9, 9.5, 10B)
   - `kids_pin_entry_screen.dart` (PIN success → flip override → pop)
   - `graduation_celebration_screen.dart` (Continue → mark celebrated → invalidate → re-render to adult)
   - Any screen that calls `Navigator.pop` followed by `ref.invalidate(...)` in close succession

   Report: which transition path triggers the warning. If reproducible in code review,
   report the offending lines.
```

This audit caught the exact problem (`kids_pin_entry_screen.dart` setting state before `Navigator.pop()`) and revealed the fix (swap order) was structural, not defensive.

### How many audit points

| Session complexity | Audit questions |
|---|:---:|
| Mechanical (translations, fonts) | 2-3 |
| Single-feature (one screen + one service) | 4-6 |
| Multi-file architectural | 6-10 |
| Cross-cutting (migration + multiple screens) | 8-15 |

If you can't think of 4 audit questions for a complex session, the session probably isn't ready — Brain hasn't surfaced enough uncertainty yet.

---

## Locked Decisions Section (v2.0 addition)

For any session that builds on previous architectural work, include a "Locked decisions" section BEFORE the audit. This prevents Claude Code from re-litigating settled questions.

### Format

```
## Locked decisions (from [source] — DO NOT renegotiate)

1. **[Decision title]** ([source doc Section X.X]). [Locked value with brief rationale.]
2. **[Decision title]** ([source doc Section X.X]). [Locked value.]
```

### Example (from Session 10B)

```
## Locked decisions (from ratified strategy doc — DO NOT renegotiate)

1. **Graduate is one-way** (child_kid → child_adult only). No reverse path in v1.0.
   Strategy doc Section 3.2.
2. **Graduate preserves all data** — prayer streak, Quran progress, etc. carry forward.
   No data deletion.
3. **Celebration screen** shows ONCE on the kid's device after Graduate. Per strategy
   doc Section 3.2 (ASCII mockup): MashaAllah header, name, journey stats (streak/
   surahs/prayers), Continue button.
[...]
```

### Why this matters

Claude Code's natural tendency is to question requirements when implementation gets hard. Locked decisions tell it: "this question is closed. Don't re-litigate. Build the locked answer." Combined with reference doc section citations, this anchors Claude Code to the agreed architecture.

---

## Migration Section Pattern (v2.0 addition)

For sessions that include database schema changes, the migration goes FIRST in scope, as a standalone section before the code changes.

### Format

```
### MIGRATION — [Purpose]

**NEW: `supabase/migrations/[YYYYMMDDHHMMSS]_[name].sql`**

​```sql
-- [Purpose comment]
-- Reference: docs/[strategy-doc].md Section [X.X]

[SQL]
​```

Akhi applies in Supabase SQL Editor.
```

### Why migrations come first in scope

1. Code can't be tested until schema matches
2. RPC creation/update sometimes affects Dart calling code
3. Idempotency requirements (IF NOT EXISTS, CREATE OR REPLACE FUNCTION) need to be visible up front
4. Akhi's manual apply step happens BEFORE the smoke test

Each migration must:
- Use `IF NOT EXISTS` for column additions
- Use `CREATE OR REPLACE FUNCTION` for RPC updates
- Include `COMMENT ON COLUMN` for non-trivial new columns
- Reference the strategy doc section that justifies the change

---

## Hygiene Rules (BINDING, ratified May 12 2026)

All session prompts written by Brain MUST encode these rules:

1. **Pre-paste setup must include `git status --porcelain` for every touched repo.** If output is non-empty, the prompt either (a) blocks paste until orphan state is resolved manually, or (b) contains an explicit cleanup group (like Session 2's Group 0) that resolves the orphans inline.

2. **Claude Code never uses `git add .` or `git add -A`.** Prompts specify exact files to stage (`git add path/to/file1 path/to/file2`) or use `git add -p`. The hygiene risk of blanket-add was demonstrated in Session 2 pre-paste audit (May 12 2026): mihrab-app had accumulated 9 untracked items including a Flutter watch experiment conflicting with the ratified Kotlin Compose architecture; `git add .` in any earlier session would have committed orphans into feature branches.

3. **Phase boundaries trigger hygiene audit.** When a Phase completes, Brain runs `git status --porcelain` across all four Mihrab repos before starting the next Phase. Any non-empty output is resolved before Phase work begins.

4. **DECLARATION RE-CHECK RULE (MM-ratified, July 2026).** When a feature is added or changed that touches a DECLARED capability (background location, full-screen intent, DND/notification policy, camera, contacts), the store declarations AND the in-app disclosures are re-read in the SAME session. A declaration is code-adjacent, not documentation — it goes stale the moment the code moves. Rationale: this is the second occurrence of that drift — background location first (the masjid claim outliving the geo-masjid pivot), then full-screen intent (Prayer Mode shipping as a second FSI consumer while the declaration still said the Umrah takeover was the only one).

These rules apply alongside the audit-first and Plan Mode disciplines, not as a replacement.

---

## Outer Wrapper Conventions

Above the code block (Layer 1), include:

1. **Title** — `# Session [N] — [Title]`
2. **Configuration line** — bold one-liner: `**Open Claude Code session. Folder: PATH. Model, Effort, Plan Mode. Worktree off.**`
3. **1-2 sentence context** — what this session does, why now
4. **Three-thing preview** (for complex sessions) — "Three things ship: 1. ... 2. ... 3. ..."
5. **Estimated wall-clock time**
6. **Horizontal rule** — `---`
7. **Pre-paste setup section** — PowerShell commands Akhi runs before opening Claude Code (cd, git checkout, git pull, git checkout -b)
8. **Confirmation criteria** — what `git log --oneline -1` and `git branch --show-current` should show
9. **Horizontal rule** — `---`
10. **"## The Prompt" heading**
11. **The code block** — Layer 2, the actual prompt

Below the code block (Layer 3), include:

1. **"What This Session Delivers" table** — Group → deliverable → estimated files
2. **"Action Steps for Akhi"** — numbered checklist of what to do after Brain provides the prompt
3. **"After this session merges"** — what comes next on the roadmap

These post-block sections help Akhi read the prompt's intent at a glance before pasting.

---

## Numbered Smoke Tests (v2.0 addition)

After Phase 3, smoke tests became sequential numbered steps in plain English, NOT letter labels (a, b, c, "skip g-k").

### Format

Each step describes what the user DOES and what they SHOULD SEE, like a recipe:

```
Manual smoke test (Akhi runs post-merge, post-migration-apply):

1. Apply `[migration-filename].sql` in Supabase SQL Editor.
2. Sign in as parent → Settings → Family Dashboard.
3. Tap own (parent) slot → sheet opens, info-only, no Graduate/Remove buttons.
4. Tap a child slot → sheet opens with type-appropriate actions.
5. Tap Graduate (on a Kids slot) → confirmation → confirm → "Graduated to Teen" 
   SnackBar → slot updates to Teen.
6. Verify in Supabase: `member_type='child_adult'`, `graduated_at IS NOT NULL`, 
   `graduation_celebrated_at IS NULL`.
[...]
```

### Why this format

- Plain language survives across model versions and conversation compactions
- Each step is independently testable
- Brain can ask "tell me the result of step 4" and Akhi knows exactly which step
- Mid-test failures are reproducible (which step? what did you see?)

### One step at a time during execution

When running smoke tests with Akhi, Brain asks for ONE step at a time. Akhi pastes back the result. Brain confirms before issuing the next step. This prevents Akhi from racing ahead and missing failures.

---

## Examples — Phase History

### Car Mode v1.0 (May 2026, Sessions 1-7)

| Session | Configuration | Key outcome |
|---------|---------------|-------------|
| **1** | Opus 4.7, High, Plan Mode ON | Foundation work, multi-file scope. Audit caught 6th `_setAndroidAutoActive` write site missed by initial prompt |
| **4v2** | Opus 4.7, High, Plan Mode ON | 8 decisions to lock, 4-file impact. D3 audit revealed `setReciter` already preserves position — saved unnecessary refactor |
| **5** | Opus 4.7, High, Plan Mode ON | State pivot, 9 references to migrate. Phase 1+2 audit mapped 5 files / 9 touches with exact line numbers before edits |
| **6** | Opus 4.7, High, Plan Mode ON | 4 fixes in one session, investigation required. Empirical-verification step locked the right disconnect-detection approach |
| **7** | Sonnet 4.6, Medium, no Plan Mode | Single-button addition. Shipped clean in ~30 min, +316 bytes APK delta |

### Kids Mode + Family Entitlement v1.0 (May 8-11, 2026, Sessions 1-10B + Polish)

| Session | Configuration | Key outcome |
|---------|---------------|-------------|
| **8** | Sonnet 4.6, Medium, Plan Mode ON | PIN system. Audit confirmed bcrypt was already in pubspec via `crypto` |
| **9** | Opus 4.7, High, Plan Mode ON | Schema migration + Dart refactor. Pre-paste setup format adopted: PowerShell commands BEFORE the prompt |
| **9.5** | Sonnet 4.6, Medium, Plan Mode ON | Cross-cutting cleanup. Locked-decisions section adopted to prevent re-litigation of Crashlytics gating |
| **10A** | Opus 4.7, High, Plan Mode ON | Family Dashboard. Audit caught Session 9 RPCs (`accept_family_invite`, `redeem_claim`) silently broken by NOT NULL constraint without setting `member_type` — discovery beyond the brief |
| **10B** | Opus 4.7, High, Plan Mode ON | Graduate + Removal + Auto-promote. Two audit-surfaced gaps (no display-name field, no streak tracking) led to pragmatic v1.0 simplifications (type labels, prayer count only) |
| **Polish** | Sonnet 4.6, Medium, Plan Mode ON | Translation keys + font + UI warning. Audit caught Brain's prompt error (`amiri` vs `amiriQuran`); structural warning fix preferred over defensive mounted check |

### Phase 3 lessons absorbed into v2.0

- **Pre-paste setup is mandatory.** Without it, Akhi sometimes pasted the prompt on the wrong branch.
- **Locked decisions prevent re-litigation.** Session 9.5 originally re-debated Crashlytics gating until Brain added the section.
- **Audit-first catches inherited bugs.** Session 10A's audit found the broken RPCs from Session 9 — these would have failed at runtime but Brain didn't know.
- **Brain's prompt is not infallible.** Session 10A discovered Brain wrote `GoogleFonts.amiri()` but the canonical pattern is `GoogleFonts.amiriQuran()`. Audit-first by Claude Code catches Brain's mistakes too.
- **Group-based scope scales better than numbered.** When a session has 6+ files across 3+ concerns, GROUPs (A, B, C) read more clearly than `1.1, 1.2, 1.3, 2.1...`.

---

## What This Format Captures (And Why It Works)

Each section serves a specific purpose:

| Section | Purpose | Why it matters |
|---------|---------|----------------|
| **CRITICAL** preamble | Force `git pull`, verify branch, verify HEAD | Prevents working on stale code |
| **Pre-paste setup** | PowerShell commands BEFORE Claude Code opens | Eliminates "wrong branch" mistakes |
| **Reference docs in order** | Read most-authoritative doc first, with section pointers | Master Guide wins over outdated assumptions; specific sections cut research time |
| **Locked decisions** | Lists settled architectural questions with source citations | Prevents re-litigation when implementation gets hard |
| **Audit-first** | Investigation BEFORE Plan Mode plan | Catches missing assumptions, broken state, existing patterns to mirror |
| **Scope (groups OR numbered)** | Reviewable, testable, mappable | Mechanical sessions stay flat; complex sessions get themed |
| **Migration first in scope** | DB changes precede code | Code can't run without matching schema |
| **DO NOT touch** | Anti-regression list | Protects shipped work, enforces session boundaries |
| **Plan Mode decisions** | Surface design choices BEFORE coding | Brain reviews Claude Code's interpretation |
| **Verification ladder** | 4-6 standardized checks | Reproducible quality gates |
| **Numbered smoke test** | Sequential post-merge verification in plain English | Survives compaction, supports one-step-at-a-time execution |
| **Branch + commit** | Specific format, specific message, explicit merge/don't-merge | Clean git history, traceable commits, no premature merges |
| **Action Steps for Akhi** | Post-prompt checklist | Akhi knows exactly what to do after Brain delivers |
| **Commit and push when done** | Closing instruction | Unambiguous end state |

The format is intentionally redundant in places. Repetition is the discipline. Don't shorten.

---

## When To Deviate

The format is canonical, but don't follow it dogmatically. Deviate when:

- **Investigation-only sessions** — no code changes, just audit. Drop the "Verification" ladder, keep the rest. Add an "Audit deliverable" section instead.
- **Documentation-only commits** — no `flutter analyze` needed. Drop technical verification, replace with "Cross-check against [related doc]".
- **Hot-fix on main** — shorter scope, lighter wrapper. But still include CRITICAL preamble and pre-paste setup.
- **Empirical-verification sessions** — like Car Mode Session 6's logcat-data-needed phase. Add a "Pause for Akhi's data input" section between implementation and final verification.
- **Trivial single-file mechanical change** — drop Plan Mode, drop audit-first, keep CRITICAL preamble + scope + verification.

When you deviate, explain WHY in the outer wrapper so Akhi understands the format change.

---

## File Output Discipline

**Every prompt longer than ~30 lines goes in a downloadable `.md` file**, never inline in chat. Same applies to design briefs, master guide updates, status reports.

Inline only for:
- Short conversational answers
- Quick clarifications
- Approval / revision instructions to Akhi
- Status confirmations after a task ships

When you write a prompt, save it to `/mnt/user-data/outputs/SESSION_N_PROMPT.md` and present it via `present_files`. Akhi downloads, opens new Claude Code session, pastes Layer 2 (the inner code block), executes.

---

## Closing Note For Future Brain

This format was paid for in 7 Car Mode sessions + 10 Kids Mode sessions + Polish — 18 sessions of real shipping pressure. It survived:

- A v1 → v2 architectural pivot (Car Mode auto-detect → user-driven)
- 4 DHU verification cycles
- Multiple late-night debugging passes
- One near-miss with a Riverpod 2→3 upgrade incident
- A schema migration that silently broke 2 RPCs (caught by audit-first in Session 10A)
- Two strategy-doc-vs-reality gaps (no display-name, no streak tracking — caught by audit-first in Session 10B)
- A font-variant prompt error (`amiri()` vs `amiriQuran()` — caught by audit-first in Polish)

**Trust it.**

Adjust details as new lessons emerge — but the skeleton (CRITICAL preamble, Pre-paste setup, Reference docs in order, Locked decisions, Audit-first, Scope, DO NOT touch, Plan Mode decisions, Verification ladder, Numbered smoke test, Branch + commit, Action Steps for Akhi) is load-bearing.

When in doubt, look back at the Phase 3 commits on `main`:
- `9f0b302` Session 8 PIN
- `7f3d22a` Session 9 schema
- `69b9929` Session 9.5 cleanup
- `ff8920c` Session 10A Dashboard
- `4b088e4` Session 10B lifecycle (closes Phase 3)
- `e007dda` Polish

Every Phase 3 session prompt is preserved in the conversation transcripts. The format you see there is the format you write.

---

## Summary For Future Brain

Five things to remember when writing your first Mihrab prompt:

1. **Three layers**: outer wrapper for Akhi (model/effort/Plan Mode + pre-paste setup), inner code block for Claude Code, post-prompt notes for Akhi (what's delivered, action steps)
2. **Canonical sections inside the inner block**: CRITICAL preamble → Reference docs with section pointers → Task → Locked decisions → Audit-first → Scope (numbered or grouped) → DO NOT touch → [Plan Mode decisions] → Verification ladder → Numbered smoke test → Branch + commit
3. **Migrations first**: if the session touches DB, the migration is the first scope section, not buried
4. **Audit-first is the v2.0 superpower**: it catches what Brain forgot, what previous sessions broke, what the strategy doc assumed wrong
5. **File output**: deliver the prompt as a downloadable `.md` file, not inline

Get those five right and your prompts will inherit the discipline that shipped Car Mode v1.0, Kids Mode v1.0, and Family Entitlement v1.0 — three architecturally significant phases with zero net new lints, zero regressions to prior shipped work, and reproducible verification at every step.

---

| Created | May 3, 2026 |
| Updated | May 11, 2026 (v2.0 — Phase 3 refinements) |
| Author | Brain (Claude Opus 4.7) — refined across 18 shipping sessions |
| Status | ACTIVE — canonical prompt format for Mihrab |
| Supersedes | All prior versions of this document |
