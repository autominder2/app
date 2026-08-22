---
description: Use to create or review premium Jetpack Compose Material 3 components for AutoMinder. Enforces presentational components, visual hierarchy, accessibility, and performance safety.
allowed-tools: Read, Grep, Glob, Bash, Edit, Write
effort: high
---

# Premium Compose Components for AutoMinder

## Goal

Create reusable, dumb, premium Material 3 components in `app/src/main/kotlin/com/autominder/app/ui/components/premium/`.

Target feel: **BMW service cockpit + digital glovebox + proactive maintenance assistant.**

Fleet-inspired (behance.net/gallery/250251481) design logic — hero-first, bold metric moments, insight-then-action — translated to Racing Teal Material 3. Never copy Fleet's yellow/purple palette or fake content.

## Component rules

- No ViewModel access. No Repository/DAO access. No navigation. No Billing/Ads/WorkManager.
- Text passed in already-localized, or via `@StringRes` resolved at the caller.
- Explicit parameters; actions are lambdas.
- Icons: meaningful icons get contentDescription; decorative icons get `null`.
- `MaterialTheme.colorScheme` only — no raw `Color(0xFF...)`.
- `MaterialTheme.typography` (Exo 2 display / Nunito Sans body / JetBrains Mono data).
- Existing `Shapes` and `Motion.kt` (respect `Motion.reduceMotion`).
- No infinite animation.
- Large-text safe: no fixed heights on text containers; test mentally at 2.0x.
- 48dp+ touch targets where interactive.
- `semantics(mergeDescendants = true)` on card-level summaries.
- Pre-formatted display values expected as inputs (use `DistanceFormat.grouped()` at the caller) — never format inside item-level composition.

## Expected kit (create only when the slice approves)

- `VehicleHeroCard` — Expanded (detail hero) / Compact (list row); photo or primaryContainer icon tile; Exo 2 name; Mono odometer; status chip slot.
- `HealthCockpitCard` — Fleet/Single variants; human-verdict headline ("7 services need attention"), score ring demoted to instrument. Never a lone giant "0".
- `InsightMetricCard` — eyebrow label + Mono value + unit; single / 2-up grid.
- `ProactiveAttentionCard` — service, one-line why (mileage-overdue phrasing), single CTA.
- `StatusReminderCard` — status corner morphing 8/16/28dp via `animateDpAsState` + `Motion.springDefault`; 4dp error rail for OVERDUE; Mono timing values.
- `RecordsTimelineCard` — icon tile, title, vehicle name, Mono date + cost right-aligned, subtle tertiary rail.
- `FormSectionCard` — title + helper + content slot + adjacent error slot, on `surfaceContainerLow`.
- `PremiumSectionHeader` — title + optional count badge + optional trailing action; `heading()` semantics.
- `PremiumActionGrid` — 2×2 FilledTonal tiles, ≥56dp.
- `PremiumPaywallPlanCard` — period, Mono price or loading, badge, selected = tonal lift + 1.02 scale via `Motion.springSnappy`.

## Design rules

- Vehicle identity must feel emotional, not like a database row.
- Status card must use: text + icon/chip + semantic container color + shape/accent rail.
- Forms must feel guided: What service? / When? / Repeat rules / Optional notes / Save.
- Records must feel like a timeline: month header, service icon, title, vehicle name, date/cost/odometer metadata.

## Output

Report: components created; inputs and variants; accessibility behavior; performance safeguards; screens expected to consume each; build/test/lint results (release-hardening gate from `autominder-ui-slice` skill).
