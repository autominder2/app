# AutoMinder — AGENTS.md
# Agent Configuration & Scope Control
# Updated: 2026-08 | Aligned with CLAUDE.md v6.1 + 13-Layer Production Standard

## Project Identity
- App: AutoMinder — car maintenance reminders, fuel/service records & launch-truth hardening
- Package: `com.autominder.app` ← LOCKED FOREVER
- Architecture: Clean Architecture + MVVM + Offline-First + Repository + UDF
- Platform: Android | minSdk 26 | targetSdk 36 | Java 17

## Tech Stack (pinned — never change without explicit human approval)
| Tool | Version | Critical Rule |
|------|---------|---------------|
| Kotlin | 2.1.21 | KSP prefix must match |
| AGP | 8.9.1 | Requires Kotlin 2.1.x |
| KSP | 2.1.21-2.0.1 | Prefix mirrors Kotlin exactly |
| Compose BOM | 2025.06.01 | M3 Expressive. Never pin individual compose libs. |
| Material3 Adaptive | 1.1.0 | NavigationSuiteScaffold for 3-tab nav |
| Hilt | 2.55 | KSP only — NEVER kapt |
| Room | 2.7.1 | KSP only — exportSchema=true — NO fallbackToDestructiveMigration |
| Navigation | 2.9.0 | Type-safe @Serializable routes ONLY |
| WorkManager | 2.10.0 | ReminderCheckWorker + FuelReminderWorker + DailyCheckWorker |
| Coroutines | 1.9.0 | viewModelScope + Dispatchers.IO |
| DataStore | 1.1.2 | Settings, onboarding flag, isPremium |
| Coil | 3.1.0 | Vehicle photos — AsyncImage (coil3 group) |
| Kotlinx Serialization | 1.8.0 | Nav type-safe routes require this |
| AdMob | 23.5.0 | IDs from local.properties only |
| Billing | 9.1.0 | Google Play Billing 9.x active |
| Timber | 5.0.1 | Debug builds only — DebugTree |
| Turbine | 1.2.0 | Flow unit testing |

## Current Build Phase
**Phase**: Release hardening & Play Store readiness (feature-frozen)
**Last green build**: `gradlew.bat compileDebugKotlin testDebugUnitTest lintDebug`
**Next milestone**: Paywall exit & trust hardening, billing state machine, release-candidate evidence

## Folder Structure (verified against codebase 2026-08)
```
app/src/main/kotlin/com/autominder/app/
├── AutoMinderApp.kt              ← @HiltAndroidApp — never touch
├── MainActivity.kt               ← Single Activity host — never touch
├── ads/                          ← AdManager.kt, BannerAdView.kt
├── billing/                      ← SubscriptionManager.kt (Billing 9.x)
├── core/di/                      ← DatabaseModule, NetworkModule, WorkerModule
├── data/
│   ├── local/
│   │   ├── dao/                  ← VehicleDao, ReminderDao, FuelDao, ServiceDao, MileageLogDao
│   │   ├── entity/               ← VehicleEntity, ReminderEntity, FuelEntryEntity, ServiceEntity, MileageLogEntity
│   │   ├── database/             ← AutoMinderDatabase (v2)
│   │   └── preferences/          ← DataStore preferences
│   ├── mapper/                   ← Entity ↔ Domain model mappers
│   └── repository/               ← *RepositoryImpl (data layer implementations)
├── domain/
│   ├── intelligence/             ← VehicleConfidenceEngine.kt
│   ├── model/                    ← Vehicle, Reminder, FuelEntry, MileageLogEntry, Service, ServiceStatus, ServiceType
│   ├── repository/               ← I*Repository interfaces (contracts)
│   ├── usecase/                  ← StatusCalculator, GetDashboardData, CreateDefaultReminders, ExportServiceHistory, CalculateEfficiency
│   │   └── cockpit/              ← CalculateConfidenceUseCase, CalculateDrivingPatternUseCase, CalculateOwnershipCostUseCase
│   ├── util/                     ← Domain utilities
│   └── validation/               ← Input validation rules
├── ui/
│   ├── theme/                    ← Color.kt, Type.kt, Shape.kt, AutoMinderTheme.kt, LocalDistanceUnit.kt
│   ├── navigation/               ← NavRoutes.kt, NavGraph.kt
│   ├── components/               ← BottomNavBar, StatusChip, LoadingState, EmptyState, ErrorState, LocalSnackbarHostState
│   └── screens/
│       ├── about/                ← AboutScreen.kt
│       ├── dashboard/            ← DashboardScreen.kt, DashboardViewModel.kt
│       ├── fuel/                 ← AddFuelScreen.kt, AddFuelViewModel.kt, FuelHistoryScreen.kt, FuelHistoryViewModel.kt
│       ├── mileage/              ← MileageLogScreen.kt, MileageLogViewModel.kt
│       ├── onboarding/           ← OnboardingScreen.kt, OnboardingViewModel.kt
│       ├── reminder/             ← AddReminderScreen.kt, AddReminderViewModel.kt, EditReminderScreen.kt, EditReminderViewModel.kt
│       ├── service/              ← AddServiceScreen.kt, AddServiceViewModel.kt, ServiceDetailScreen.kt, ServiceDetailViewModel.kt, ServiceHistoryScreen.kt, ServiceHistoryViewModel.kt
│       ├── settings/             ← SettingsScreen.kt, SettingsViewModel.kt
│       └── vehicle/              ← AddVehicleScreen.kt, AddVehicleViewModel.kt, EditVehicleScreen.kt, EditVehicleViewModel.kt, VehicleDetailScreen.kt, VehicleDetailViewModel.kt, VehicleListScreen.kt, VehicleListViewModel.kt
├── widget/                       ← AutoMinderWidget.kt, WidgetDataProvider.kt (Glance)
├── worker/
│   ├── ReminderCheckWorker.kt    ← Daily maintenance reminder checks
│   ├── WorkScheduler.kt          ← Worker enqueue logic
│   └── BootReceiver.kt           ← Re-enqueue all workers after reboot
└── [FUTURE]
    ├── worker/FuelReminderWorker.kt   ← Phase 4: fuel prediction triggers
    └── worker/DailyCheckWorker.kt     ← Phase 5: morning pre-drive check
```

## Absolute Rules — These Are Law
1. **NEVER** use `kapt()` — only `ksp()`
2. **NEVER** use `Int` for `@PrimaryKey(autoGenerate = true)` — always `Long`
3. **NEVER** use `fallbackToDestructiveMigration()` — write `Migration` objects
4. **NEVER** use raw strings in `navigate()` — type-safe sealed routes only
5. **NEVER** hardcode AdMob IDs, signing keys, or secrets in source
6. **NEVER** touch `*.keystore`, `google-services.json`, `local.properties`
7. **NEVER** use `animateFloat()` — use `animateFloatAsState()`
8. **NEVER** use `calculateWindowSizeClass()` — use `NavigationSuiteScaffold`
9. **NEVER** check snooze before OVERDUE in StatusCalculator
10. **NEVER** store money as Double/Float — Int cents only
11. **EVERY** screen must implement all 4 states: Loading / Empty / Error / Success
12. **EVERY** `@Dao` read must return `Flow<T>` and every write must be `suspend`
13. **EVERY** coroutine must run on `viewModelScope` or `Dispatchers.IO`
14. **EVERY** `LazyColumn` must use `key = { item.id }` on items
15. **EVERY** new `.kt` file must start with `package com.autominder.app.*`
16. **EVERY** multi-table write must use `@Transaction`

## Agent Scope Control (prevents merge conflicts)
Each agent session has exactly ONE scope. NEVER cross these lines.

| Agent Type | ONLY Files Allowed |
|------------|-------------------|
| **Data Agent** | `*Dao.kt`, `*RepositoryImpl.kt`, `AppDatabase.kt`, `Converters.kt` |
| **UI Agent** | `[Screen]Screen.kt` + `[Screen]ViewModel.kt` (ONE screen per session only) |
| **Theme Agent** | `Color.kt`, `Type.kt`, `Shape.kt`, `AutoMinderTheme.kt` |
| **Worker Agent** | `*Worker.kt`, `BootReceiver.kt`, `ReminderScheduler.kt` |
| **Nav Agent** | `NavGraph.kt`, `NavRoutes.kt` |
| **Domain Agent** | `*UseCase.kt`, `domain/model/*.kt`, `domain/repository/*.kt` |
| **Build Agent** | `libs.versions.toml`, `build.gradle.kts` (root + app) |

## Session Protocol (run in order)
1. Inspect `git status`; do not commit, stash, or revert user work unless explicitly asked
2. Agent pastes CLAUDE.md universal header in full
3. Agent states exact files it will touch (no scope surprises)
4. Agent pastes current content of ALL files it will modify
5. Agent generates code
6. Run: `./gradlew compileDebugKotlin`
7. PASS → report changed files and verification; commit only if explicitly asked
8. FAIL → report FIRST error line only → fix → repeat from step 6

## Do NOT Touch (Protected Files)
- `app/google-services.json` — do not regenerate or modify
- `*.keystore` files — never overwrite, never print contents
- `local.properties` — never commit, never read contents aloud in responses
- `.gitignore` — do not modify without showing full diff first
- `app/schemas/*.json` — never delete, always commit new versions

## StatusCalculator Business Logic (VERIFIED CORRECT)
```
Priority order: OVERDUE > DUE_SOON > SNOOZED > OK > COMPLETED > UNKNOWN
Step 1: if isCompleted → COMPLETED
Step 2: check overdue by date or odometer BEFORE snooze
Step 3: if overdue → OVERDUE (snooze CANNOT hide OVERDUE)
Step 4: if snoozeActive → SNOOZED
Step 5: if due soon by date or odometer → DUE_SOON
Step 6: if any future due condition exists → OK
Step 7: otherwise UNKNOWN
```

## Notification Cooldown Rules (WorkManager)
```
OVERDUE:   86_400_000L ms (24 hours) — urgent, daily is correct
DUE_SOON:  86_400_000L * 3 ms (3 days) — avoids spam
FUEL RED:  43_200_000L ms (12 hours)
FUEL AMBER: 86_400_000L ms (24 hours)
OK / SNOOZED / COMPLETED / UNKNOWN: never send
30-day planning: 86_400_000L * 30 ms cooldown (once per window)
Result.success() always — never retry-storm on partial notification failure
```

## Database Version History
- v1: vehicles, reminders, services, mileage_logs, initial schema
- v2: + fuel_entries table + @Index on reminders(vehicleId, serviceType) + lastNotifiedAt on reminders
- Future: every change needs a named Migration object. Never destructive.

## Verification Gate (Mandatory After Every Task)
```bash
./gradlew compileDebugKotlin
# BUILD SUCCESSFUL → proceed, git commit
# BUILD FAILED → STOP. Report first error line ONLY. Fix before next task.
```

## Parallel Agent Safety Rules
When running multiple agents simultaneously:
1. Each agent MUST operate on different files (no overlapping scope)
2. VehicleDetailViewModel.kt: only ONE agent touches it per session
3. AppDatabase.kt: only ONE agent touches it per session (version + entity list)
4. NavGraph.kt: only ONE agent touches it per session
5. If unsure about overlap: run agents sequentially, not parallel
