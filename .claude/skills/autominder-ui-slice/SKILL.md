---
description: Use for AutoMinder UI implementation slices. Enforces exact file scope, Material 3 rules, release-hardening gates, no feature creep, AVD screenshots, and production-safe reporting.
allowed-tools: Read, Grep, Glob, Bash, Edit, Write
effort: high
---

# AutoMinder UI Slice Skill

## Source of truth

- Package: `com.autominder.app` — FINAL, never change.
- Bottom nav: `Home | Vehicles | Records | Settings`
- Current phase: v1.0 release hardening. v1.0 is FEATURE-FROZEN.
- Design contract: the 12 AutoMinder 2026 Premium UI Rules (below).
- Governance: CLAUDE.md at repo root always wins on conflicts.

## Absolute rules

- Do not rename package.
- Do not rename Records.
- Do not change navigation routes.
- Do not change database schema.
- Do not touch DAOs/repositories/use cases unless explicitly approved in the slice prompt.
- Do not touch Billing, Ads, or WorkManager unless explicitly approved.
- Do not add VIN, OCR, GPS, OBD, cloud, maps, widgets, family sharing, or new features.
- Do not hardcode `Color(0xFF...)` in UI — `MaterialTheme.colorScheme` only.
- Do not hardcode user-visible English in Kotlin — strings.xml only.
- Never render `ServiceType.label` directly — use `ServiceType.localizedLabel()` from `ui/util/ServiceTypeLabel.kt`.
- Format all distance/odometer values with `DistanceFormat.grouped()` from `ui/util/DistanceFormat.kt`.
- Fonts: Exo 2 (hero/display), Nunito Sans (body), JetBrains Mono (data precision) only.
- Use existing `ui/theme/Motion.kt` for all animation; respect `Motion.reduceMotion`.
- Status must be text + icon/chip + color + shape/accent rail — never color-only.

## 2026 premium UI rules (condensed)

1. **Truth before beauty** — no `Year: 0`, no raw `201000 km`, no dead CTAs, no false paywall claims, no fake features.
2. **One screen = one story** — Dashboard = what needs attention now; Vehicle Detail = this car's condition + next action; Records = proof of care; Forms = guided task completion; Settings/Paywall = trust and membership clarity.
3. **No equal-weight layouts** — primary story, secondary actions, metadata, tertiary/admin actions, visually distinct.
4. **Tonal layering** — page = `surface`, section = `surfaceContainerLow`, card = `surfaceContainer`, sheet/paywall = `surfaceContainerHigh`, warnings = semantic containers only.
5. Status corner morphing is brand: OVERDUE = 8dp, DUE_SOON = 16dp, GOOD = 28dp.
6. Vehicle identity is emotional — car object, not database row.
7. Numbers must feel premium — formatted, aligned, JetBrains Mono where precision matters.
8. Forms are guided workflows — sectioned, never raw field stacks.
9. Motion comes after hierarchy.
10. Performance is design — stable lazy keys, remembered formatted strings, no infinite animation.
11. Accessibility is not optional — 48dp targets, TalkBack labels, large-text safe, reduced motion.
12. No feature creep.

## Required workflow

Before editing:
1. State slice name.
2. List allowed files.
3. List forbidden files.
4. State exact acceptance criteria.
5. Confirm no feature creep and no business-logic change.

During editing:
1. Modify only allowed files.
2. Keep components presentational (no ViewModel/repo/nav access in shared components).
3. Keep all visible text localized.
4. Prefer reusable UI utilities/components (`ui/components/`, `ui/components/premium/`, `ui/util/`).

After editing run the release-hardening gate:

```bash
./gradlew clean assembleDebug
./gradlew testDebugUnitTest
./gradlew lintDebug
```

Windows note (this machine): run via PowerShell with the loopback fix or builds hang:

```powershell
$env:_JAVA_OPTIONS = "-Djdk.net.unixdomain.tmpdir=C:\tmp -Djava.net.preferIPv4Stack=true"
& D:\Autominder\gradlew.bat clean assembleDebug --no-configuration-cache --no-daemon *> "$env:TEMP\am_build.txt"
"EXIT:$LASTEXITCODE"; Get-Content "$env:TEMP\am_build.txt" -Tail 40
```

If a command fails:
1. Stop.
2. Show first meaningful error.
3. Explain root cause.
4. Fix only that issue.
5. Re-run the failed command.

After green:
1. Capture AVD screenshots for touched screens (use the `autominder-visual-qa` skill procedure).
2. Summarize files changed.
3. Summarize defects fixed / visual impact.
4. List remaining risks honestly.
5. Recommend commit message. Do NOT commit without explicit user approval.
