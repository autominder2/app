# AutoMinder Design System — v1.0
Single source of truth for UI decisions. Mirrors the Figma file
**AutoMinder — Brand & Design System** (https://www.figma.com/design/1hZFh9n8uovpqISCaz7T3d)
and the code tokens in `ui/theme/`. If this doc and code disagree, code wins — then fix this doc.

## Brand
- **Name:** AutoMinder · **Developer:** TikiTaka3D · **Primary:** Racing Teal `#006B5F`
- **Personality:** calm, precise, effortless. The app is a quiet co-pilot, never an alarm clock.
- **Voice:** short sentences, no jargon, no exclamation marks. "Gentle reminders arrive before things are due."

## Color (M3 HCT, seed #006B5F) — `ui/theme/Color.kt`
| Token | Light | Dark |
|---|---|---|
| primary | #006B5F | #006B5F |
| primaryContainer | #9CF2E1 | #005048 |
| onPrimaryContainer | #00201C | #9CF2E1 |
| secondary | #4A635E | #4A635E |
| secondaryContainer | #CCE8E1 | #334B46 |
| tertiary (amber) | #7D5700 | #7D5700 |
| tertiaryContainer | #FFDEA6 | #5C4000 |
| error | #BA1A1A | #BA1A1A |
| errorContainer | #FFDAD6 | #93000A |
| surface | #F5FBF8 | #0F1512 |
| surfaceContainer | #EFF5F2 | #1A201E |
| surfaceContainerHigh | #E9EFEC | #252B28 |
| outline | #6F7976 | #899390 |

**Law:** all color via `MaterialTheme.colorScheme`. Zero `Color(0xFF..)` in composables.

## Status system — color + corner radius carry meaning
| Status | Container | Radius | Notification cadence |
|---|---|---|---|
| OVERDUE | errorContainer / onErrorContainer | **8dp** (sharp = urgent) | every 24h · cannot be snoozed |
| DUE_SOON | tertiaryContainer / onTertiaryContainer | **16dp** | every 3 days |
| GOOD | secondaryContainer / onSecondaryContainer | **28dp** (calm = safe) | never |

Corner morphing animates with `animateFloatAsState` (never `animateFloat`).
StatusCalculator: raw status first; OVERDUE always beats SNOOZED.

## Typography — three voices, strict roles — `ui/theme/Type.kt`
- **Exo 2** Bold/ExtraBold — display + headlines: vehicle names, health score, hero numbers, screen titles.
- **Nunito Sans** 400–700 — titles, body, labels: every piece of UI text.
- **JetBrains Mono** Medium/SemiBold — data: odometer, costs, km, dates. Tabular figures, clear 0/O.

Never mix roles (no Exo 2 body text, no Nunito odometer readings).

## Components
- **Primary CTA:** `Button`, 56dp height, radius 16, `titleMedium` Bold. One per screen.
- **Text fields:** `OutlinedTextField`, radius 14, leading icon, single line.
- **Forms:** required fields visible; optional fields folded behind "Add more details".
- **Lists:** `LazyColumn` with `key = { item.id }`, swipe-to-delete via `SwipeToDeleteContainer`.
- **Every screen ships 4 states:** Loading (skeleton — never a bare spinner), Empty, Error, Success.

## Motion — `ui/theme/Motion.kt`
- Springs only, from the shared `Motion` vocabulary. No ad-hoc `tween` durations in screens.
- Parallax/slide amplitude multiplies `Motion.amplitude` (0 when system "Remove animations" is on).
- Save buttons morph (SaveButton); step/page changes slide + fade.

## Haptics vocabulary
| Event | Type |
|---|---|
| Successful save | `Confirm` |
| Validation error | `Reject` |
| Step/page settle | `SegmentTick` |
| Switch on/off | `ToggleOn` / `ToggleOff` |

One-buzz-for-everything is forbidden.

## Layout
4dp grid · screen padding 20–24dp.
Bottom nav (verified against code 2026-07): **Home / Vehicles / Records / Settings** — 4 tabs.
Records = the ServiceHistory route: confirmed cross-vehicle (`ServiceWithVehicle`, grouped by
month), so it earns a top-level tab rather than living inside Vehicle Detail. It holds service
records only for now — Fuel History and Mileage Log are still separate per-vehicle screens, not
merged in. Revisit the name (e.g. "Glovebox") only once/if those get folded into one hub.
Navigation via `@Serializable` NavRoutes — raw route strings forbidden.
Nav tab labels live in strings.xml (`nav_home`, `nav_vehicles`, `nav_records`, `nav_settings`) —
never hardcode label strings in `BottomNavBar.kt`.

## Onboarding doctrine
Activation-first: Welcome → Add your car (chips + 3 fields) → reminders permission asked in context.
No feature tours. User lands on a live dashboard with default reminders.

## Data display
Money: stored as Int cents, rendered `cents / 100.0` in JetBrains Mono.
Distance: stored km, rendered in the user's unit via `DistanceUtil` + `LocalDistanceUnit`.
