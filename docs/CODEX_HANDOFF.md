# AutoMinder — Verified Codex Handoff

Last verified date: 2026-08-14 (Asia/Karachi)

Repository: `D:\Autominder`

Branch at inventory: `feat/ui-log-service-night-garage`

HEAD: `59653a8020a262855c88b783c0f25d5d810bb5d3` (`feat(ui): Night Garage Log Service — Midnight Cobalt and a fast path`)

Canonical package / application ID / namespace: `com.autominder.app`

Product version: `1.0.0`

Current phase: v1.0 release hardening; feature-frozen except approved bug, stability, security, billing, localization, test/lint, accessibility, and Play-submission work.

This file replaces the `UNKNOWN` technical sections in the ChatGPT-to-Codex migration handoff prepared on 2026-08-14. It records repository evidence, not chat memory. Re-verify Git state and the version catalog before every implementation task.

## 1. Source-of-truth order

1. `gradle/libs.versions.toml` for dependency versions.
2. Current source, tests, exported Room schemas, and Git history.
3. `CLAUDE.md` for current phase and governance.
4. Path rules in `.claude/rules/` and `docs/GOVERNANCE_REFERENCE.md`.
5. This handoff, then `HANDOFF.md`, session handoffs, `PRD.md`, and older planning documents.

When documents disagree, do not silently merge them. Record the contradiction and follow the higher source.

## 2. Product and release objective

AutoMinder is an offline-first Android car-maintenance companion. It tracks vehicles, mileage, services, fuel, and maintenance reminders; produces dashboard health/status information; schedules reminder notifications; provides a weekly digest and a home-screen widget; and includes ads/consent, Play Billing entitlements, analytics, Crashlytics, Performance Monitoring, review, and in-app update integrations.

The immediate product objective is a stable Google Play v1.0 release for `com.autominder.app`. v1.1+ work is explicitly deferred: fuel-intelligence expansion, VIN lookup, OBD-II, GPS/geofencing, cloud sync, receipt OCR, family sharing, major widget redesign, and unapproved full-screen/Figma redesign work.

The official Google Play Billing deprecation FAQ, checked 2026-08-14, gives Billing Library 7 a new-app/update deadline of 2026-08-31 and an extension deadline of 2026-11-01. The repository still pins `7.1.1`; migration to 9.x is mandatory before production submission unless Billing is removed or an approved extension is used. Repository decision: perform the migration alone on `migration/play-billing-9`, never bundled with UI, Ads, Room, navigation, or release-workflow changes.

## 3. Git and existing WIP

Inventory began with user-owned uncommitted work. Do not discard, overwrite, stash, or generically checkpoint it:

- Modified: `app/src/main/res/values/strings.xml` — four sentence-case edits in Add Service copy.
- Untracked: `.mcp.json` — inspected only for common secret-key names; none detected. Do not assume it is safe to publish without human review.
- Untracked: `HANDOFF_SESSION_2026-08-04.md` — substantive prior-session context.
- Untracked: `scripts/stitch-proxy.mjs`.

The current HEAD already contains the Night Garage Log Service slice across Add Service UI/ViewModel/test, theme, strings, `ServiceChoicePicker`, `ServiceTypeGrid`, and `docs/DESIGN_SYSTEM_2026.md`. The remaining `strings.xml` edits appear related but are still uncommitted and must be reviewed by their owner.

Ignored protected local files are present but were neither opened nor changed: `local.properties`, `autominder-upload.jks`, and `app/google-services.json`. `git ls-files` did not report them as tracked.

## 4. Modules and build configuration

Only one Gradle module is included: `:app` (`settings.gradle.kts`).

| Item | Verified value | Evidence |
|---|---:|---|
| Gradle wrapper | 8.11.1 | `gradle/wrapper/gradle-wrapper.properties` |
| Android Gradle Plugin | 8.9.1 | `gradle/libs.versions.toml` |
| Kotlin | 2.1.21 | `gradle/libs.versions.toml` |
| KSP | 2.1.21-2.0.1 | `gradle/libs.versions.toml`; prefix matches Kotlin |
| Java / JVM target | 17 | `app/build.gradle.kts` |
| `compileSdk` | 36 | `app/build.gradle.kts` |
| `targetSdk` | 36 | `app/build.gradle.kts` |
| `minSdk` | 26 | `app/build.gradle.kts` |
| Compose BOM | 2025.06.01 | `gradle/libs.versions.toml` |
| Material3 Adaptive | 1.1.0 | `gradle/libs.versions.toml` |
| Hilt | 2.55 | `gradle/libs.versions.toml` |
| Room | 2.7.1 | `gradle/libs.versions.toml` |
| Navigation Compose | 2.9.0 | `gradle/libs.versions.toml` |
| WorkManager | 2.10.0 | `gradle/libs.versions.toml` |
| DataStore | 1.1.2 | `gradle/libs.versions.toml` |
| Lifecycle | 2.8.7 | `gradle/libs.versions.toml` |
| Coroutines | 1.9.0 | `gradle/libs.versions.toml` |
| Kotlin serialization | 1.8.0 | `gradle/libs.versions.toml` |
| Coil | 3.1.0 | `gradle/libs.versions.toml` |
| AdMob | 23.5.0 | `gradle/libs.versions.toml` |
| UMP | 3.1.0 | `gradle/libs.versions.toml` |
| Play Billing | 7.1.1 | `gradle/libs.versions.toml` |
| Firebase BOM | 33.9.0 | `gradle/libs.versions.toml` |
| Timber | 5.0.1 | `gradle/libs.versions.toml` |
| Turbine | 1.2.0 | `gradle/libs.versions.toml` |
| Glance | 1.1.1 | `gradle/libs.versions.toml` |

Other pinned versions are in the catalog and must be read there rather than copied from memory. Annotation processing is KSP-only for Hilt and Room; no `kapt` dependency is configured. Release builds enable R8 minification and resource shrinking. Locales are `en`, `es`, and `pt-rBR`. `versionCode` is generated from `yyMMddHH`; `versionName` is `1.0.0`.

## 5. Architecture map

Architecture law: Compose single-activity app using MVVM + Clean Architecture + offline-first repositories and unidirectional state. Expected dependency direction is UI → ViewModel → Use case/repository interface → repository implementation → DAO.

Key entry points and ownership:

- Application: `app/src/main/kotlin/com/autominder/app/AutoMinderApp.kt` (`@HiltAndroidApp`, WorkManager configuration, deferred startup initialization, notification channels).
- Activity: `app/src/main/kotlin/com/autominder/app/MainActivity.kt` (`@AndroidEntryPoint`, Compose host, onboarding start route, notification deep links, bottom navigation, ads/consent/update integration).
- Navigation: `ui/navigation/NavRoutes.kt` and `NavGraph.kt`; 16 `@Serializable` route types and type-safe `composable<T>` destinations.
- UI: `ui/screens/` with dashboard, vehicle, reminder, service, fuel, mileage, onboarding, settings, and about flows.
- Shared UI: `ui/components/`; premium presentation kit in `ui/components/premium/`.
- Domain: `domain/model`, `domain/repository`, `domain/usecase`, `domain/validation`, and `domain/util`.
- Data: Room in `data/local`, DataStore in `data/local/preferences`, entity/domain mappers, and five repository implementations.
- DI: `core/di/DatabaseModule.kt`, `RepositoryModule.kt`, and `AnalyticsModule.kt`.
- Background: `worker/ReminderCheckWorker.kt`, `WeeklyDigestWorker.kt`, `WorkScheduler.kt`, and `BootReceiver.kt`.
- Notifications: `core/notifications/NotificationHelper.kt` and `NotificationActionReceiver.kt`.
- Widget: `widget/AutoMinderWidget.kt` (Glance; this is shipped v1.0 functionality, not future work).
- Monetization: `ads/` and `billing/SubscriptionManager.kt`.
- Store/release docs: `store/`, `PLAY_STORE_CHECKLIST.md`, `PLAY_LAUNCH_SCORECARD.md`, and `website/`.

`AndroidManifest.xml` declares only INTERNET, POST_NOTIFICATIONS, RECEIVE_BOOT_COMPLETED, and VIBRATE; cleartext traffic is disabled. It contains the launcher activity, internal notification action receiver, permission-protected boot receiver, non-exported FileProvider, and widget receiver. WorkManager default startup initialization is removed because `AutoMinderApp` supplies `HiltWorkerFactory`.

## 6. Navigation and UI state

Bottom navigation is four tabs: Home (`Dashboard`), Vehicles (`VehicleList`), Records (`ServiceHistory`), and Settings. Do not rename Records or revert to obsolete three-tab documentation.

Type-safe routes:

`Dashboard`, `VehicleList`, `ServiceHistory`, `Settings(openPaywall)`, `VehicleDetail(vehicleId, openMileageSheet, mileageRequestId)`, `AddVehicle`, `EditVehicle(vehicleId)`, `AddReminder(vehicleId)`, `EditReminder(reminderId)`, `ServiceDetail(serviceId)`, `AddService(vehicleId)`, `MileageLog(vehicleId)`, `AddFuel(vehicleId)`, `FuelHistory(vehicleId)`, `About`, and `Onboarding`.

DataStore file name: `user_preferences`. Exact keys in `UserPreferences.kt`:

- `notifications_enabled`
- `has_seen_onboarding`
- `theme_mode`
- `distance_unit`
- `is_pro_cached`
- `service_log_count`
- `has_requested_review`

## 7. Room database and persistence

Database: `AppDatabase`, file name `autominder.db`, schema version 2, `exportSchema = true`, WAL enabled. Exported schemas exist at `app/schemas/com.autominder.app.data.local.database.AppDatabase/1.json` and `2.json`. No destructive-migration builder call exists.

Entities (all generated primary keys are `Long`):

- `vehicles`: `id`, `make`, `model`, `year`, `plateNumber`, nullable `vin`, `currentOdometer`, nullable `photoUri`, `isArchived`, `notes`, `createdAt`, `updatedAt`.
- `reminders`: `id`, `vehicleId`, `serviceType`, nullable `customLabel`, nullable `intervalDays`, nullable `intervalKm`, nullable `nextDueDate`, nullable `nextDueOdometer`, nullable `snoozeUntil`, `notifyDaysBefore`, nullable `lastNotifiedAt`, `isCompleted`, nullable `completedAt`, `notes`, `createdAt`, `updatedAt`; FK to vehicles with cascade delete; index on `vehicleId`.
- `services`: `id`, `vehicleId`, `serviceType`, nullable `customLabel`, `odometerAtService`, `serviceDate`, nullable `costCents`, nullable `shopName`, `notes`, nullable `receiptPhotoUri`, `createdAt`; FK cascade; index on `vehicleId`.
- `mileage_logs`: `id`, `vehicleId`, `odometer`, `loggedAt`, nullable `notes`; FK cascade; index on `vehicleId`.
- `fuel_entries`: `id`, `vehicleId`, `date`, `odometer`, `volumeMilliliters`, `costCents`, `notes`, `createdAt`; FK cascade; index on `vehicleId`.

DAOs: `VehicleDao`, `ReminderDao`, `ServiceDao`, `MileageLogDao`, and `FuelDao`. Reads are Flow-based except explicitly snapshot/suspend lookup operations used by transactions. Multi-table operations use Room transactions in repository implementations, including atomic service completion/reminder rebasing.

Only migration: `DatabaseModule.MIGRATION_1_2` creates `fuel_entries` and its `vehicleId` index, and is registered through `.addMigrations(MIGRATION_1_2)`. `MigrationTest` validates 1→2 data preservation/schema and fresh v2 creation. Device execution was not performed during this inventory.

## 8. Status and reminder behavior

The implemented and unit-tested status vocabulary is `OVERDUE`, `DUE_SOON`, `SNOOZED`, `OK`, `COMPLETED`, and `UNKNOWN` (`ServiceStatus.kt`). Current order in `StatusCalculator` is completed first, then overdue, snoozed, due-soon, OK, unknown. Overdue is intentionally evaluated before snooze. Due-soon thresholds are 14 days or 500 distance units.

Important contradiction: older `AGENTS.md`/governance prose describes `GOOD`/`DISABLED` and an `isEnabled` flag. The current `Reminder` model/entity has no `isEnabled`; code and `StatusCalculatorTest` are authoritative until an explicit domain change is approved.

Work scheduling:

- Unique work `reminder_check_work`: every 6 hours, `ExistingPeriodicWorkPolicy.KEEP`.
- Unique work `weekly_digest_work`: every 7 days with 1-day initial delay, `KEEP`.
- `BootReceiver` schedules both after `BOOT_COMPLETED`.
- Reminder worker returns success even after partial notification failure to prevent retry storms.
- Notification cooldowns: overdue 24 hours; due-soon 3 days.

Notification identifiers:

- Channels: `autominder_reminders` / “Maintenance Reminders”; `autominder_digest` / “Weekly Summary”.
- Digest notification ID: `-1000`.
- Actions: `com.autominder.app.action.NOTIF_MARK_DONE`, `com.autominder.app.action.NOTIF_SNOOZE`.
- Extras: `extra_reminder_id`, `vehicleId`, `openMileageSheet`, `mileageRequestId`.

## 9. Billing, ads, and external SDK behavior

`SubscriptionManager` currently handles subscription and in-app products separately, restores purchases, acknowledges purchased items, does not grant Pro for pending purchases, and caches Play-confirmed entitlement in DataStore.

Exact product IDs:

- `autominder_pro_monthly`
- `autominder_pro_yearly`
- `autominder_pro_lifetime`

Debug builds disable ads and use official Google test IDs. Release IDs come from environment/local properties and are protected by a production safety gate; never replace that with test-ID fallback. UMP consent gates ad initialization. Firebase Analytics, Crashlytics, and Performance Monitoring are included.

## 10. Verified feature state

Status terms below mean repository implementation plus the stated verification, not Play production readiness.

| Area | Status | Evidence / qualification |
|---|---|---|
| Foundation/build/DI/theme | VERIFIED for debug build | compile/assemble passed 2026-08-14 |
| Vehicle CRUD and archive | IMPLEMENTED — PARTIALLY TESTED | screens, repository, DAO; selected ViewModel tests |
| Reminder CRUD/status engine | IMPLEMENTED — UNIT TESTED | screens, worker, `StatusCalculatorTest` |
| Service logging/history | IMPLEMENTED — UNIT TESTED | current Night Garage slice; atomic completion repository tests |
| Mileage logging/quick update | IMPLEMENTED — UNVERIFIED ON DEVICE in this inventory | source and routes present |
| Fuel logging/efficiency | IMPLEMENTED — UNVERIFIED ON DEVICE in this inventory | source and calculation use case present |
| Reminder notifications/actions | IMPLEMENTED — UNVERIFIED ON DEVICE in this inventory | workers/receiver/helper present |
| Weekly digest | IMPLEMENTED — UNVERIFIED ON DEVICE in this inventory | worker and scheduler present |
| Onboarding | IMPLEMENTED — UNIT TESTED | screen/ViewModel and 13 tests |
| Ads + UMP | IMPLEMENTED — PLAY/REGIONAL TEST REQUIRED | source/config present |
| Billing | IMPLEMENTED ON PBL 7.1.1 — MIGRATION REQUIRED | source/config present; Play license testing still required |
| Glance widget | IMPLEMENTED — MANUAL QA REQUIRED | shipped source/manifest entry present |
| Localization | PARTIAL | locale filters and resource sets exist; current string WIP remains |
| Play listing/privacy | PARTIAL | store/website/checklists exist; Console actions remain |
| Baseline profile | PARTIAL | plugin/profileinstaller present; no producer module |

The old phase tables in `README.md` and `PRD.md` are stale and contradictory. Do not use their YES/NO cells as completion evidence.

## 11. Baseline verification (2026-08-14)

Environment: Windows PowerShell, Amazon Corretto JDK 17.0.18, pinned Gradle 8.11.1, with `_JAVA_OPTIONS=-Djdk.net.unixdomain.tmpdir=C:\tmp -Djava.net.preferIPv4Stack=true` and no configuration cache/daemon.

- `gradlew.bat compileDebugKotlin --no-configuration-cache --no-daemon`: **PASS**, 39s.
- `gradlew.bat assembleDebug testDebugUnitTest lintDebug --no-configuration-cache --no-daemon`: **PASS**, 269s.
- JVM unit tests: **70 passed, 0 failed, 0 skipped** across 7 test classes.
- Lint report: **No errors or warnings** after baseline filtering.
- Instrumented tests: **NOT RUN** (no device/emulator gate was executed). Includes Room migration and service-completion transaction coverage.
- `assembleRelease bundleRelease`: R8 produced `app-release.apk` (6,056,141 bytes), `app-release.aab` (12,625,205 bytes), and mapping outputs at 02:43 on 2026-08-14. The observing command timed out after 10 minutes after artifacts were written, and an up-to-date verification attempt also exceeded its observation window. Therefore artifacts are confirmed generated, but a definitive wrapper exit status is **NOT ESTABLISHED** by this inventory.
- Signed artifact verification, device migration tests, Play Billing purchase/restore, UMP regional consent, test-ad behavior, TalkBack, 2.0× font, light/dark, reduced motion, process death, and Play pre-launch report: **NOT RUN**.

Build outputs are ignored artifacts and are not part of the handoff commit.

## 12. Known gaps and contradictions

Confirmed gaps:

- Billing 7.1.1 must be migrated before the 2026-08-31 submission deadline (or approved extension path used).
- Current four-line `strings.xml` WIP needs review/reconciliation.
- No baseline-profile producer module exists.
- Play Console product setup/license testing, Data Safety, ads declaration, content rating, store assets, and closed testing remain human/Console work.
- Migration/device tests and full release-candidate manual matrix remain outstanding.
- Demo data can show implausible “Overdue by 173,000 km”; fix before store screenshots.
- Accessibility sweep still needs TalkBack, 200% font, reduced motion, and short-screen/FAB checks.
- `website/.firebase/*.cache` was previously flagged as tracked; re-check before modifying `.gitignore` or index state.

Documentation drift:

- `AGENTS.md` says Phase P6/reminder engine active and describes `GOOD`/`DISABLED`; current `CLAUDE.md`, code, tests, and Git history show later release-hardening work and different status vocabulary.
- `PRD.md` says three navigation tabs; shipped code has four.
- `PRD.md` lists Fuel Intelligence as v1.0; `CLAUDE.md` defers its expansion to v1.1+.
- `README.md` phase status predates current billing, UI, and release-hardening work.
- `HANDOFF.md` predates commits after its listed UI slices. Use it for design intent, not current Git state.

No TODO/FIXME markers representing concrete product bugs were found in app source by the inventory search; known work is primarily carried in handoffs/checklists rather than inline markers.

## 13. Exact next implementation sequence

### Step 0 — reconcile WIP

Before branching or editing production code, the owner must decide whether the four sentence-case `strings.xml` edits belong to commit `59653a8`. Review and commit them descriptively if valid, or deliberately revert them with explicit authorization. Review the three untracked files separately; never include `.mcp.json` without checking it for credentials and intended portability.

### Step 1 — mandatory Billing migration

After reaching a recoverable Git state, create `migration/play-billing-9` from the intended green base and migrate Billing `7.1.1` to the approved 9.x target. Re-check the version catalog and official Google PBL 9 migration/release notes immediately before editing. Do not bundle UI, Ads, Room, navigation, or release workflow changes.

Acceptance criteria:

1. Version catalog pins the approved PBL 9 release; no unrelated version changes.
2. `SubscriptionManager` compiles against PBL 9 and preserves product IDs.
3. Entitlement is granted only for verified `PURCHASED`; pending remains distinct.
4. Existing subscriptions and lifetime purchases restore at startup and via Restore.
5. Eligible purchases are acknowledged; tokens and sensitive billing data are never logged.
6. Monthly/yearly/lifetime ProductDetails states remain loading/available/unavailable as appropriate; no fake prices.
7. ITEM_ALREADY_OWNED re-queries purchases; transient failures preserve last confirmed entitlement.
8. Regression tests cover changed billing logic where JVM-testable.
9. Quick and hardening gates pass; release compile/bundle exit is captured.
10. Play-signed license-tester purchase, pending, cancel, restore, offline-cache, and relock behavior is recorded before submission.

### Step 2 — release hardening only

Resume deferred UI slices or nit fixes only after Billing migration and explicit scope approval. Use current Git history rather than old slice numbering to determine what remains. Then run the full release-candidate gate and Play/manual checks.

## 14. Confirmed decisions vs suggestions

Confirmed decisions:

- `com.autominder.app` is locked permanently.
- Kotlin/AGP/KSP/SDK and other versions stay pinned until an explicitly scoped migration.
- KSP only; no kapt.
- Clean Architecture + MVVM + offline-first repository layering.
- Type-safe serialized routes only.
- No destructive Room fallback; named migrations and committed schemas.
- Money uses integer cents.
- Four bottom tabs: Home, Vehicles, Records, Settings.
- v1.0 is feature-frozen and Play readiness is first-class.
- PBL migration occurs alone on a `migration/*` branch.
- Important continuation context stays in the repository.

Suggestions (not approved decisions):

- Keep this document current after every significant milestone.
- Add ADRs for future architectural changes.
- Split large release/manual verification evidence into dated documents under `docs/` rather than expanding this handoff indefinitely.

## 15. Session startup checklist

1. Read `CLAUDE.md`, relevant `.claude/rules/*`, and this file.
2. Run `git status`, record branch/HEAD, and inspect WIP ownership.
3. Check `gradle/libs.versions.toml` for every version relied on.
4. State current phase and exact files to be touched before editing.
5. Select one scope; do not cross ownership boundaries without explicit approval.
6. Preserve protected files and secrets; never print their contents.
7. Run the verification tier required by `CLAUDE.md` and report what was not verified.
