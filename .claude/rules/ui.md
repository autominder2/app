---
paths:
  - "app/src/main/kotlin/com/autominder/app/ui/**/*.kt"
  - "app/src/main/kotlin/com/autominder/app/MainActivity.kt"
  - "app/src/main/res/**"
---

# AutoMinder UI Rules (loads when UI files are touched)

## Brand tokens
Racing Teal #006B5F primary | Exo 2 (display: vehicle names, big numbers) |
Nunito Sans (body) | JetBrains Mono (odometer, cost, km, dates).
Status containers: OVERDUE=errorContainer | DUE_SOON=tertiaryContainer |
GOOD=secondaryContainer. Status corners: OVERDUE=8dp DUE_SOON=16dp GOOD=28dp.
Full sheet + 12 premium UI rules: DESIGN_SYSTEM.md, autominder-ui-slice skill.

## Hard rules
- MaterialTheme.colorScheme only — never Color(0xFF..) in composables.
- All user-visible text in strings.xml — never hardcoded in .kt.
- Status is NEVER color-only: text + chip + color + rail/corner.
- Every screen: Loading/Empty/Error/Success (+ offline/unavailable where
  relevant). No dead-end CTAs.
- LazyColumn/LazyRow always key = { item.id }. Pre-compute/remember formatted
  strings; remember{} cannot be called inside LazyListScope builders.
- Never render ServiceType.label — use localizedLabel() (ui/util/).
- Distances via DistanceFormat.grouped(); locale-format dates/currency.
- Plurals resources for quantities (lint enforces PluralsCandidate); never
  concatenate translated fragments; layouts tolerate long text and RTL.
- Deleting a string's last usage requires deleting the string (UnusedResources).

## Motion & adaptive
- animate*AsState for one value; updateTransition for coordinated state;
  Animatable only for gesture/interrupt control; no infinite animation
  without genuine ongoing activity; everything respects Motion.reduceMotion.
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
