---
description: Use before committing AutoMinder UI work. Reviews current diff for scope creep, forbidden file changes, hardcoded colors/text, architecture violations, accessibility risks, and commit readiness.
allowed-tools: Read, Grep, Glob, Bash
effort: medium
---

# AutoMinder UI Diff Review

## Current diff

!`git diff --stat`

Then inspect the full diff of UI files as needed:

```bash
git diff -- app/src/main/kotlin/com/autominder/app app/src/main/res/values/strings.xml
```

## Review checklist

Flag any of:
- forbidden files changed (database, DAOs, repositories, use cases, Billing/SubscriptionManager, Ads, WorkManager, NavRoutes/NavGraph, manifest, Gradle files, keystores, local.properties)
- package name changes
- Records renamed
- navigation route changes
- raw `Color(0xFF...)` outside `ui/theme/`
- hardcoded visible English in Kotlin
- visible `ServiceType.label` usage (must be `localizedLabel()`)
- dead click lambdas `{}` on visible CTAs
- unformatted distance/cost/date (must use `DistanceFormat.grouped()` / cents-based cost formatting / `DateFormatUtil`)
- string resources referenced but not added (or added but unused → lint UnusedResources)
- semantic icons without contentDescription
- lazy lists modified without stable keys
- new infinite animations or raw `animateFloat()`
- money stored/handled as Double/Float instead of Int cents
- missing Gradle gate results in the session (assembleDebug + testDebugUnitTest + lintDebug)

## Output

1. Diff summary.
2. Scope violations (blocking).
3. UI quality risks.
4. Accessibility/performance risks.
5. Safe to commit? Yes/No.
6. Recommended commit message (conventional commits: `fix(ui):` / `feat(ui):` / `chore(ui):`).
