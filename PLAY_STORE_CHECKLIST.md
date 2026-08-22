# AutoMinder — Play Store Release Checklist
Audited 2026-07-03 against the actual codebase. ✅ verified in code · ⚠️ needs your action · ❌ gap.

## Build & signing
- ✅ targetSdk 36 / minSdk 26, Java 17, R8 full (`isMinifyEnabled` + `isShrinkResources`) with proguard-rules.pro
- ✅ Release signing from local.properties only (KEYSTORE_PATH/…); nothing in source
- ✅ Auto versionCode (YYMMDDHH) — unique per hour; versionName 1.0.0
- ✅ Lint gate: `abortOnError` + `warningsAsErrors` on release builds
- ✅ Locale filters en / es / pt-BR
- ⚠️ Build the store artifact with `./gradlew bundleRelease` (AAB, not APK) and keep the keystore + passwords backed up offline — losing them loses the app identity

## Ads (AdMob) & consent
- ✅ UMP consent flow gates Mobile Ads init (`canRequestAds()` before initialize)
- ✅ "Ad privacy options" re-prompt exposed in Settings
- ✅ Release build FAILS if no real AdMob app ID is set (production safety gate task)
- ✅ Debug uses Google test IDs; `ENABLE_ADS=false` in debug
- ⚠️ Before first release: set RELEASE_ADMOB_ID + the 4 unit IDs in local.properties/CI, and register the app in the AdMob console with the *final* package `com.autominder.app`
- ⚠️ In Play Console → App content: declare "Yes, contains ads"

## Subscriptions (Billing 7.1.1)
- ✅ SUBS + INAPP queried on connect; purchases acknowledged (required within 3 days or auto-refund)
- ⚠️ Create the subscription products in Play Console with IDs matching SubscriptionManager, activate them, and test with a license-tester account (products only resolve on a Play-signed build)

## Privacy & data safety
- ✅ Privacy policy hosted: https://com-autominder-app.web.app/privacy — verify it loads and names AdMob, Firebase Analytics, Crashlytics as third parties
- ✅ Minimal permissions (INTERNET, POST_NOTIFICATIONS, BOOT_COMPLETED, VIBRATE); photo picker avoids media permissions
- ✅ backup_rules + data_extraction_rules present; cleartext traffic off + network security config
- ⚠️ Play Console Data Safety form must declare: Analytics (Firebase), Crash logs (Crashlytics), Advertising ID (AdMob), and that vehicle data stays on-device

## Stability & performance
- ✅ Crashlytics + Performance Monitoring + Analytics wired
- ✅ LeakCanary debug-only; Timber logging; profileinstaller present
- ❌ Baseline Profile: plugin is applied but there is NO producer module (`settings.gradle.kts` includes only `:app`) — no profile is actually generated. Cold-start win still on the table; fine to ship 1.0 without it, queue for 1.1
- ✅ In-app review (throttled via ReviewHelper) + in-app update helper

## Store listing (nothing in-code — all yours)
- ⚠️ 512px icon, feature graphic 1024×500, ≥4 phone screenshots (use the Racing Teal brand system: DESIGN_SYSTEM.md / Figma file)
- ⚠️ Title ≤30 chars, short description ≤80, full description; content rating questionnaire; target-audience declaration (13+, not child-directed — required because ads)
- ⚠️ Start with a closed-testing track; Google requires 12 testers/14 days for new personal dev accounts — check whether TikiTaka3D account is affected

## Pre-submit smoke test (on device, release build)
1. Fresh install → onboarding → add car → permission → live dashboard
2. Log service with reminder toggle → reminder appears; log fuel with empty cost
3. Kill app, reboot device → BootReceiver reschedules; notification fires
4. Consent form appears in an EEA-locale emulator; ads load after consent
5. Purchase + cancel a test subscription; Pro gates unlock/relock
