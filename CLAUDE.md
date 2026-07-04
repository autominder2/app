# AUTOMINDER AGENT CONTEXT — READ FIRST, EVERY SESSION
# Place this file as CLAUDE.md in your project root: D:\Autominder\CLAUDE.md
# Updated: 2026-04 | Version: 4.0 | Aligned with PRD v4.0

## IDENTITY
App: AutoMinder — Android car maintenance & fuel intelligence app
Package: com.autominder.app  ← FINAL. LOCKED. NEVER CHANGE. EVER.
Play Store Developer: TikiTaka3D
Workspace: D:\Autominder
Target: Play Store production release
Version: 1.0.0 (current) → 1.1.0 (Fuel Intelligence next)

## SESSION VERIFICATION PROTOCOL
At the start of EVERY session, agent must confirm all 4:
1. Package is com.autominder.app
2. KSP version is 2.1.21-2.0.1 (never kapt)
3. Compose BOM is 2025.06.01
4. Files to be touched are listed before any code is written
If agent cannot confirm → STOP. Do not write code until confirmed.

## EXACT TECH STACK — never deviate from these versions
Kotlin:               2.1.21
AGP:                  8.9.1            # Must be 8.9+ for Kotlin 2.1.x
KSP:                  2.1.21-2.0.1     # MUST mirror Kotlin version prefix exactly
Compose BOM:          2025.06.01       # M3 Expressive (May 2025+)
Material3 Adaptive:   1.1.0            # NavigationSuiteScaffold
Hilt:                 2.55             # KSP only — NEVER kapt
Room:                 2.7.1            # KSP only — exportSchema=true always
Navigation:           2.9.0            # Type-safe @Serializable routes ONLY
WorkManager:          2.10.0
Coroutines:           1.9.0
DataStore:            1.1.2
Coil:                 3.1.0            # Coil 3 — io.coil-kt.coil3 group
Kotlinx Serialization: 1.8.0
AdMob:                23.5.0
Billing:              7.1.1
Timber:               5.0.1
Turbine:              1.2.0
minSdk:               26 (Android 8.0)
targetSdk:            36 (Android 16)
Java:                 17

## ARCHITECTURE LAW (violation = immediate rollback)
Pattern: MVVM + Clean Architecture + Offline-First + Repository
Layer chain: UI → ViewModel → UseCase → Repository interface → RepositoryImpl → DAO

ABSOLUTE RULES:
- ViewModel NEVER imports DAO directly
- UI composable NEVER imports Repository
- ALL DB reads return Flow<T> — suspend reads are WRONG
- ALL DB writes are suspend fun — Flow writes are WRONG
- ALL operations return sealed Result<T, AppError> — zero raw exceptions
- ALL async runs on viewModelScope or Dispatchers.IO — never Main thread
- ALL text lives in strings.xml — zero hardcoded English in .kt files
- ALL colors via MaterialTheme.colorScheme — zero Color(0xFF..) in UI
- stateIn(SharingStarted.WhileSubscribed(5_000L)) on ALL list ViewModels

## NAVIGATION LAW
Routes file: NavRoutes.kt — @Serializable sealed objects ONLY
CORRECT:   navController.navigate(VehicleDetail(vehicleId = id))
FORBIDDEN: navController.navigate("vehicle/42")  ← crashes silently
Bottom nav: Home | Vehicles | Records | Settings (4 tabs — verified 2026-07, matches code)
  Records = ServiceHistory route. Cross-vehicle (ServiceWithVehicle), grouped by month.
  A real top-level destination, not a per-vehicle detail screen — earned its tab.
FuelScreen: VehicleDetail tab (NOT a bottom tab)
Nav labels: strings.xml (nav_home/nav_vehicles/nav_records/nav_settings) — never hardcode in BottomNavBar.kt

## BRAND / DESIGN TOKENS
Primary color:  #006B5F (Racing Teal 700) — NOT the old #4CAF82
Font Display:   Exo 2, weight 700-800      (vehicle names, health score, big numbers, headlines)
Font Body:      Nunito Sans, weight 400-600 (all UI text, labels, descriptions, lists)
Font Mono:      JetBrains Mono, weight 500-600 (odometer, costs, km values, dates)
OVERDUE:        MaterialTheme.colorScheme.errorContainer + onErrorContainer
DUE_SOON:       MaterialTheme.colorScheme.tertiaryContainer + onTertiaryContainer
GOOD:           MaterialTheme.colorScheme.secondaryContainer + onSecondaryContainer
Card corners:   OVERDUE=8dp | DUE_SOON=16dp | GOOD=28dp (animateFloatAsState morphing)

## FILE OWNERSHIP — agents must not cross these lines
Data Agent:    *Dao.kt | *RepositoryImpl.kt | AppDatabase.kt | Converters.kt
UI Agent:      [Screen]Screen.kt + [Screen]ViewModel.kt ONLY (one screen per session)
Theme Agent:   Color.kt | Type.kt | Shape.kt | AutoMinderTheme.kt ONLY
Worker Agent:  *Worker.kt | BootReceiver.kt | ReminderScheduler.kt ONLY
Nav Agent:     NavGraph.kt | NavRoutes.kt ONLY
Domain Agent:  *UseCase.kt | domain/model/*.kt | domain/repository/*.kt ONLY

## ABSOLUTE FORBIDDEN PATTERNS
NEVER kapt() anywhere — only ksp()
NEVER fallbackToDestructiveMigration() — write Migration objects
NEVER animateFloat() — use animateFloatAsState()
NEVER calculateWindowSizeClass() — use NavigationSuiteScaffold
NEVER raw strings in navigate() — NavRoutes sealed class only
NEVER check snooze BEFORE raw status (StatusCalculator: OVERDUE always wins over SNOOZED)
NEVER store money as Double/Float — Int cents only, display as cents/100.0
NEVER hardcode Color(0xFF..) in composables — MaterialTheme.colorScheme only
NEVER hardcode AdMob IDs in source files — local.properties + CI env vars
NEVER touch *.keystore, google-services.json, local.properties content
NEVER commit secrets, signing passwords, or API keys
NEVER create a LazyColumn without key = { item.id }
NEVER leave a screen without all 4 states: Loading/Empty/Error/Success
NEVER create multi-table writes without @Transaction

## PRE-CODE CHECKLIST (mandatory before writing any code)
1. List all files to be created or modified (no surprises mid-session)
2. Check libs.versions.toml — never hardcode a version number
3. Check NavRoutes.kt — never create inline route strings
4. Check existing @Entity classes — never duplicate
5. Confirm package declaration at top of every new file = com.autominder.app.*
6. Run ./gradlew compileDebugKotlin after — must PASS before session ends

## BUILD VARIANTS
debug:   ENABLE_ADS=false | test AdMob IDs | debuggable=true | isMinifyEnabled=false
release: ENABLE_ADS=true  | IDs from local.properties/CI | isMinifyEnabled=true

## ADMOB ID PATTERN (correct approach)
```kotlin
// CORRECT: read from local.properties with fallback to test ID
import java.util.Properties
val local = Properties().apply {
    rootProject.file("local.properties").takeIf { it.exists() }?.inputStream()?.let(::load)
}
resValue("string", "admob_app_id",
    System.getenv("ADMOB_APP_ID")
    ?: local.getProperty("ADMOB_APP_ID")
    ?: "ca-app-pub-3940256099942544~3347511713"  // test ID fallback — never empty string
)
```

## SIGNING
Keystore path: local.properties → KEYSTORE_PATH
Passwords: STORE_PASSWORD + KEY_PASSWORD from local.properties only
NEVER in build files. NEVER in source. NEVER printed or read aloud.

## STATUSCALCULATOR — CORRECT ALGORITHM
```kotlin
fun calculate(reminder, currentOdometer, now): ServiceStatus {
    if (!reminder.isEnabled) return DISABLED
    val rawStatus = computeRaw(reminder, currentOdometer, now)  // Step 1: raw FIRST
    if (rawStatus == OVERDUE) return OVERDUE   // Step 2: OVERDUE always wins — cannot be snoozed
    val snoozed = reminder.snoozeUntil?.let { it > now } == true
    if (snoozed) return SNOOZED               // Step 3: only non-critical can be snoozed
    return rawStatus
}
// computeRaw: take WORST of date trigger and km trigger
// OVERDUE(severity=0) < DUE_SOON(severity=1) < GOOD(severity=2)
```

## NOTIFICATION COOLDOWNS (WorkManager)
OVERDUE:   re-notify every 24 hours (urgent — daily is correct)
DUE_SOON:  re-notify every 3 days (planning time without spam)
FUEL RED:  re-notify every 12 hours
FUEL AMBER: re-notify every 24 hours
GOOD/SNOOZED/DISABLED: never notify
30-day planning (insurance/registration): once per 30-day window (21-30 days before due)

## FUEL INTELLIGENCE ALGORITHM PHASES
Phase 1 (0 fill-ups):   seed from vehicle DB + driving style multiplier. Accuracy ±30%
Phase 2 (1-4 fill-ups): Bayesian updating against vehicle DB prior. Accuracy ±20%
Phase 3 (5-9 fill-ups): EMA α=0.25. Running sigma tracked. Accuracy ±12%
Phase 4 (10+ fill-ups): Seasonal adjustment. Adaptive α. Accuracy ±7%
Reminder trigger: confidence_low_km = estimated_km - 2*sigma_km < 50km → SEND

## BRANCH CONVENTION
main:          always compiles, always green — no direct commits
agent/phase-*: where AI agents work
fix/*:         quick bug fixes
feature/*:     human-driven features

## VERIFICATION GATE (mandatory after every task)
./gradlew compileDebugKotlin
# PASS → git commit → proceed to next task
# FAIL → STOP. Report the first error line only. Fix before any other task.
# git commit BEFORE every agent session — 10 seconds saves 10 hours.