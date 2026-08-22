# AutoMinder — 2026 MVP Plan
**Status: SOURCE OF TRUTH for scope, sequence and ship criteria.**
Written 2026-08-16 against the verified repository, not against any prior document.
Supersedes PRD.md, DESIGN_SYSTEM.md, HANDOFF.md and UI_REDESIGN_NOTES.md where they conflict.
Governance (CLAUDE.md) still wins on process. Code still wins on fact.

---

## 1. The thesis

> AutoMinder is the app that tells you what your car needs **before** it costs you money.

The market is saturated with maintenance loggers. AutoMinder is not competing on
logging. It is competing on **one promise**: the reminder arrives on time, and
everything it says is true.

That yields exactly three things worth being best at:

| # | Promise | What it means in code |
|---|---|---|
| 1 | **The reminder fires correctly** | `ReminderCheckWorker` + cooldowns + boot rescheduling actually work on real devices |
| 2 | **Nothing shown is invented** | No fabricated scores, no `0 km` for absent data, no advertised capability that doesn't exist |
| 3 | **Logging costs almost nothing** | Prefill everything known; one primary action per screen; ~15s to log a service |

Everything else — charts, OCR, analytics, silhouettes — is decoration on top of
those three, and none of it ships until all three are proven.

---

## 2. The finding that reorders this plan

**The reminder engine cannot keep the promise — and it has no tests.**

This section was revised on second audit. The first version said the engine was
untested. That was true but too generous: the **architecture itself cannot
deliver "reminds you on time"** on a large share of Android devices, and no
amount of testing fixes that.

Here is the entire scheduling strategy:

```kotlin
// WorkScheduler.kt — this is all of it
PeriodicWorkRequestBuilder<ReminderCheckWorker>(6, TimeUnit.HOURS).build()
WorkManager.getInstance(context)
    .enqueueUniquePeriodicWork(REMINDER_CHECK_WORK, ExistingPeriodicWorkPolicy.KEEP, request)
```

Five defects, in severity order:

1. **`PeriodicWorkRequest` is best-effort, not a guarantee.** It is deferred by
   Doze, by App Standby buckets, and — critically — by OEM battery managers.
   Xiaomi, Huawei, Oppo, Vivo, Samsung and Realme all ship aggressive process
   killers that stop WorkManager outright. That is the majority of the South
   Asian market this product explicitly targets (the seed database is full of
   Suzuki Altos and the mock data says "QuickLube Karachi"). **On those phones
   the reminder simply never arrives, and nothing tells the user.**
2. **No `AlarmManager` anywhere.** Date-critical items — insurance expiry,
   registration, inspection — need `setExactAndAllowWhileIdle`. A 6-hour
   best-effort poll is the wrong instrument for "expires tomorrow".
3. **No exact-alarm permission declared.** Android 12+ requires
   `SCHEDULE_EXACT_ALARM`, or `USE_EXACT_ALARM` for apps whose core function is
   reminders. AutoMinder plausibly qualifies for the latter — but it is declared
   nowhere, so exact scheduling is currently impossible.
4. **No battery-optimization handling at all.** Zero references to
   `isIgnoringBatteryOptimizations` in the codebase. There is no exemption
   request, and no honest fallback message when the OS has throttled us.
5. **`ExistingPeriodicWorkPolicy.KEEP`** means any future change to cadence or
   logic never reaches existing installs — they keep the old schedule forever.

Add to that: `ReminderCheckWorker`, `WeeklyDigestWorker`, `WorkScheduler` and
`BootReceiver` have **zero tests** between them, while 70 tests pass elsewhere.

`PRODUCT_STRATEGY_AUTOMINDER.md §9` said *"reminder apps die of silence, not
competition"* and treated it as a growth risk. It is a **correctness** risk.

**A maintenance app whose reminder silently fails is worse than no app** — the
user believes they are covered, and finds out when something breaks. That is the
precise harm this product exists to prevent.

---

## 3. Document authority — ending the drift

Nine documents currently describe this app and they disagree with each other and
with the code. This is now settled:

| Document | Status | Action |
|---|---|---|
| `CLAUDE.md` | **AUTHORITATIVE** — process, governance, phase | Keep. Update stale nav/token references only. |
| `PLAN.md` (this file) | **AUTHORITATIVE** — scope, sequence, ship criteria | — |
| `docs/DESIGN_SYSTEM_2026.md` | **AUTHORITATIVE** — visual system | Keep, with §3 corrected (see §4) |
| `docs/exec-plans/mobbin-design-blueprint-2026.md` | **AUTHORITATIVE** — screen contracts, Mobbin evidence | Keep |
| `PRODUCT_STRATEGY_AUTOMINDER.md` | **ADVISORY** — strategy, still the best thinking here | Keep as strategy, not as scope |
| `UI_AUDIT_2026.md` | **ADVISORY** — findings valid, tokens stale | Mine for issues; ignore Racing Teal references |
| `PRD.md` | **ARCHIVE** | Move to `docs/archive/`. Stale on: 3 tabs, `GOOD/DISABLED/NO_DATA` enums, Racing Teal, Exo 2, Fuel Intelligence in v1.0, Billing 7.1.1, health-score ring, gamification. Its market statistics are unsourced (`UI_REDESIGN_NOTES.md` already flagged this) and **must not** appear in store copy. |
| `DESIGN_SYSTEM.md` | **ARCHIVE** | Fully superseded by `DESIGN_SYSTEM_2026.md` |
| `HANDOFF.md` | **REWRITE** | Dated 2026-07-07; describes a branch and slice pipeline that has moved on |
| `docs/UI_REDESIGN_NOTES.md` | **FOLD IN** | Its findings are absorbed here; delete after |

**Rule going forward:** a document that contradicts the code is a bug in the
document. Fix it or archive it — never leave it to mislead the next session.

---

## 4. Contradictions, resolved

These were live conflicts across the documents. Each is now decided.

| Conflict | Resolution | Evidence |
|---|---|---|
| Status-dependent corner radii (8/16/28dp) | **REMOVED.** Shape is set by component family. Status is carried by icon + label + colour + rail + copy — five channels, still colour-blind safe. | BMW's [NEXT SERVICES](https://mobbin.com/screens/fbbd81ca-7614-4e75-8d50-94907c1cc870) and Tesla's [history](https://mobbin.com/screens/a1ea4590-a32c-4634-94b0-5dd3e669e2af) both convey urgency with zero shape variation. Ragged radii read as broken, not informative. **`DESIGN_SYSTEM_2026.md §3` must be edited to match.** |
| `GOOD` / `DISABLED` / `NO_DATA` states | **Do not exist.** Real enum: `OVERDUE, DUE_SOON, SNOOZED, OK, COMPLETED, UNKNOWN` | `domain/model/ServiceStatus.kt` |
| Racing Teal `#006B5F` vs Midnight Cobalt `#7AB4FF` | **Midnight Cobalt.** | Shipped in commit `59653a8`; `DESIGN_SYSTEM.md` is stale |
| Exo 2 + Nunito Sans vs Manrope | **Manrope + JetBrains Mono**, but **not yet implemented** — no Manrope files in `res/font/`. Queued as its own slice. | Verified filesystem |
| Health score | **Deleted.** Four competing formulas existed; one scored `UNKNOWN` as 100/100. | Removed 2026-08-16, gate green |
| 3 tabs vs 4 tabs | **4: Home / Vehicles / Records / Settings.** Never rename Records. | `strings.xml:6-9` |
| Fuel Intelligence in v1.0 | **v1.1.** | CLAUDE.md overrides PRD |

---

## 5. What "MVP ready" actually means

Not "feels finished". These are binary, checkable gates.

### 5.1 Blocking — cannot submit without these

| # | Gate | Status | Owner |
|---|---|---|---|
| B1 | **Billing 9.1.0 migrated + signed release verified** | Code done, gates green in worktree; **release build unverified** — needs keystore | User supplies signing config |
| B2 | **Reminder engine has tests and is device-verified** | **NOT STARTED** | Next work |
| B3 | **Privacy policy URL live and reachable** | `website/` committed, not deployed | Deploy + link in Settings |
| B4 | **Data Safety form matches real SDK behaviour** | Not started | AdMob + Firebase + Billing all collect |
| B5 | **Locale claims match shipped translations** | **BROKEN** — `localeFilters` declares `es`/`pt-rBR`; only `values/` exists | Either ship translations or drop the filters |
| B6 | **Signed AAB from clean release gate** | Blocked on B1 | — |
| B7 | **Store listing + screenshots** | Blocked on B8 | — |
| B8 | **Demo/seed data is credible** | **BROKEN** — "Overdue by 173,000 km" | Reseed before any capture |

> **B5 is a misrepresentation risk, not a polish item.** Declaring Spanish and
> Portuguese support while shipping only English is a false store claim. The
> honest fix for MVP is to **drop the locale filters** and launch English-only;
> translation is a v1.1 project, not a launch blocker.

### 5.1b Platform gates — added on second audit, previously missed

These are 2026 Play/Android platform requirements, not preferences. Each needs
an explicit verified/not-verified answer before submission.

| # | Gate | Why it blocks |
|---|---|---|
| P1 | **16 KB memory page support** | Required for apps targeting Android 15+. Every bundled native library (Firebase, AdMob, Crashlytics NDK) must be 16 KB-aligned or the app crashes on 16 KB devices. `packaging.jniLibs` is configured; alignment is **unverified**. |
| P2 | **Edge-to-edge enforcement** | `targetSdk 36` enforces edge-to-edge. Any screen assuming insets it no longer gets will clip content behind the system bars. |
| P3 | **Predictive back** | Enforced at `targetSdk 36`. `DESIGN_SYSTEM_2026 §7` requires it on every in-progress form; never verified. |
| P4 | **Play Vitals thresholds** | Crash rate must stay under ~1.09% and ANR under ~0.47% or Play demotes discoverability. Crashlytics is a dependency; **no evidence it has ever reported a real session.** |
| P5 | **Backup / device transfer actually restores the database** | `allowBackup="true"` with `backup_rules` is declared, but for an offline-first app **Room is the only copy of the user's data**. If backup silently excludes the DB, a phone upgrade wipes years of history. The PRD itself cites aCar dropping 4.5→1.6 stars over exactly this. Untested. |
| P6 | **Notification-permission denial path** | If POST_NOTIFICATIONS is denied the product has no function. There must be a truthful in-app state and a route to re-enable — not a silent dead app. |

**Data export must not be Pro-gated.** `ExportServiceHistoryUseCase` exists and
the PRD lists CSV/JSON export as a paid feature. For an offline-first app where
the device holds the only copy, charging for the ability to get your own data out
is a trust failure and a data-portability risk. Export is free, forever.

### 5.1c Performance — the plan claimed 120 Hz and measured nothing

| # | Item | Finding |
|---|---|---|
| S1 | **Baseline Profile is not actually wired** | `alias(libs.plugins.baselineprofile)` is applied in `app/build.gradle.kts`, but there is **no producer module** — no `baselineprofile/` or `macrobenchmark/` directory exists. The plugin is decorative; no profile is generated, and the typical 20–30% cold-start win is being left on the floor. `PRODUCT_STRATEGY §5` flagged this and it is still unwired. |
| S2 | **No startup measurement at all** | No macrobenchmark, so "fast" is an assertion. Target: first verdict visible under 800 ms on a mid-range device. |
| S3 | **No jank measurement** | `DESIGN_SYSTEM_2026 §11` says "the design is not done if it does not scroll at 120 Hz" — nothing measures it. |

### 5.2 Quality bar — should not ship broken

| # | Item | Source |
|---|---|---|
| Q1 | Paywall ✓/✕ icons unannounced to TalkBack (WCAG 4.1.2) | `UI_AUDIT_2026.md §3.1` |
| Q2 | `EmptyState` subtitle ≈3.8:1 contrast, below 4.5:1 (WCAG 1.4.3) | `UI_AUDIT_2026.md §3.2` |
| Q3 | FAB speed dial: no scrim, no `BackHandler` | `UI_AUDIT_2026.md §2.2` |
| Q4 | Widget renders `serviceType.label` raw, bypassing `localizedLabel()` | Verified `AutoMinderWidget.kt:73` |
| Q5 | 2.0× font scale + TalkBack + reduced-motion sweep | Only 1.5× ever verified |
| Q6 | `RecordsTimelineCard`, `FormSectionCard` built but never used (0 usages) | Verified |

---

## 6. The queue — do these in this order

Each step is one branch, one gate, one commit. Do not start the next until the
current one is green.

### Step 0 — Commit the truthfulness fix *(ready now)*
Health score removal. Gates green: compile ✅ · 70/70 tests ✅ · lint ✅ · assembleDebug ✅.
Blocked only on deciding whether to bundle the four pre-existing sentence-case
string edits.
`fix(ui): report maintenance status the app can actually know`

### Step 1 — Make the reminder engine trustworthy ⚠️ **highest value in this plan**
Branch: `fix/reminder-engine-reliability`

**1a — Fix the architecture** (this is not optional polish; see §2)
- Add `AlarmManager.setExactAndAllowWhileIdle` for date-critical reminders
  (insurance, registration, inspection); keep WorkManager for interval sweeps
- Declare `USE_EXACT_ALARM` (core function is reminders) or request
  `SCHEDULE_EXACT_ALARM` with an in-context rationale
- Battery-optimization exemption request, shown **once**, in context, with a
  truthful explanation — and a visible "reminders may be delayed" state in
  Settings when the exemption is absent
- `ExistingPeriodicWorkPolicy.UPDATE` so schedule changes reach existing installs
- Reschedule on `BOOT_COMPLETED`, app update, and timezone change

**1b — Prove it**
- Unit tests: `ReminderCheckWorker` selection by status, `lastNotifiedAt`
  cooldowns (24h overdue, 3d due-soon), multi-vehicle, empty state, DST/timezone
- `WorkScheduler` scheduling and `BootReceiver` rescheduling
- Instrumented: the notification posts and deep-links to the correct task
- Device: survives reboot, force-stop, Doze, and — where obtainable — one
  aggressive-OEM device (Xiaomi/Realme are the realistic worst case)

**1c — Be honest when we cannot deliver**
If notifications are disabled, or the app is battery-restricted, Home says so
plainly. Never let the user believe they are covered when they are not.

*Rationale: this is the product. Everything else is packaging.*

### Step 2 — Close Billing 9
Branch: `migration/play-billing-9` (exists, gates green)
Needs `KEYSTORE_*` + `RELEASE_ADMOB_ID` → `clean assembleRelease bundleRelease` →
verify signed AAB → commit. **Deadline 2026-08-31.**

### Step 3 — Honest store surface
- Drop `es`/`pt-rBR` from `localeFilters` (B5)
- Reseed demo data (B8)
- Deploy privacy policy, link in Settings (B3)
- Data Safety form against real SDK behaviour (B4)

### Step 3b — Platform gates *(added on second audit)*
P1–P6 from §5.1b: verify 16 KB alignment, edge-to-edge, predictive back,
Crashlytics actually reporting, backup/restore of the Room database on a real
device transfer, and the notification-denied path. Un-gate data export.

### Step 4 — Accessibility blockers
Q1, Q2, Q3, Q4 — all small, all independently testable.

### Step 4b — Wire performance measurement *(added on second audit)*
Create the missing Baseline Profile producer module so the applied plugin does
something; add a macrobenchmark for cold start (target: first verdict < 800 ms
mid-range); add JankStats to catch real-device scroll regressions. Without this,
every performance claim in every document here is unmeasured.

### Step 5 — Home reference slice
Apply the blueprint's Home direction: sentence-first verdict (done), attention
count, BMW-style due list with date-**or**-distance phrasing, first-run checklist
for the empty-garage case.

### Step 6 — Wire what already exists
`RecordsTimelineCard` → Records. `FormSectionCard` → the six form screens.
No new components — consume the ones already built and paid for.

### Step 7 — Release candidate
Full gate per CLAUDE.md: clean `assembleRelease` + `bundleRelease`, R8 review,
device migration tests, billing purchase+restore via Play, UMP consent,
test ads, TalkBack, 2.0× font, dark/light, process-death restoration,
pre-launch report.

---

## 6b. Per-screen: the problem, and why ours is better

The first draft of this plan was process-heavy and never said what each screen is
*for*. A screen that doesn't solve a named problem better than the competition
shouldn't ship.

| Screen | Real user problem | Our answer | Competitor gap |
|---|---|---|---|
| **Home** | "Is there anything I need to deal with?" answered in under 3s | One sentence verdict + attention count. No score, no gauge, no analytics above the fold. | CARFAX/Drivvo open on a record list — the user must read and decide for themselves |
| **Vehicles** | "Which of my cars needs me?" | Status visible per vehicle in the list, sorted worst-first, readable with no photo | Most list cars alphabetically with no status signal |
| **Vehicle Detail** | "What's the state of this car, and what do I do next?" | Verdict → the one thing to start with → triaged list, date **or** distance | BMW does this well; consumer apps show a table |
| **Records** | "Prove what's been done" — at resale, at the counter | Flat chronological timeline, month headers, cost in mono | Drivvo is a spreadsheet; nobody optimises for showing it to another person |
| **Add Service** | Logging is a chore people abandon | ~15s: prefill date, prefill odometer, tap-not-type type grid, cost optional | Long forms with required fields are why maintenance logs die at entry #3 |
| **Add/Edit Reminder** | "I don't know what interval to use" | Seeded intervals from vehicle age and usage; user adjusts rather than invents | Most apps hand you an empty number field |
| **Mileage** | Reminders drift wrong if mileage is stale | 5-second update, quick-add chips, never fabricates a reading | Apps that assume mileage produce confidently wrong due dates |
| **Fuel** | Cost awareness without a hardware dongle | Manual log, honest averages, no invented predictions in v1.0 | Apps imply OBD-grade insight from typed numbers |
| **Settings** | "Will this actually remind me?" | Notification + battery-restriction state stated plainly (new, per §2) | Nobody tells the user their reminders are being throttled |
| **Pro** | "What am I paying for, exactly?" | Play-derived prices, visible restore, no fake savings or testimonials | Fake urgency and strikethrough anchors are the category norm |
| **Onboarding** | Empty app after signup = uninstall | Accountless; first vehicle in ~60s; seeded plan reveal is the aha | Account walls before any value — the single biggest drop-off cause |
| **Widget** | Glanceable truth on the home screen | Real attention count (fixed today — was a fabricated score) | Few competitors ship one at all |

---

## 7. What ships next (v1.1) — not now

In priority order, per `PRODUCT_STRATEGY_AUTOMINDER.md`, which is right about this:

1. **Quote Auditor v0** — paste/manual line items, rules-based, no AI, no OCR. The word-of-mouth feature.
2. **Mechanic Prep script** — pure templating from existing reminders.
3. **Seeded age/km templates** — day-1 value with zero history; fixes cold-start.
4. **"Don't re-buy" guard** — flag work already done inside its interval.
5. **Notification actions** — Done / Snooze inline.
6. Manrope font migration.
7. Translations (es, pt-BR) — properly, then re-enable locale filters.

**Deferred indefinitely:** OBD-II, VIN decode, cloud sync, receipt OCR, family
sharing, marketplace, social/gamification, chat-as-home. Each was proposed in at
least one document; none survives the "does it make the reminder arrive on time
and true?" test.

---

## 8. Standing rules

- **No fabricated data, ever.** An absent value and a zero value are different facts.
- **No advertised capability that doesn't exist** — in UI, store listing, or locale filters.
- **Status never by colour alone**, and never by shape.
- **Every bug fix gets a regression test.**
- **State what was not verified.** "Compiles" is not "works".
- **A spec citing external research must be checked before it is built on.** The
  "Technical Handoff" document attributed a bento-grid pattern to Mobbin; live
  Mobbin research showed the opposite. See the blueprint's authority note.

---

## 9. Honest risk register

| Risk | Why it matters | Mitigation |
|---|---|---|
| Reminder silently fails | The whole product is void, and the user doesn't know | Step 1, before anything else |
| Billing deadline missed | Cannot submit at all after 2026-08-31 | Step 2; keystore is the only blocker |
| Store claim mismatch (locales, Data Safety) | Policy rejection or removal | Steps 3 |
| Document drift returns | Already cost this project real time | §3 authority table; archive aggressively |
| Scope creep from polished specs | Three separate documents proposed OCR/charts during a feature freeze | §7 deferral list is binding |
| No visual verification | No AVD/adb available this session; UI changes are compile-verified only | Run `autominder-visual-qa` before release candidate |
