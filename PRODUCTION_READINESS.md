# AutoMinder — Production Readiness Plan (Play Store v1.0)

> Audit date: 2026-06-12 · Branch: sprint/1-stability · Auditor: Claude Code
> Goal: ship to Play production with premium UX, hardened security, and zero policy rejections.

---

## AUDIT FINDINGS

### 🔴 P0 — Launch blockers (policy rejection / data loss / revenue loss)

| # | Finding | Risk | Fix |
|---|---------|------|-----|
| 1 | **`USE_EXACT_ALARM` declared but unused** (no AlarmManager anywhere — app uses WorkManager). This permission is policy-restricted to alarm/calendar apps. | **Play rejection** | Remove it + `SCHEDULE_EXACT_ALARM` |
| 2 | **`CAMERA` + `READ_MEDIA_IMAGES` declared but unused** (photo picker `PickVisualMedia` needs neither) | Play review flag, scary install dialog | Remove both |
| 3 | **`FOREGROUND_SERVICE` declared but no service exists** | Play review flag | Remove |
| 4 | **Backup rules exist but are NOT referenced in the manifest** — `allowBackup=true` with defaults. Worse: the rule files *exclude* the Room DB and prefs, which would mean **new phone = all records lost** (the exact Simply Auto disaster our marketing attacks) | Data-loss trust killer | Reference `fullBackupContent` + `dataExtractionRules` in manifest; rules must **include** the database (nothing secret is stored locally) |
| 5 | **No AdMob UMP consent flow** — serving ads in the EEA/UK without GDPR consent violates Google's EU User Consent Policy | **Ad serving blocked / account risk** | Add `user-messaging-platform`, gather consent before `MobileAds.initialize()` |
| 6 | **Billing: existing unacknowledged purchases never acknowledged** — `queryExistingPurchases()` doesn't call acknowledge. If the app dies between purchase and ack, Google **auto-refunds in 3 days** | Silent revenue loss | Acknowledge in the existing-purchases path too |
| 7 | **Pro entitlement not persisted** — cold start offline ⇒ billing can't connect ⇒ paying user sees locked features + ads | Pro users feel scammed | Cache entitlement in DataStore, load at startup, reconcile when billing connects |
| 8 | **No crash reporting** — release goes out blind | Can't fix what you can't see | Add Firebase Crashlytics (google-services.json already present) |
| 9 | **Release build (R8/minify) never verified** — proguard rules exist but `assembleRelease` hasn't been proven | Crash-on-launch in prod | Run + smoke-test the minified build before every release |

### 🟠 P1 — Required for a quality launch (week of release)

10. **Billing reconnect**: `onBillingServiceDisconnected` just logs — add exponential-backoff retry.
11. **versionCode strategy**: currently `1`; adopt `versionCode = epoch-day` or manual bump checklist; release as **AAB** (`bundleRelease`), enroll in **Play App Signing**.
12. **Privacy policy must be live** at https://autominder.app/privacy before submission (untracked `website/` folder — deploy it). Required by both Play and AdMob.
13. **Play Data Safety form**: declare AdMob (device IDs, ad interactions), Billing; all vehicle data is on-device only — that's the headline.
14. **Pre-launch report pass**: upload to internal testing, fix anything the robo-crawl finds.
15. **`lintRelease` clean**: run and triage; add `lint { abortOnError = true }` once clean.
16. **Notification permission UX**: POST_NOTIFICATIONS is requested in Settings — also ask contextually after the user creates their first reminder (that's the moment of intent; 70%+ grant rates vs cold asks).
17. **Store listing assets**: feature graphic (1024×500), 8 phone screenshots showing health score / charts / swipe / dark mode, promo text with ASO keywords from the research docs.

### 🟡 P2 — Fast follow (v1.0.x)

18. Baseline Profile for startup performance (measurable on Pixel-class devices).
19. In-app review prompt (`ReviewManager`) after 3rd successful service log — never after an error.
20. In-app update prompt (`AppUpdateManager`, flexible mode).
21. ViewModel unit tests (only `StatusCalculatorTest` + `ValidatorsTest` exist today); priority: VehicleDetailViewModel (recurring reminders), SubscriptionManager state machine.
22. Accessibility pass: TalkBack walk-through, 48dp touch targets, contentDescription audit (swipe actions need custom accessibility actions).
23. `localeConfig` + first translations (ES/PT-BR — largest car-app markets after EN).
24. Shared-element Dashboard→Detail transition (deferred from UX sprint; needs device tuning).

### ✅ Already production-grade (verified in audit)

- Signing/AdMob secrets sourced from `local.properties`/env only; release build fails loudly if IDs missing; debug uses Google test IDs.
- R8 minify + resource shrink ON for release, sensible keep rules for Room/Hilt/serialization/billing/Glance.
- Room `exportSchema=true` with schemas v1+v2 committed; no destructive migrations.
- WorkManager + Hilt manual init correct; BootReceiver properly permission-guarded; FileProvider not exported.
- Billing v7: pending purchases enabled, acknowledge on new purchase, SUBS + INAPP (lifetime) handled.
- Predictive back enabled; RTL supported; Timber only plants in debug; zero TODO/FIXME left in source.
- All-screen premium motion layer, scroll behaviors, haptics, gestures (this sprint).

---

## EXECUTION PLAN

### Sprint A — "Compliance & Trust" (code-side P0, no external accounts needed)
1. Manifest: strip 5 unused permissions; wire `fullBackupContent` + `dataExtractionRules`.
2. Backup rules: include DB + DataStore (user's records survive device moves — becomes a marketing line).
3. SubscriptionManager: acknowledge-on-query, DataStore entitlement cache, reconnect backoff.
4. UMP consent gate before ads init in `AutoMinderApp`/`MainActivity`.
5. Gate: `compileDebugKotlin` + `assembleRelease` (verify R8) → commit per slice.

### Sprint B — "Eyes & Ears" (needs Firebase/Play consoles)
6. Crashlytics + (optional) Analytics for funnel events: vehicle_added, reminder_created, trial_started, purchase.
7. Internal testing track upload → pre-launch report → fix findings.
8. Data Safety form + privacy policy deployment.

### Sprint C — "Launch Quality"
9. lintRelease clean, in-app review + update prompts, contextual notification ask.
10. Store assets + listing copy (ASO from research docs).
11. Staged rollout: 10% → 50% → 100%, watching Crashlytics + vitals (ANR < 0.47%, crash < 1.09% — Play's bad-behavior thresholds).

### Release checklist (every release)
- [ ] versionCode bumped, CHANGELOG updated
- [ ] `bundleRelease` green, minified build smoke-tested on device (cold start, add vehicle, purchase flow with license tester)
- [ ] Room schema diff reviewed; migration written if entities changed
- [ ] Crashlytics dashboard clean from previous rollout
