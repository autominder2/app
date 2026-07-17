# AUTOMINDER AGENT CONTEXT — READ FIRST, EVERY SESSION
# Place this file as CLAUDE.md in your project root: D:\Autominder\CLAUDE.md
# Updated: 2026-07 | Version: 5.0 | Release-Hardening Governance Update

## IDENTITY
App: AutoMinder — Android car maintenance & fuel intelligence app
Package: com.autominder.app  ← FINAL. LOCKED. NEVER CHANGE. EVER.
Play Store Developer: TikiTaka3D
Workspace: D:\Autominder
Target: Play Store production release
Version: 1.0.0 (current — FEATURE-FROZEN, release hardening only) → 1.1.0 (post-launch, see CURRENT PHASE)

## CURRENT PHASE — RELEASE HARDENING
v1.0 is FEATURE-FROZEN. No new user-facing features land on this version.
Work in this phase is limited to: bug fixes, stability, security, billing
correctness, string/localization cleanup, lint/test hardening, and Play
Store submission readiness (Console setup, store listing, compliance).

Deferred to v1.1+ (do not start without explicit user go-ahead):
- Figma full-screen build (all 16 screens + component library)
- Garage hero silhouettes (vehicle body-style art)
- Offline VIN decode (local WMI table)
- Network VIN/plate lookup (NHTSA vPIC / EU plate APIs)
- OBD-II integration
- GPS / geofencing
- Cloud sync
- Home screen widgets
- Receipt OCR
- Family sharing

If a request touches any of the above, or adds new user-facing behavior,
STOP and confirm with the user that it's an intentional v1.1+ exception
before writing code.

## SESSION VERIFICATION PROTOCOL
At the start of EVERY session, agent must confirm all 5:
1. Package is com.autominder.app
2. KSP version is 2.1.21-2.0.1 (never kapt)
3. Compose BOM is 2025.06.01
4. Files to be touched are listed before any code is written
5. Current phase (v1.0 feature-frozen, see CURRENT PHASE) is acknowledged before scoping any new feature work
If agent cannot confirm → STOP. Do not write code until confirmed.

## EXACT TECH STACK — MUST MIRROR gradle/libs.versions.toml
This table is a read-only mirror of libs.versions.toml, not a second source
of truth. Before relying on any version below, check libs.versions.toml.
If the two ever disagree: STOP. Do not guess which is right, do not edit
either file to "fix" the mismatch — report the drift to the user and wait.
(Verified in sync as of this update, 2026-07.)

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

## BILLING GOVERNANCE
Billing 7.1.1 stays pinned for the entire release-hardening phase — do not
upgrade as part of any hardening task, even if a newer version is available.
Billing 8+ is backlog: required before Google's deadline for app UPDATES,
~Aug 31, 2026. Track it as its own pre-deadline upgrade task with enough
runway to test — never bundle it into an unrelated fix.

## ARCHITECTURE LAW (violation = immediate rollback)
Pattern: MVVM + Clean Architecture + Offline-First + Repository
Layer chain: UI → ViewModel → UseCase → Repository interface → RepositoryImpl → DAO

ABSOLUTE RULES:
- ViewModel NEVER imports DAO directly
- UI composable NEVER imports Repository
- Reactive UI reads (anything a screen observes) return Flow<T> — suspend reads for these are WRONG
- One-shot reads for background work or export (WorkManager, CSV/PDF export, migrations) MAY use a suspend fun getXOnce() — name it explicitly with "Once" so it's never mistaken for the reactive path
- ALL DB writes are suspend fun — Flow writes are WRONG
- Repository/UseCase layer failures return sealed Result<T, AppError> — zero raw exceptions crossing that boundary
- UI-facing state machines (ViewModel UiState) MAY use dedicated sealed states (Loading/Empty/Error/Success, or feature-specific variants like PurchaseState/RestoreState) instead of a generic Result wrapper — this is not a violation
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

## FILE OWNERSHIP — default boundary, not an absolute wall
Each role below is the DEFAULT set of files a task should touch. A task's
own explicitly listed scope (files enumerated up front, per PRE-CODE
CHECKLIST) is authoritative — if a task explicitly lists files outside the
default set, that's allowed, not a violation. Support files (strings.xml
and other resources) may be touched outside the default set ONLY when the
task explicitly needs them (e.g. a Screen change requiring a new string) —
never as an unannounced side effect.

Data Agent:    *Dao.kt | *RepositoryImpl.kt | AppDatabase.kt | Converters.kt
UI Agent:      [Screen]Screen.kt + [Screen]ViewModel.kt (one screen per session, default)
Theme Agent:   Color.kt | Type.kt | Shape.kt | AutoMinderTheme.kt
Worker Agent:  *Worker.kt | BootReceiver.kt | ReminderScheduler.kt
Nav Agent:     NavGraph.kt | NavRoutes.kt
Domain Agent:  *UseCase.kt | domain/model/*.kt | domain/repository/*.kt

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

## FUEL INTELLIGENCE ALGORITHM PHASES — REFERENCE ONLY DURING v1.0 RELEASE HARDENING
Fuel Intelligence is a v1.1+ feature (see CURRENT PHASE). The spec below is
preserved for when that work resumes — do not implement, expand, or wire up
any of these phases while v1.0 is feature-frozen.

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

## VERIFICATION GATE — three tiers, pick the one the task needs

MINIMUM COMPILE GATE (every task, no exceptions):
./gradlew compileDebugKotlin
# PASS → git commit → proceed
# FAIL → STOP. Report the first error line only. Fix before any other task.

RELEASE-HARDENING GATE (any task touching shipped behavior: bug fixes,
billing, notifications, string/localization changes, UI changes):
./gradlew clean assembleDebug
./gradlew testDebugUnitTest
./gradlew lintDebug
# All three must PASS before commit.

RELEASE-CANDIDATE GATE (before tagging a build for Play Console submission
or closed testing — run in addition to the release-hardening gate):
./gradlew assembleRelease
./gradlew bundleRelease

git commit BEFORE every agent session — 10 seconds saves 10 hours.