---
paths:
  - "app/src/main/kotlin/com/autominder/app/ui/**/*.kt"
  - "app/src/main/kotlin/com/autominder/app/MainActivity.kt"
  - "app/src/main/res/**"
---

# AutoMinder UI Rules (loads when UI files are touched)

## Brand tokens — "Midnight Cobalt"
Authority: `docs/DESIGN_SYSTEM_2026.md`. Racing Teal `#006B5F` is **retired** —
it predates the Night Garage commit and must not be reintroduced.

Cobalt is both the accent **and** the healthy state, so only caution and critical
ever interrupt a screen. Reference values (always consumed through
`MaterialTheme.colorScheme`, never as literals):

| Role | Dark | Light |
|---|---|---|
| ground | `#0F1316` | `#F7F9FC` |
| surface | `#171C20` | `#FFFFFF` |
| primary (accent **and** OK) | `#7AB4FF` | `#0B4FC4` |
| caution (DUE_SOON) | `#EFB552` | `#7A5300` |
| critical (OVERDUE) | `#FF8A80` | `#B3261E` |
| muted (UNKNOWN, metadata) | `#98A4AD` | `#4A5568` |

**No dynamic/wallpaper colour.** `AutoMinderTheme.kt` has no dynamic-colour path
and must not gain one in v1.0 — Night Garage *is* the identity
(`DESIGN_SYSTEM_2026 §2`). Never pure black; elevation is a lighter surface,
never a shadow.

Type: JetBrains Mono for **all** numeric vehicle data (odometer, cost, distance,
dates). Header/body face is migrating Exo 2 + Nunito Sans → **Manrope**;
until the Manrope files land in `res/font/`, the shipped faces stay — do not
reference Manrope in code before it exists.

## The only valid states
`OVERDUE` · `DUE_SOON` · `SNOOZED` · `OK` · `COMPLETED` · `UNKNOWN`
(`domain/model/ServiceStatus.kt`)

`GOOD`, `DISABLED`, `NO_DATA`, `isEnabled` and any "health score" **do not
exist**. Never invent, render, or reason about them.

## Shape — set by component family, never by status
Card 16dp · sheet 28dp (top only) · field 14dp · pill/CTA 999dp.
**Status-dependent corner radii are removed.** A list whose radii vary per row
reads as broken, not informative — and BMW and Tesla both convey urgency with
zero shape variation. Status carries: icon + label + colour + rail + copy.

## Hard rules
- MaterialTheme.colorScheme only — never Color(0xFF..) in composables.
- All user-visible text in strings.xml — never hardcoded in .kt.
- Status is NEVER color-only: text + chip + colour + rail. Never shape.
- Never fabricate a value. An absent reading says "Mileage not added", never `0 km`.
- Every screen: Loading/Empty/Error/Success (+ offline/unavailable where
  relevant). No dead-end CTAs.
- LazyColumn/LazyRow always key = { item.id }. Pre-compute/remember formatted
  strings; remember{} cannot be called inside LazyListScope builders.
- Never render ServiceType.label — use localizedLabel() (ui/util/).
- Distances via DistanceFormat.grouped(); locale-format dates/currency.
- Plurals resources for quantities (lint enforces PluralsCandidate); never
  concatenate translated fragments; layouts tolerate long text and RTL.
- Deleting a string's last usage requires deleting the string (UnusedResources).

## Motion & loading
- animate*AsState for one value; updateTransition for coordinated state;
  Animatable only for gesture/interrupt control; no infinite animation
  without genuine ongoing activity; everything respects Motion.reduceMotion.
- **Zero layout-jump.** Skeleton blocks match the exact geometry of the content
  they replace. Skeletons appear only after 150ms — a faster load shows nothing
  rather than a flash. Under "Remove animations" they hold static at 0.55 alpha.
- Skeleton animation is an **opacity pulse** (0.40 → 0.70, 1000ms, ease-in-out),
  **not a translating shimmer sweep** — a moving gradient costs frames on every
  skeleton and is an infinite animation with no underlying activity
  (`DESIGN_SYSTEM_2026 §9`).
- NavigationSuiteScaffold owns navigation presentation; use pinned Material3
  Adaptive APIs for window info; no second window-size abstraction.

## Accessibility (every changed screen)
Usable at 2.0x font scale | meaningful TalkBack order + semantics |
stateDescription for non-text status | decorative images marked decorative |
targets ≥48dp | no clipped text.

## Forms (low-typing standard)
Prefill everything known (vehicle, date, last odometer, intervals, currency).
Correct keyboard per field. Optional fields behind progressive disclosure.
One primary CTA. Entered data survives validation errors, rotation, and
process death (rememberSaveable/SavedStateHandle for small transient state).
