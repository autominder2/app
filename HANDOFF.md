# AutoMinder — Session Handoff
**Date:** 2026-07-07 · **Branch:** `sprint/1-stability` · **Tree:** clean · **All gates:** GREEN
**Read with:** `CLAUDE.md` (governance, always wins) · `.claude/skills/` (procedures) · this file (state)

---

## 1. What this project is

AutoMinder — Android car maintenance & fuel intelligence app.
- Package: `com.autominder.app` — **FINAL, never change**
- Bottom nav: `Home | Vehicles | Records | Settings` — never rename Records
- Phase: **v1.0 release hardening, FEATURE-FROZEN** (no new user-facing features; v1.1+ list in CLAUDE.md)
- Stack: Kotlin 2.1.21 · Compose BOM 2025.06.01 (M3, compose-ui 1.8.3) · Hilt/KSP · Room · Billing 7.1.1 (pinned) · minSdk 26 / targetSdk 36
- Brand: Racing Teal `#006B5F` · Exo 2 (display) / Nunito Sans (body) / JetBrains Mono (data) · status corners OVERDUE=8dp / DUE_SOON=16dp / GOOD=28dp

## 2. Design contract (binding for every UI slice)

The 12 "AutoMinder 2026 Premium UI Rules" — full text in `.claude/skills/autominder-ui-slice/SKILL.md`. Short form:
truth before beauty · one visual anchor per screen · no equal-weight layouts · tonal layering (surface → surfaceContainerLow → surfaceContainer → surfaceContainerHigh) · status never color-only (text+chip+color+rail+corner) · vehicle identity is emotional · premium formatted numbers (Mono) · guided sectioned forms · motion after hierarchy · perf is design (stable keys, remembered formatting, no infinite animation) · accessibility mandatory · **no feature creep**.

Fleet Behance case study (behance.net/gallery/250251481) is the storytelling reference — borrow narrative/hero/metric-moment logic, never its yellow/purple palette or fake content. "Digital glovebox" is a **future** brand concept; Records keeps its name in v1.0.

## 3. Session commit log (all green, newest first)

| Commit | What |
|---|---|
| `ae337b5` | **Slice 4** — Vehicle Detail command center (hero once, Mono odometer instrument, diagnosis card, PremiumActionGrid, triaged reminders + Show-all expander, Export/Archive → overflow, HealthSetupCard CTA fixed) |
| `2f4dc50` | **Slice 3** — Dashboard health cockpit (verdict headline via plurals, demoted score ring, metric cards, needs-attention cards from existing use-case data, VehicleHeroCard rows, car icon not bus) |
| `4c7b1d9` | **Slice 2** — dormant premium component kit (12 files, `ui/components/premium/`) |
| `38c6090` | docs: marketing/privacy website (`website/`, Firebase; privacy URL = Play prerequisite) |
| `a7bac8d` | docs: Play Store release checklist |
| `4a5a530` | docs: CLAUDE.md v5.0 release-hardening governance |
| `9b723ca` | **Slice 1A.2** — last `ServiceType.label` sites localized (ServiceDetail, Records card, EditReminder ×2, chart legend via TypeSpend identity refactor + render-time localization) |
| `293cfa2` | **Slices 1A+1A.1** — visual truth cleanup (surfaceContainer tokens wired into both ColorSchemes, year-0 hidden, `DistanceFormat.grouped()` everywhere, localized status labels, honest StatusChip, overdue-by-mileage timing leads, dead Pro CTAs → snackbar interim, paywall Free/Pro headers, unshipped-widgets row removed) |
| `5b8cac4` | Claude Code skill stack (8 skills + 4 agents, `.claude/` un-gitignored for skills/agents only) |

Deleted (false-claim stubs, per user decision): `CLAUDE_CODE_BRIEFING.md`, `STRATEGY_2026.md`.

## 4. Pipeline status

```
✅ Slice 0    Visual QA audit (evidence in D:\tmp\autominder-qa\)
✅ Slice 1A   Truth cleanup            ✅ 1A.1  Labels/sheet timing   ✅ 1A.2  Last labels
✅ Slice 2    Premium component kit    ✅ Slice 3  Dashboard cockpit
✅ Slice 4    Vehicle Detail command center
⏭ Slice 4.1  (micro, AWAITING YES/NO) NavGraph paywall wiring — Pro-gate CTA on VehicleDetail
             is still a snackbar hint; real navigation needs NavGraph.kt + one callback param
⏭ Slice 5    Records premium timeline (RecordsTimelineCard; ServiceHistoryScreen.kt + strings)
⏭ Slice 6    Guided forms (AddReminder/EditReminder ← ServiceTypeGrid + FormSectionCard;
             section-wrap AddService/AddFuel/MileageLog; fix discard-guard predicate)
⏭ Slice 7    Settings + Paywall polish (PremiumPaywallPlanCard + PremiumPriceDisplay states,
             billing-unavailable timeout copy, grouped settings cards)
⏭ Slice 8    Final QA + nit list (see §7)
```

Approved commit messages: Slice 5 `feat(ui): redesign records as premium timeline` · 6 `feat(ui): redesign forms as guided workflows` · 7 `feat(ui): polish settings and pro paywall` · 8 `chore(ui): final visual QA pass for v1.0`.

## 5. Component kit (`ui/components/premium/`) — consume, don't reinvent

`VehicleHeroCard` (Compact/Expanded) · `HealthCockpitCard` (verdict headline + demoted ring; never a lone number) · `InsightMetricCard`+`InsightMetricRow` · `ProactiveAttentionCard` · `StatusReminderCard` (corner morph + rail + timing pair) · `RecordsTimelineCard` (for Slice 5) · `FormSectionCard` (for Slice 6) · `PremiumSectionHeader` · `PremiumActionGrid` · `PremiumPaywallPlanCard` + `PremiumPriceDisplay` sealed (Loading/Available/Unavailable — for Slice 7) · `PremiumStatusStyle` (THE single status-style source).

Rules: dumb/presentational, caller-localized text, pre-formatted values (`DistanceFormat.grouped()` at caller), status chips delegate to existing `StatusChip`, all animation via `Motion.kt`.

Key utilities: `ui/util/DistanceFormat.kt` (locale grouping) · `ui/util/ServiceTypeLabel.kt` (`ServiceType.localizedLabel()` — **never render `.label`**) · `ui/theme/Motion.kt` (springs + `reduceMotion`).

## 6. Environment recipes (hard-won — do not relearn)

- **Gradle (Windows, mandatory):** `$env:_JAVA_OPTIONS = "-Djdk.net.unixdomain.tmpdir=C:\tmp -Djava.net.preferIPv4Stack=true"` then `gradlew.bat <task> --no-configuration-cache --no-daemon` redirected to a temp log; trust `$LASTEXITCODE`, ignore the `_JAVA_OPTIONS` stderr false-error. Gate = clean assembleDebug + testDebugUnitTest + lintDebug.
- **Lint enforces UnusedResources and PluralsCandidate** (no `%d` followed by words — reorder phrase to end with the number). Removing a string's last usage without deleting the string FAILS lint.
- **Known pre-existing warnings** (never report as new): SubscriptionManager SERVICE_TIMEOUT, UpdateHelper deprecation, FuelHistoryViewModel opt-in, VehicleDetailViewModel unchecked casts ×4.
- **Emulator:** AVD `Medium_Phone_API_36.1` (1080×2400). Launch app with `am start -n com.autominder.app/.MainActivity` — **never `monkey` LAUNCHER** (LeakCanary trap). Screencap via `cmd /c "adb exec-out screencap -p > file"` (PS 5.1 `>` corrupts binary). Wait ~8-14s for splash. Nav taps: Home(126,2225) Vehicles(400,2225) Records(676,2225) Settings(952,2225). Always record→change→capture→restore→**verify** for `cmd uimode night` and `settings system font_scale`. Screenshots → `D:\tmp\autominder-qa\`.
- **File-editing trap:** PowerShell `Get-Content`/`Set-Content` on UTF-8 Kotlin files mojibakes em-dashes and adds a BOM — prefer the Edit tool; if bulk line surgery is unavoidable, repair with `sed 's/â€”/—/g'` and strip BOM.
- **`remember{}` cannot be called inside `LazyListScope` builders** — compute in composable scope above the LazyColumn.

## 7. Slice 8 nit list (accumulated, do NOT fix early)

1. Hide "Due soon 0" zero-value metric on Dashboard
2. Tighten StatusReminderCard/ProactiveAttentionCard vertical padding under the CTA row
3. Greeting copy contradiction: "Good afternoon — everything under control" next to an OVERDUE cockpit (`dashboard_greeting_*` strings)
4. Delete now-dead `ui/components/FleetHealthScore.kt` (orphaned by Slice 3)
5. 2.0x font-scale sweep (only 1.5x verified so far) + TalkBack pass + reduced-motion pass
6. Release-build (R8) scroll-jank check
7. **Demo/seed data quality:** "Overdue by 173,000 km" is truthful but absurd for Play screenshots — reseed sensible demo data before store captures
8. `website/.firebase/*.cache` committed — add `.gitignore` line, `git rm --cached`
9. Consider Quick Add FAB overlap with last list items on short screens (spacers exist; verify at 2.0x)

## 8. Beyond UI (release backlog)

Play Console setup + store listing + Data Safety form (use `/play-store-readiness` skill) · privacy policy URL live (website committed; needs Firebase deploy + URL in Settings/listing) · closed-testing AAB via release-candidate gate (`assembleRelease` + `bundleRelease`) · **Billing 8 upgrade** = separate pre-~Aug-2026 task, never bundled.

## 9. How to run the next slice

New session: skills auto-register. Use:
```
/autominder-ui-slice
APPROVED SLICE 5 ONLY — RECORDS PREMIUM TIMELINE.
[allowed: ServiceHistoryScreen.kt, strings.xml, premium/* API-fit only]
```
Then after green: `/autominder-visual-qa`, `/compose-performance-guardian`, `/accessibility-qa`, `/ui-diff-review`, commit with the approved message. Full slice prompts live in the conversation history and follow the same shape; the skill enforces the rest.

**Open decisions for the user:** (a) Slice 4.1 NavGraph paywall wiring — yes/no; (b) demo-data reseed timing (before Slice 8 screenshots).
