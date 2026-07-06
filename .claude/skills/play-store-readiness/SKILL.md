---
description: Use to audit AutoMinder Play Store readiness - package identity, release build, signing, Billing, Ads/UMP, privacy, Data Safety, screenshots, and closed testing.
allowed-tools: Read, Grep, Glob, Bash
effort: high
---

# AutoMinder Play Store Readiness Skill

Read-only audit. Do not edit code in this skill unless separately approved.

## Audit checklist

Check and report status for each:

- Package is `com.autominder.app` (manifest + applicationId).
- Release build config: `isMinifyEnabled=true`, ENABLE_ADS=true, R8 rules present and release build verified green.
- versionCode/versionName sane for the submission track.
- Signing readiness: keystore path + passwords via local.properties only (NEVER read, print, or commit values — verify presence only).
- AdMob release IDs from local.properties/env (no hardcoded IDs in source; debug uses Google test IDs).
- UMP consent flow gates MobileAds.initialize (ads/ConsentManager.kt, MainActivity).
- Billing 7.1.1 pinned (do NOT propose upgrade — Billing 8 is a tracked separate pre-Aug-2026 task); products query split SUBS/INAPP; restore purchases; entitlement downgrade safety; paywall claims match shipped features only.
- POST_NOTIFICATIONS permission flow (contextual ask after first reminder save).
- Privacy policy URL exists and is reachable (Settings + Play listing).
- Data Safety form inputs enumerated (data collected: none/analytics/ads identifiers — derive from actual SDKs: Firebase Crashlytics/Perf, AdMob, Billing).
- Store listing assets: icon, feature graphic, ≥4 phone screenshots (use `D:\tmp\autominder-qa\` captures as source material), short + full description honest to shipped features.
- Closed testing track readiness: AAB builds (`bundleRelease`), testers list, release notes.
- Crash reporting live (Crashlytics wired).

## Output

- Ready for closed testing? Yes/No
- Ready for production? Yes/No
- P0 blockers (must fix before closed testing)
- P1 before production
- Manual Play Console checklist (things only the human can do)
- Exact next actions in order
