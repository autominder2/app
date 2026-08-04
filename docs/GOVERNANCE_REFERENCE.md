# AutoMinder Governance Reference
Moved out of root CLAUDE.md (v6, 2026-08-04) for token efficiency.
CLAUDE.md holds invariants; this file holds algorithms, matrices, and
patterns consulted on demand. CLAUDE.md wins on any conflict.

## STATUSCALCULATOR — CORRECT ALGORITHM
Enforced by app/src/test/.../StatusCalculatorTest.kt — the test is the
authority; this prose is explanation.
```kotlin
fun calculate(reminder, currentOdometer, now): ServiceStatus {
    if (!reminder.isEnabled) return DISABLED
    val rawStatus = computeRaw(reminder, currentOdometer, now)  // raw FIRST
    if (rawStatus == OVERDUE) return OVERDUE   // OVERDUE cannot be snoozed
    val snoozed = reminder.snoozeUntil?.let { it > now } == true
    if (snoozed) return SNOOZED                // only non-critical snoozes
    return rawStatus
}
// computeRaw: WORST of date trigger and km trigger
// OVERDUE(severity=0) < DUE_SOON(severity=1) < GOOD(severity=2)
```

## NOTIFICATION COOLDOWNS (WorkManager)
OVERDUE:    re-notify every 24 hours (urgent — daily is correct)
DUE_SOON:   re-notify every 3 days (planning time without spam)
FUEL RED:   re-notify every 12 hours
FUEL AMBER: re-notify every 24 hours
GOOD/SNOOZED/DISABLED: never notify
30-day planning (insurance/registration): once per 30-day window
(21-30 days before due)

## ADMOB ID PATTERN (as implemented in app/build.gradle.kts)
Debug block: Google's official test IDs, hardcoded — the sanctioned pattern.
Release block: RELEASE_ADMOB_ID / ADMOB_APP_ID from env or local.properties,
falling back to "PLACEHOLDER" (never a test ID, never empty).
Production Safety Gate (build.gradle.kts ~L216): any assembleRelease /
bundleRelease task fails with a GradleException when the resolved release
ID is null, blank, or "PLACEHOLDER". Do not weaken this gate.

## AGENT FILE-OWNERSHIP DEFAULTS
Default boundary, not an absolute wall — a task's explicitly approved file
scope is authoritative. Resource files (strings.xml etc.) may be touched
outside the default set only when the task explicitly needs them, never as
an unannounced side effect.
Data Agent:    *Dao.kt | *RepositoryImpl.kt | AppDatabase.kt | Converters.kt
UI Agent:      [Screen]Screen.kt + [Screen]ViewModel.kt (one screen/session)
Theme Agent:   Color.kt | Type.kt | Shape.kt | AutoMinderTheme.kt
Worker Agent:  *Worker.kt | BootReceiver.kt | ReminderScheduler.kt
Nav Agent:     NavGraph.kt | NavRoutes.kt
Domain Agent:  *UseCase.kt | domain/model/*.kt | domain/repository/*.kt

## FUEL INTELLIGENCE ALGORITHM PHASES — v1.1+ ONLY
Do not implement, expand, or wire up while v1.0 is feature-frozen.
Phase 1 (0 fill-ups):   seed from vehicle DB + driving style multiplier. ±30%
Phase 2 (1-4 fill-ups): Bayesian updating against vehicle DB prior. ±20%
Phase 3 (5-9 fill-ups): EMA α=0.25. Running sigma tracked. ±12%
Phase 4 (10+ fill-ups): Seasonal adjustment. Adaptive α. ±7%
Reminder trigger: confidence_low_km = estimated_km - 2*sigma_km < 50km → SEND
