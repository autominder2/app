# 🚀 Milevora 1.0.0 (RC-1) Release Checklist & Launch Calendar
**App Identity:** Milevora (`Milevora: Car Maintenance`)  
**Target Track:** Google Play Console — Direct-to-Production Fast-Path (Established Developer Account)  
**Package:** `com.autominder.app` | **Version Name:** `1.0.0` | **Version Code:** `27000001`  
**Target SDK:** 36 | **Min SDK:** 26 | **Java Target:** 17  
**Date:** August 2026

---

## ⚡ Direct-to-Production Fast-Path (Bypassing 20-Tester Closed Beta)

Because you own an **established Google Play Developer account** (created prior to November 2023), Google's mandatory "20 testers for 14 days" requirement does **not** apply. You can publish directly to Production!

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    ACCELERATED 3-DAY PRODUCTION LAUNCH                      │
│                                                                             │
│  STEP 1: Generate Release AAB (`.\gradlew.bat bundleRelease`)               │
│          • Produces verified, R8-shrunk `app-release.aab` (15.6 MB).        │
│                                                                             │
│  STEP 2: Instant Internal Test Link (15-Minute Hardware Check)              │
│          • Upload to Internal Testing track for private download link.      │
│          • Open app on your physical phone, tap "Upgrade Pro", test Billing. │
│                                                                             │
│  STEP 3: Fill Play Console Data Safety & Store Listing                      │
│          • Copy exact answers from `docs/release/PLAY_SUBMISSION_NOTES.md`. │
│          • Paste Store Copy from `store/STORE_LISTING.md`.                  │
│                                                                             │
│  STEP 4: Promote to Production Track & Submit for Review                    │
│          • Hit "Start rollout to Production" (Approval in 24–48 hours).     │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## ✅ Pre-Submission Verification Gate Checklist

### 1. Build & Compilation Verification
- [x] All 244 unit tests passing (`testDebugUnitTest` 100% green).
- [x] Zero Lint errors (`lintDebug` / `lintRelease` passing).
- [x] Kotlin compilation clean (`compileDebugKotlin` / `compileReleaseKotlin`).
- [x] Version code strictly set to `27000001` (checked-in, deterministic).
- [x] `minSdk = 26`, `targetSdk = 36` aligned with Android 16 standards.

### 2. Security & Credentials Verification
- [x] Zero hardcoded API keys, tokens, or plaintext secrets in source code.
- [x] `.gitignore` isolates `local.properties`, `*.keystore`, `*.jks`, `google-services.json`.
- [x] `requireReleaseInputs` gate configured in `build.gradle.kts` to block builds missing real signing keys or real AdMob IDs.
- [x] ProGuard `-assumenosideeffects` physically strips `Timber.d/v/i` from release bytecode.
- [x] Sentry screenshots and view-hierarchy serialization disabled (`attach-screenshot=false`, `attach-view-hierarchy=false`).
- [x] `SensitiveDataLeakTest.kt` permanent regression guard active.

### 3. Permissions & Privacy Verification
- [x] Permissions minimized to 4 safe items: `INTERNET`, `POST_NOTIFICATIONS`, `RECEIVE_BOOT_COMPLETED`, `VIBRATE`.
- [x] Zero dangerous permissions (no Camera, Location, Contacts, Microphone, Storage).
- [x] Photo attachments use the Android system Photo Picker exclusively.
- [x] `entitlement_cache.preferences_pb` excluded from cloud backup and device transfer in `backup_rules.xml` and `data_extraction_rules.xml`.
- [x] Data Safety declares Advertising ID (`AD_ID` via AdMob) and Crash/Perf Diagnostics.

### 4. Billing & Monetization Verification
- [x] Google Play Billing 9.1.0 active.
- [x] Product IDs mapped in Play Console: `autominder_pro_monthly`, `autominder_pro_yearly`, `autominder_pro_lifetime`.
- [x] Proactive purchase acknowledgment implemented to prevent 3-day auto-refunds.
- [x] Safe anti-downgrade logic prevents accidental loss of Pro entitlement on network timeout.
- [x] Restore purchases entry point accessible from Settings and Paywall.

### 5. UI/UX & Flow Verification
- [x] Universal 4-state coverage (Loading, Empty, Error, Success) on all 15 screens.
- [x] Zero dead placeholder screens or unhandled `TODO` click listeners.
- [x] Touch targets compliant with $\ge 48\text{dp}$ accessibility guidelines.
- [x] Material 3 Dark and Light themes fully styled with custom Obsidian & Platinum tokens.
