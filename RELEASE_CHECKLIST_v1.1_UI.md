# AutoMinder — UI Production-Readiness Checklist
Updated 2026-07-17 · Companion to `UI_AUDIT_2026.md` · Branch `sprint/1-stability`

## 1. Verify this session's changes (REQUIRED before anything ships)
Commit `c57554c` touched 7 files (paywall, dashboard, empty state, reminder card, records/fuel cards, strings). Run the release-hardening gate:

```
./gradlew clean assembleDebug
./gradlew testDebugUnitTest
./gradlew lintDebug
```

Manual smoke test: open paywall from Settings (plan cards select, Continue launches correct product, TalkBack reads "Included/Not included"); Dashboard FAB menu (back gesture closes it, tapping scrim closes it); Records + Fuel history (costs render in JetBrains Mono).

Lint note: `paywall_yearly_label`, `paywall_monthly_label`, `paywall_lifetime_label` are now unused strings — safe to delete once the gate is green.

## 2. Remaining P1 UI work (code, needs its own sessions)
- [x] Skeletons: Vehicles, Vehicle Detail, Service Detail (`2b57622`)
- [x] Add/Edit Reminder → `ServiceTypeGrid` (`2b57622`)
- [x] Settings decorative-icon a11y + single-target upgrade card (`2b57622`)
- [x] Off-token corners 14dp → `shapes.medium` (`2b57622`)
- [ ] Vehicles tab → `VehicleHeroCard(Compact)` + `StatusChip` (needs status in `VehicleListViewModel`)
- [ ] Records → wire `RecordsTimelineCard` + `ServiceTypeIcons`
- [ ] `FormSectionCard` sectioning on the 6 form screens
- [ ] Settings grouped-card sections; optional dynamic color toggle
- [ ] Shared `InfoBanner`; onboarding TalkBack liveRegion (needs on-device TalkBack verification)

## 3. Figma file (design source of truth)
https://www.figma.com/design/NkQV8bOTxpHa36QCbGZmNT — 16 screens done.
Blocked on Figma Starter-plan MCP rate limit. Remaining: collapse ~20 oversized spacer frames (Service Detail rows / paywall table look stretched), About screen, Edit-variant frames. Resume when the limit resets or after a plan upgrade.

## 4. Play Store submission gates (from CLAUDE.md)
- [ ] Release-candidate gate: `./gradlew assembleRelease && ./gradlew bundleRelease`
- [ ] AdMob IDs from `local.properties` / CI env only (never committed) — pattern already in build files
- [ ] Signing via `KEYSTORE_PATH` + passwords in `local.properties` only
- [ ] UMP consent flow verified for EEA (privacy options entry appears in Settings)
- [ ] Billing 7.1.1 stays pinned; Billing 8+ upgrade is its own pre-Aug-2026 task
- [ ] Store listing: screenshots — consider exporting from the new Figma frames for visual consistency
- [ ] Data safety form matches actual collection (offline-first, no cloud sync in v1.0/1.1)

## 5. Session log
- `2996840` — checkpoint (pre-existing WIP + UI audit)
- `c57554c` — P0 fixes: paywall plan cards + a11y, contrast tokens, FAB scrim/back, mono numerals
