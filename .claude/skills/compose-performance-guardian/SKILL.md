---
description: Use to review AutoMinder Compose UI for smooth scrolling, recomposition safety, lazy list keys, remembered formatting, and release-ready performance.
allowed-tools: Read, Grep, Glob, Bash
effort: high
---

# Compose Performance Guardian

Read-only review. Run before committing any large UI change.

## Review rules

1. Lazy lists:
   - Stable keys (`key = { it.id }`) on every `items()`; section headers get string keys.
   - No date/number formatting repeated inside item lambdas without `remember(value, locale)` or pre-formatted UI models.
   - No heavy allocations per item.
   - `Modifier.animateItem()` present where rows enter/exit via Room Flow emissions.

2. State:
   - `collectAsStateWithLifecycle` for ViewModel flows.
   - `remember` for derived display strings.
   - `derivedStateOf` only for high-frequency inputs (scroll state) — not as a cargo cult.
   - `stateIn(SharingStarted.WhileSubscribed(5_000L))` on list ViewModels (governance requirement).

3. Animation:
   - Everything routes through `ui/theme/Motion.kt`; `Motion.reduceMotion` collapses specs to `snap()`.
   - No infinite animation except intentional skeleton/loading shimmer.
   - `animateFloatAsState`/`animateDpAsState`/`animateColorAsState` only — never raw `animateFloat()` (forbidden by governance).
   - Do not animate weak layouts for decoration.

4. Components:
   - Shared/presentational components must be dumb: no repository/DAO/ViewModel imports under `ui/components/`.

5. Gate (must be green before verdict):
   - `clean assembleDebug`, `testDebugUnitTest`, `lintDebug` (see `android-release-gate` skill for the Windows invocation pattern).

## Output

- Smoothness verdict.
- Risky composables (file:line).
- Lazy list issues.
- Formatting/allocation issues.
- Animation issues.
- Fix plan (smallest change first).
