# AutoMinder - Play Store Release Checklist

Audited 2026-08-23 against the actual codebase and current market/release plan.

Legend: VERIFIED = checked in code; ACTION = owner/device/Console work; GAP = release blocker or claim mismatch.

## Build & Signing

- VERIFIED: targetSdk 36 / minSdk 26, Java 17, R8 full release config, release lint gates.
- VERIFIED: release signing is sourced from local properties / CI inputs only; signing material stays out of source.
- ACTION: Build the store artifact with `./gradlew bundleRelease` and keep keystore/password backup offline.

## Ads & Consent

- VERIFIED: UMP consent flow exists and Settings exposes ad privacy options.
- ACTION: Verify every actual ad request is blocked until UMP `canRequestAds()` is true.
- ACTION: Set release AdMob app ID and unit IDs in local/CI secrets, then register package `com.autominder.app` in AdMob.
- ACTION: In Play Console App Content, declare that the app contains ads.

## Billing

- GAP: Play Billing is still 7.1.1. Migrate to 9.1.0 before production submission unless an approved extension path is used.
- VERIFIED: current code queries SUBS and INAPP products and acknowledges purchases.
- ACTION: Decide whether v1.0 keeps monthly/yearly/lifetime products or changes catalog; update code, copy, and Play Console together.
- ACTION: License-test purchase, cancel, pending, already-owned, restore, cached entitlement, reconnect, and entitlement removal on a Play-signed build.

## Privacy & Data Safety

- VERIFIED: minimal explicit permissions, backup rules, data extraction rules, cleartext off, network security config present.
- ACTION: Data Safety must disclose Firebase Analytics, Crashlytics, Performance Monitoring, AdMob/Advertising ID, Billing, and Android backup behavior.
- ACTION: Privacy policy must match real SDK behavior and must not imply zero collection while Firebase/AdMob/Billing are present.

## Stability & Performance

- VERIFIED: Crashlytics, Performance Monitoring, Analytics, debug-only LeakCanary, Timber, and profileinstaller are wired.
- VERIFIED: Baseline Profile producer module is present and consumed by `:app`.
- ACTION: Record Baseline Profile generation and device performance evidence if it remains part of release confidence.

## Store Listing & Claim Truth

- ACTION: Prepare 512 px icon, 1024 x 500 feature graphic, at least 4 phone screenshots, title <= 30 chars, short description <= 80 chars, content rating, and target audience declaration.
- GAP: Do not submit copy that claims Fleet Health Score, vehicle health score, cloud sync, PDF export, 7-day trial, guaranteed timing, unsupported prediction, Quote Auditor, OCR, AI, VIN lookup, recalls, diagnostics, or family sharing unless matching tested code and Play product evidence exist.
- ACTION: Start with closed testing. Confirm whether the TikiTaka3D account is subject to Google Play's 12-tester / 14-day requirement for new personal developer accounts.

## Pre-Submit Smoke Test

1. Fresh install -> onboarding -> add car -> permission -> live dashboard.
2. Log service with reminder toggle -> reminder appears; log fuel with empty cost.
3. Kill app, reboot device -> worker reschedules; notification behavior is verified.
4. EEA consent path -> ads load only after consent permits requests.
5. Purchase/cancel configured test products -> Pro unlocks/relocks only after Play-confirmed entitlement.
