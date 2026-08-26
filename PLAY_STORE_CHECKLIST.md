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
- ACTION: Data Safety must disclose Firebase Analytics, Crashlytics, Performance Monitoring,
  Sentry (error monitoring + performance tracing, a SECOND crash reporter alongside
  Crashlytics and previously absent from this list), AdMob/Advertising ID, Billing, and
  Android backup behavior.
- VERIFIED 2026-08-26: privacy policy corrected. store/PRIVACY_POLICY.md had affirmatively
  DENIED using crash reporting or analytics while shipping four such SDKs; the live
  website/public/privacy.html omitted Sentry and Google Play Billing entirely and claimed
  an "encrypted-at-rest SQLite database" the app does not implement (no SQLCipher, no
  SupportFactory). All three corrected; both documents now agree with the shipped SDKs.
- ACTION: Terms of Use is still missing. https://com-autominder-app.web.app/terms returns
  HTTP 404 (verified 2026-08-26) and website/public/ has no terms.html. Do not add an
  in-app Terms row until the page is authored and deployed.
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

---

## 2026 Submission Requirements — verified against the build (2026-08-26)

Facts checked against Google's own docs and against the merged release manifest,
not from memory.

### Deadlines — both already met
- **targetSdk 36 required for new apps and updates from 2026-08-31** (extension
  to 2026-11-01 by request). App is `targetSdk 36`, `compileSdk 36`. PASS, with
  5 days to spare.
- **Play Billing 7 rejected for new apps after 2026-08-31.** `libs.versions.toml`
  pins `billing = "9.1.0"`. PASS. (Version read from the catalog, which is the
  sole authority — not remembered.)

### THE CRITICAL PATH IS NOW THE CALENDAR, NOT THE CODE
Personal developer accounts created after 2023-11-13 need a closed test with
**at least 12 testers opted in continuously for 14 days** before production
access is granted. That is a hard 14-day wall no amount of engineering removes.

Consequence: start the clock NOW and do the remaining UI/UX work inside the
window. Sequence:
1. **Internal testing track today.** No tester minimum, no waiting period.
   This is the ONLY way to close the untested billing gate — real purchase,
   cancel, restore, offline relaunch, reinstall — plus the backup/restore
   entitlement test (`bmgr backup` / `bmgr restore` then relaunch offline:
   expected result is NOT Pro).
2. **Open the closed test the same day** with 12+ testers to start the 14-day
   counter running in parallel.
3. Finish paywall/Settings polish during those 14 days.
Confirm whether the TikiTaka3D account falls under this rule before planning any
launch date around it.

### Permissions — the SHIPPED set is 14, not the 4 in source
An earlier audit note said "4 permissions, all justified". That was the *source*
manifest. Play reviews the **merged** manifest. Verified shipped set:

  ACCESS_ADSERVICES_AD_ID / _ATTRIBUTION / _TOPICS   (AdMob, Privacy Sandbox)
  ACCESS_NETWORK_STATE, WAKE_LOCK, FOREGROUND_SERVICE (WorkManager / GMS)
  INTERNET, POST_NOTIFICATIONS, RECEIVE_BOOT_COMPLETED, VIBRATE  (ours)
  com.android.vending.BILLING                        (Play Billing)
  com.google.android.gms.permission.AD_ID            (AdMob)
  com.google.android.finsky.permission.BIND_GET_INSTALL_REFERRER_SERVICE (Analytics)
  com.autominder.app.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION  (AndroidX, self-scoped)

All 14 are library-injected and legitimate; none is on Play's restricted list,
so no permissions declaration form is triggered. Two consequences that ARE real:
- **AD_ID is present**, so Data Safety MUST declare Device or other IDs as
  collected and shared for advertising. Do not answer "no" to advertising ID.
- FOREGROUND_SERVICE is WorkManager's. Verified NO `setForeground`,
  `ForegroundInfo`, `setExpedited` or `OutOfQuotaPolicy` anywhere in
  `app/src/main/kotlin`, and no `<service>` in the app's own manifest — so the
  Android 14+ typed-foreground-service requirement is not triggered and there is
  no missing `foregroundServiceType` crash risk.

### App access declaration
The app has **no accounts and no sign-in**, so no credentials are needed for
review. Still declare this explicitly on the App content page as "All
functionality is available without special access" — leaving it blank invites a
rejection asking for credentials that do not exist.

### Release artifact — verified, not assumed
- `assembleRelease` BUILD SUCCESSFUL; `minifyReleaseWithR8` ran; R8 emitted **no**
  `missing_rules.txt` and no missing-class warnings.
- `apksigner verify --print-certs` PASSES: CN=AutoMinder, O=TikiTaka3D.
- `output-metadata.json`: versionCode **27000001**, versionName 1.0.0.
- Deobfuscation live for BOTH crash reporters:
  `uploadCrashlyticsMappingFileRelease` and `uploadSentryProguardMappingsRelease`
  both executed, so release stack traces will be readable.

### Still NOT verified (needs a device or the Console)
- Billing purchase / cancel / restore / offline / reinstall on hardware.
- Backup-restore entitlement behaviour (the P0 fix is config-asserted only).
- TalkBack pass, 2.0x font pass, process-death restoration.
- Pre-launch report (Console-generated; needs an upload first).
- Store listing assets: 512px icon, 1024x500 feature graphic, >=4 phone
  screenshots, title <=30 chars, short description <=80 chars, content rating.

---

## Release Candidate evidence — 2026-08-27

Built on `release/1.0.0`. Every line below is a command's own output, not a
summary of one.

### Artifacts

| Item | Value |
|---|---|
| Command | `clean assembleRelease bundleRelease` |
| Result | BUILD SUCCESSFUL in 18m 40s |
| APK | 9,895,745 bytes |
| AAB | 15,686,251 bytes, `jar verified` |
| versionCode / versionName | `27000001` / `1.0.0` |
| minSdk / compileSdk | 26 / 36 |
| R8 `missing_rules.txt` | absent — rules were complete |
| `lintVitalRelease` | passed (`warningsAsErrors = true`) |
| `mapping.txt` | present, 100,748,816 bytes |

### Backup verified in the MINIFIED RELEASE build

The debug run proved the fix; this proves R8 did not undo it. R8 kept
`AutoMinderBackupAgent` under its original name, so the manifest reference
still resolves:

    com.autominder.app.data.backup.AutoMinderBackupAgent ->
    com.autominder.app.data.backup.AutoMinderBackupAgent:

Confirmed in the shipped binary manifest via `aapt2 dump xmltree`:

    android:allowBackup=true
    android:backupAgent="com.autominder.app.data.backup.AutoMinderBackupAgent"
    android:fullBackupOnly=true

`bmgr backupnow` against the release APK moved **146,432 bytes**. Before the
`fullBackupOnly` fix the same command produced no progress lines at all and
restored nothing. Backup -> `pm clear` -> restore -> launch leaves an empty
crash buffer and no entry mentioning `com.autominder`.

Not verifiable for the release build on this AVD: per-file inspection of the
restore. `run-as` needs a debuggable build and `adb root` is refused on a
production image (`adbd cannot run as root in production builds`). The
file-level exclusion was proven on the debug build, where both DataStore files
were byte-identical before backup and only `user_preferences` came back.

### Permissions actually shipped — 14, not 4

The source manifest declares 4. Play reviews the merged manifest. Every one of
the added 10 is attributable to a library via the manifest merger report, so
none is stray, but three carry Data Safety consequences.

| Permission | Injected by | Note |
|---|---|---|
| INTERNET, POST_NOTIFICATIONS, RECEIVE_BOOT_COMPLETED, VIBRATE | ours | VIBRATE is required by `NotificationHelper.kt:56` `enableVibration(true)` |
| ACCESS_NETWORK_STATE | play-services-ads-lite, measurement | SDK requirement |
| FOREGROUND_SERVICE | androidx.work 2.10.0 | WorkManager |
| WAKE_LOCK | play-services-measurement | Firebase Analytics |
| com.android.vending.BILLING | billing 9.1.0 | required |
| DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION | androidx.core 1.15.0 | internal signature permission |
| **AD_ID** | ads-lite, measurement-api | **Data Safety: advertising ID collected AND shared. Answer YES.** |
| ACCESS_ADSERVICES_AD_ID / _ATTRIBUTION / _TOPICS | ads-lite, measurement-api | Privacy Sandbox |
| BIND_GET_INSTALL_REFERRER_SERVICE | measurement-impl | install attribution |

Removing AD_ID via `tools:node="remove"` is possible but disables personalized
ads and Analytics attribution, so it is a monetization decision, not cleanup.

### Repository detox

| Check | Result |
|---|---|
| TODO / FIXME / HACK / XXX in main sources | **0** |
| `println` / `android.util.Log.x` / `printStackTrace` | **0** (the one `android.util.Log` import is for priority constants in `CrashlyticsTree`) |
| Mock / fake / dummy data in production | **0** (all matches are Compose `placeholder =` parameters) |
| Unused resources | none — `lintVitalRelease` passes with `warningsAsErrors = true` |
| Lint baseline | 5 entries: 4 widget attributes correctly gated above minSdk, 1 Compose API advisory |
| Dead code | 1 candidate: `FormField` in `ui/components/FormAnimations.kt`, zero references. `docs/exec-plans/mobbin-design-blueprint-2026.md:631` says KEEP, so left in place pending a design call. |

### Still not verified

Billing on hardware (all four lifecycle scenarios); TalkBack and 2.0x font
sweeps; process-death restoration; `terms.html` deployment; AdMob UMP consent
form; OSS licences screen; pre-launch report (needs an upload first).
