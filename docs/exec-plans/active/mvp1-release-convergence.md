# AutoMinder MVP 1 — Active Release Convergence Plan

Updated: 2026-08-14 (Asia/Karachi)

Status: **ACTIVE — NOT READY**

Objective: ship the smallest trustworthy AutoMinder 1.0 release without data loss, unsupported product claims, billing-policy failure, or avoidable accessibility defects.

## Current evidence

- Branch: `feat/ui-log-service-night-garage`
- Baseline HEAD: `4bcfb5f1df16746f8de3d4c6846909309284533d`
- User-owned WIP: four sentence-case edits in `strings.xml`; untracked `.mcp.json`, `HANDOFF_SESSION_2026-08-04.md`, and `scripts/stitch-proxy.mjs`
- 2026-08-14 hardening gate: `compileDebugKotlin`, `assembleDebug`, `testDebugUnitTest`, and `lintDebug` passed; 70 JVM tests passed, 0 failed/skipped; lint reported 0 issues
- Instrumented/device/Play tests: not run in this analysis
- Existing release AAB/APK and R8 mapping were generated earlier; AAB signature verifies, but the release Gradle exit, Play upload, Play App Signing, device behavior, and license testing are not established
- Play Billing: 7.1.1; official deadline 2026-08-31, extension 2026-11-01
- Target SDK: 36; compliant with the 2026-08-31 target API requirement
- Room: schema version 2 with exported schemas 1 and 2

## Scope rules

- v1.0 is feature-frozen. Only release, correctness, trust, accessibility, localization, and bounded design-system convergence work is active.
- Billing migration is isolated from UI, Ads, Room, navigation, and release-workflow changes.
- Existing WIP is never stashed, committed, reverted, or absorbed without owner direction.
- Counter Mode, Quote Auditor, accounts, cloud sync, OBD, VIN lookup, OCR, family sharing, and fuel-intelligence expansion remain backlog.
- Each task below is a separate reviewed patch unless an explicit owner decision combines them.

## Dependency order

```text
R0 WIP decision
  -> R1 Play Billing 9.1.0
      -> R2 billing license-test evidence

R0 WIP decision
  -> T1 consent-safe ad rendering
      -> T2 monetization truth and copy

R0 WIP decision
  -> U1 maintenance-truth language/score removal
      -> U2 semantic status components
          -> U3 loading and tonal-surface foundation
              -> U4 Home reference slice

R1 + R2 + T1 + T2 + U1-U4
  -> Q1 localization/accessibility/device matrix
      -> Q2 release artifact and Play submission gate
```

## Bounded tasks

### R0 — Reconcile user-owned WIP

Goal: establish a recoverable base without hiding or discarding current work.

Acceptance:

- Owner decides whether the four Add Service sentence-case edits should be committed.
- Each untracked file is reviewed separately; `.mcp.json` is never committed without a secret/portability review.
- The intended Billing migration base commit is named.

### R1 — Migrate Play Billing 7.1.1 to 9.1.0

Goal: remove the submission blocker while preserving all three products and entitlement behavior.

Expected scope: `gradle/libs.versions.toml`, `SubscriptionManager.kt`, billing-focused tests only.

Acceptance:

- PBL 9.1.0 is the only dependency change.
- PBL 9 product-query result/unfetched-product API is handled.
- Automatic service reconnection replaces conflicting manual retry behavior.
- Monthly/yearly/lifetime IDs, Play prices, restore, acknowledgement, cached entitlement, and PURCHASED-vs-PENDING behavior are preserved.
- `ITEM_ALREADY_OWNED` is not reported as success until entitlement is confirmed.
- Billing debug messages and purchase-sensitive data do not enter production Crashlytics logs.
- Billing unit/regression tests cover purchased, pending, cancelled, already-owned, acknowledgement, partial query failure, restore, and cached entitlement.
- Quick and hardening Gradle gates pass; release compile/bundle exit is captured.

### R2 — Record Play Billing license-test evidence

Goal: prove behavior that source and JVM tests cannot prove.

Acceptance: Play-signed license-tester evidence for monthly, yearly, lifetime, cancel, pending-to-purchased, restore, already-owned, offline cached entitlement, reconnect, and entitlement removal after complete Play evidence.

### T1 — Gate every ad request on current UMP authorization

Goal: ensure no banner or preloaded ad request can occur before `canRequestAds()` is true.

Expected scope: Ads/consent state plus the smallest hosting change needed to render eligible placements; no Billing or design-system changes.

Acceptance:

- Consent info refreshes every launch.
- Banner creation/loading is impossible until UMP permits requests.
- Pro immediately suppresses all ads.
- Home, onboarding, forms, reminder resolution, service entry, dialogs, and billing remain ad-free.
- Free core behavior is unchanged when consent is denied or the network is unavailable.
- EEA required/not-required/denied/privacy-options and test-ID states are verified.

### T2 — Reconcile monetization behavior, paywall, listing, and privacy truth

Goal: ensure every user-facing and Play-facing claim matches code.

Owner decisions required:

- Keep CSV export free, or genuinely gate it as Pro.
- Keep monthly/yearly/lifetime products active in Play, or approve a different catalog later.
- Ship English-only or complete Spanish and Portuguese-Brazil localization.

Acceptance:

- Remove Pro cloud-backup claims; Android Auto Backup is platform behavior available to all users, not a Pro feature.
- Remove PDF claims unless PDF export exists and is tested.
- Paywall represents loading, available, unavailable, pending, cancelled, and restore states distinctly.
- Privacy policy and Data Safety disclose Firebase Analytics, Crashlytics, Performance Monitoring, AdMob/Advertising ID, SDK-added ad-services permissions, and Android backup behavior accurately.
- Store listing contains no health-score, guaranteed timing, fake privacy, unsupported prediction, sub-30-second, battery, or Counter Mode claims.

### U1 — Replace fabricated health language and scores with maintenance truth

Goal: present only reminder/odometer-derived maintenance status.

Expected surfaces: Home, Vehicle Detail, widget, strings, store metadata, and focused tests.

Acceptance:

- No `health score`, `vehicle health`, `safe`, `condition`, or `all systems good` claims.
- UNKNOWN never contributes as a perfect score.
- Home answers maintenance attention, reason, and next action in plain language.
- Widget shows status/count/next reminder rather than a synthetic 0–100 score.
- No new domain states are introduced.

### U2 — Consolidate semantic status presentation

Goal: one stable ReminderCard geometry with status expressed through label, icon, tone, rail/border, and explanation.

Acceptance:

- Domain states remain OVERDUE, DUE_SOON, SNOOZED, OK, COMPLETED, UNKNOWN.
- Status-driven corner morphing is removed.
- `StatusChip` and `PremiumStatusStyle` no longer diverge.
- Status is understandable without color and has correct TalkBack state text.
- Bottom navigation gains filled-selected/outlined-unselected icons in a separately reviewable change if needed.

### U3 — Correct loading, motion, and tonal surfaces

Goal: enforce the existing design-system rules before more screen work.

Acceptance:

- Remove the prohibited `animateFloat()` shimmer and infinite translating gradient.
- Content-shaped loading respects reduced motion and avoids bare spinners.
- Dark-theme hierarchy uses tonal surfaces instead of routine card shadows.
- Existing duplicated/unused components are consolidated or deleted only after usage verification.

### U4 — Home reference vertical slice

Goal: make Home the validated reference for later screen convergence.

Acceptance:

- Vehicle context -> maintenance summary -> urgent reason -> next action -> upcoming -> useful context.
- No naked score, fake diagnostic confidence, or Home banner.
- Empty/error/loading/populated, one/multiple vehicle, no-photo, light/dark, short-screen, landscape, and 200% font states are captured and reviewed.
- TalkBack order, back handling for quick actions, and 48dp targets pass.

### Q1 — Localization, accessibility, lifecycle, and device matrix

Goal: prove core flows outside source inspection.

Acceptance:

- Owner-approved locale scope is complete and tested with long strings.
- TalkBack, 200% font, reduced motion, light/dark, compact/large/landscape/split-screen, IME, process death, lock/unlock, rapid taps, permission denial, and offline states pass.
- Room 1->2 migration and atomic service-completion instrumented tests pass on device/emulator.
- Reminder scheduling, actions, reboot rescheduling, widget, and notification permission states pass.

### Q2 — Release artifact and Play gate

Goal: produce the evidence-backed release candidate.

Acceptance:

- Clean release build, release lint, tests, R8/missing-rules review, signed AAB verification, and reproducible artifact record pass.
- Play App Signing/upload, product configuration, license testing, UMP regional tests, test ads, Data Safety, ads declaration, content rating, privacy URL, store assets, closed-testing requirements, and pre-launch report are recorded.
- Final verdict is only READY, READY WITH ACCEPTED RISKS, or NOT READY.

## Backlog, not active scope

- Counter Mode / Quote Auditor / Mechanic Prep validation and v1.1 product design
- Optional encrypted account and cross-device sync
- VIN lookup, OBD-II, GPS/geofencing, OCR, family sharing
- Fuel-intelligence expansion and prediction claims
- Baseline Profile producer module and measured macrobenchmark program
- Manrope migration pending asset/source/license proof and English/es/pt-BR/200% font evidence
- AdMob v23 migration to a supported major version; v23 is deprecated but not sunset until 2027-06-30

## Evidence log

Update this section after each bounded task with command outcomes, device/build identifiers, screenshots where visual, Play test account category (never credentials), and explicit NOT VERIFIED items.
