# AutoMinder Design System 2026 — "Midnight Cobalt"

Source of truth for the v1.0 visual language. Feeds both the Kotlin theme in
`ui/theme/` and the Stitch design system used to generate reference screens.
If this document and the code disagree, **code wins — then fix this document.**

Every contrast figure below is computed to WCAG 2.1 relative luminance, not estimated.

---

## 1. The one rule everything else serves

**One colour, one meaning. Never share.**

| Colour | Meaning | Slot |
|--------|---------|------|
| Cobalt blue `#1557C8` | Brand / action / "tap me" | `primary` |
| Health green `#167A55` | Healthy / ok / completed | `tertiary` |
| Caution amber `#9A6700` | Due soon / attention | `secondary` |
| Critical red `#B42318` | Overdue / act now | `error` |

Blue is **not** the healthy state. That was the previous rule; it is retired.
When blue = action AND blue = healthy, users cannot tell if a cobalt element
means "tap me" or "you're fine." Separation makes both signals unambiguous.

ISO 2575 (the dashboard warning-light standard drivers already read fluently):
red stops, amber warns, calm reassures. Green = good is the universal
convention — traffic lights, health apps, finance dashboards. We adopt it.

**Voice:** calm, precise, effortless. A quiet co-pilot, never an alarm clock.
Short sentences, no jargon, no exclamation marks.

---

## 2. Colour

Dark is the primary theme — the app is used in driveways and garages, often
after dark. Light is a first-class requirement, not an afterthought.

### Dark (primary)

| Token | Hex | Role | Contrast |
|---|---|---|---|
| `ground` | `#0F1316` | app background | — |
| `surface` | `#171C20` | cards, sheets | — |
| `surfaceHigh` | `#1F262B` | raised / pressed | — |
| `primary` | `#7AB4FF` | accent **and** healthy state | 8.71:1 on ground |
| `onPrimary` | `#0F1316` | label on a cobalt fill | 8.71:1 |
| `ink` | `#E8EDF1` | body and headings | 15.83:1 |
| `muted` | `#98A4AD` | secondary, metadata | 7.33:1 |
| `caution` | `#EFB552` | due soon | 10.13:1 |
| `critical` | `#FF8A80` | overdue | 8.18:1 |
| `border/subtle` | `#2C353B` | decorative dividers | 1.49:1 — exempt, see below |
| `border/interactive` | `#606F7B` | selectable boundaries | 3.32:1 on surface ✓ |
| `border/selected` | `#7AB4FF` | selected reinforcement | 8.01:1 on surface ✓ |

### Light — Midnight Intelligence

| Token | Hex | Role | Contrast |
|---|---|---|---|
| `CloudWhite` | `#F7F9FC` | app background | — |
| `SurfaceWhite` | `#FFFFFF` | cards, sheets | — |
| `SurfaceBlue` | `#EEF4FD` | tonal cards / subtle elevation | — |
| `BlueTint` | `#DCE9FF` | primaryContainer / selected controls | — |
| `CobaltBlue` | `#1557C8` | **brand / action** (CTAs, FAB, active nav) | 6.28:1 on CloudWhite ✓ |
| `MidnightNavy` | `#102A56` | logo, key headings, onPrimaryContainer | — |
| `HealthGreen` | `#167A55` | **healthy / ok / completed** | 5.52:1 on CloudWhite ✓ |
| `HealthGreenContainer` | `#DDF5EA` | healthy tonal surface | — |
| `DueSoon` | `#9A6700` | **due soon / caution** | 6.61:1 on CloudWhite ✓ |
| `DueSoonContainer` | `#FFF0C7` | caution tonal surface | — |
| `Overdue` | `#B42318` | **overdue / critical** | 6.25:1 on CloudWhite ✓ |
| `OverdueContainer` | `#FEE4E2` | critical tonal surface | — |
| `Ink` | `#101828` | primary text / headings | 18.15:1 on CloudWhite ✓ |
| `Slate` | `#52627A` | secondary text / metadata | 7.21:1 on CloudWhite ✓ |
| `OutlineSubtle` | `#D4DDEB` | decorative dividers | 1.29:1 — exempt, see below |
| `OutlineInteractive` | `#77869A` | selectable boundaries | 3.71:1 on SurfaceWhite ✓ |

### Laws

- **Never pure black.** `#000000` maximises halation — light text haloes and
  bleeds, badly for readers with astigmatism — and smears on OLED during scroll.
  `#0F1316` stays dark while leaving headroom for elevation.
- **Elevation is a lighter surface, never a shadow.** Dark themes cannot show
  shadows. Depth comes from `ground → surface → surfaceHigh`.
- **Desaturate for dark.** Light-mode cobalt (`#0B4FC4`) vibrates on near-black.
  The dark tone is lighter and lower in chroma by design; they are not the same
  colour with a filter.
- All colour via `MaterialTheme.colorScheme`. **Zero `Color(0xFF..)` in composables.**
- **Borders are not uniformly strengthened.** WCAG 1.4.11 applies to boundaries
  that carry information required to identify a control or its state. A
  decorative divider does not. `border/subtle` stays quiet by design;
  `border/interactive`, `border/selected` and `border/focus` earn 3:1 because
  they are the only thing distinguishing a selectable tile from background.
  Making every hairline 3:1 would make the interface look heavy and cheap.
- **`disabled-content` measures 2.91:1 dark / 3.00:1 light and does not pass.**
  This is a deliberate exemption under WCAG 1.4.3 (inactive components are
  incidental), recorded here so it is never mistaken for an oversight.
- **Wallpaper-driven dynamic colour stays off.** Verified: `AutoMinderTheme.kt`
  contains no dynamic-colour path today. Night Garage is the identity; a
  system-colour opt-in is a post-launch appearance setting, not a default.

---

## 3. Status contract

Follows ISO 2575, the dashboard warning-light standard drivers already read
fluently: red stops, amber warns, green reassures. Blue is reserved for brand
and action — it never doubles as a health signal.

| Status | Token | Hex | Corner radius | Notification cadence |
|---|---|---|---|---|
| **OVERDUE** | `error` / `errorContainer` | `#B42318` | **8dp** — sharp = urgent | every 24h · cannot be snoozed |
| **DUE_SOON** | `secondary` / `secondaryContainer` | `#9A6700` | **16dp** | every 3 days |
| **HEALTHY / OK** | `tertiary` / `tertiaryContainer` | `#167A55` | **28dp** — soft = safe | never |
| **UNKNOWN** | `onSurfaceVariant` / `surfaceVariant` | `#52627A` | 16dp | never |
| **COMPLETED** | `tertiary` / `tertiaryContainer` | `#167A55` | 16dp | never |

**Status is never colour alone.** Every status carries four channels: colour,
corner radius, a text label, and a leading rule or icon. This survives
greyscale, colour-blindness, and a photograph of the screen.

Corner morphing animates with `animateFloatAsState` — never `animateFloat`.

### Semantic independence

`status/good` and `action/primary` alias the same cobalt reference tone today
but are **separate semantic roles**. Retuning the action blue must never
silently change the meaning of "this vehicle is up to date."

| Role group | Members |
|---|---|
| Action | `brand/accent`, `action/primary`, `navigation/selected` |
| Maintenance state | `status/good/*`, `status/due/*`, `status/overdue/*`, `status/nodata/*` |

### The Precision Rail — already shipped, keep as-is

`PremiumStatusStyle.railColor()` returns the leading accent rule for OVERDUE and
DUE_SOON and **null for calm states** — "calm states don't shout." That is
already correct: GOOD gets no rail. The rail is decorative reinforcement; status
is carried independently by chip and text, so the rail never becomes the only
signal.

---

## 4. Typography

**Two families. Exo 2 is retired.**

Exo 2 appeared only on vehicle names and hero numbers — a whole font download
for two roles a heavier weight can carry. Dropping it cuts APK weight and one
font-loading pass, which serves the "fast loading" goal directly.

| Family | Weights | Role |
|---|---|---|
| **Manrope** | 400 / 500 / 700 / 800 | everything: display, headings, titles, body, labels |
| **JetBrains Mono** | 500 / 600 | data only: odometer, cost, distance, dates |

Manrope is geometric, modern, and has genuinely good tabular figures at heavy
weights. JetBrains Mono stays because tabular data is a different job — columns
of numbers must align and `0` must never be mistaken for `O`.

**Never mix roles.** No Manrope odometer readings, no mono body text.

### Scale

| Role | Size / line | Weight | Tracking |
|---|---|---|---|
| Display | 34 / 38 | 800 | −0.02em |
| Headline | 26 / 31 | 700 | −0.02em |
| Title | 19 / 25 | 700 | −0.01em |
| Body | 15 / 23 | 400 | 0 |
| Label | 13 / 17 | 500 | 0 |
| Micro (caps) | 11 / 15 | 600 | 0.12em |
| Data L | 30 / 34 | 600 | −0.01em |
| Data M | 15 / 20 | 500 | 0 |

**Micro caps are capped at two words.** They were the failure point in the
earlier explorations — at 2.0× font scale, long uppercase labels wrap and break
layouts. If a label needs more than two words, it is not a micro label.

Every screen must remain usable at **2.0× font scale**. No fixed-height text
containers.

---

## 5. Spacing, shape, layout

- **4dp grid.** Screen padding 20dp. Card padding 16dp. Section gap 24dp.
- **Radii:** card 16, sheet 28 (top only), field 14, pill/CTA 999, status per §3.
- **Touch targets ≥ 48dp** always, including icon buttons and list-row actions.
- **Text measure:** body copy never exceeds ~60 characters per line.

---

## 6. Iconography

- **Material Symbols Rounded** — matches the geometric warmth of Manrope.
- Weight 400 · optical size 24 · **grade 0 in light, grade −25 in dark**
  (icons optically bold on dark grounds; negative grade compensates).
- **Filled = active or selected. Outlined = inactive.** This gives navigation a
  second, non-colour channel, same principle as the status system.
- Icons are never the only label on a primary action.
- Decorative icons are marked decorative for TalkBack; meaningful icons carry
  a content description.

---

## 7. Navigation

Four destinations, fixed: **Home · Vehicles · Records · Settings.**

- Material 3 Expressive `NavigationBar`, filled icon on the active destination.
- **No centre FAB in the navigation bar.** The earlier explorations had three
  different navigation models — one with a floating centre button. That pattern
  hides a destination behind a control that looks like an action. A screen-level
  FAB on Home and Records is correct; a FAB inside the nav bar is not.
- Type-safe `@Serializable` routes only. No raw route strings.
- Predictive back must be honoured on every screen with an in-progress form.
- Tab labels live in `strings.xml` — never hardcoded.

---

## 8. Components

**Primary CTA** — 56dp, radius 999, `primary` fill, `onPrimary` label, Title
weight. **One per screen.** Sticky to the bottom on any scrolling form.

**Text field** — `OutlinedTextField`, radius 14, leading icon, single line,
correct keyboard per field. Numeric fields use JetBrains Mono.

**Card** — `surface`, radius 16, 16dp padding, no shadow in dark.

**Status row** — leading 3dp rule in the status colour, title, trailing value in
mono. The core repeating unit of the app.

**Bottom sheet** — radius 28 top, drag handle, `surface`.

**Forms** — required fields visible; optional fields folded behind "Add more
details". Prefill everything already known: vehicle, date, last odometer,
intervals, currency. Entered data survives validation errors, rotation, and
process death.

---

## 9. State matrix — every screen ships all five

| State | Rule |
|---|---|
| **Loading** | Content-shaped skeleton. Never a bare spinner. |
| **Empty** | Illustration + one sentence + one action. Never a dead end. |
| **Error** | What went wrong, and the button that fixes it. No apologies. |
| **Success** | The data, with truthful absences (see below). |
| **Offline** | The app is offline-first — say what still works, don't block. |

### Skeleton specification

- Skeleton blocks match the **exact geometry** of the content they replace, so
  there is zero layout shift on load.
- **No shimmer sweep.** A translating gradient costs frames on every skeleton
  and reads as dated. Use an opacity pulse `0.40 → 0.70`, 1000ms, ease-in-out.
- With "Remove animations" enabled, skeletons hold static at 0.55 opacity.
- Skeletons appear only after 150ms — faster loads should show nothing at all
  rather than a flash.

### Truthful absence

Never render a fabricated zero. If mileage was never entered, the UI says
"Mileage not added" — not `0 km`. An absent value and a zero value are
different facts, and the interface must not conflate them.

---

## 10. Motion

Spring-based, per Material 3 Expressive. No ad-hoc `tween` durations in screens.

| Token | Damping | Stiffness | Use |
|---|---|---|---|
| `spatialFast` | 0.9 | 700 | chips, small toggles |
| `spatialStandard` | 0.8 | 380 | cards, sheets, shared transitions |
| `effects` | 1.0 | 1600 | colour, alpha — never overshoots |

All amplitude multiplies `Motion.amplitude`, which is **0** when the system
"Remove animations" setting is on. No infinite animation without genuine
ongoing activity.

---

## 11. Performance contract

The design is not done if it does not scroll at 120Hz.

- `LazyColumn` / `LazyRow` always take `key = { item.id }` and `contentType`.
- Formatted strings — dates, currency, distances — are computed **outside** the
  item composable. `remember {}` cannot be called inside `LazyListScope`.
- No nested vertical scrolling containers.
- Images load through Coil with an explicit target size; a vehicle photo is
  never decoded at full resolution into a 120dp slot.
- Baseline Profile is regenerated whenever navigation or list rendering changes.
- Every list item is stable — no unstable lambdas or unstable data classes in
  the item signature.

---

## 11b. No-photo law

**Night Garage must look premium with no vehicle photograph.**

`Vehicle.photoUri` is nullable and Add Vehicle folds the photo behind
progressive disclosure, so the realistic majority case is **no photo at all**.
A design carried by photography is therefore a design most users never see.

The premium baseline is built from make and model, year, mileage, status,
typography, cobalt hierarchy, and a truthful generic vehicle treatment.
A user photo is progressive enhancement layered on top of that baseline —
never the scaffolding holding it up.

**Never substitute:** manufacturer press imagery, stock photography standing in
for the user's car, or AI-generated vehicles. A missing photo is a fact about
this user's garage, not a hole to be filled with a prettier lie.

The no-photo state is a **required proof state** for Vehicles, Vehicle Detail
and Home. It is proven first, not last.

---

## 11c. Existing brand signatures — reconciled with shipped code

Verified against the repository, not from memory.

| Signature | Where | Verdict |
|---|---|---|
| **Precision Rail** | `VehicleHeroCard.kt:64` → `PremiumStatusStyle.railColor()` | **KEEP** — already returns null for calm states |
| **Verdict Sentence** | `VehicleListScreen.kt:144` | **KEEP** — retone to Night Garage |
| **Tabular Truth Marks** | no occurrence anywhere | **DOES NOT EXIST** — do not implement from memory |

Twelve premium components already ship under `ui/components/premium/`. This
system retones them; it does not replace them. Any new primitive must justify
itself against that inventory first.

---

## 12. Scope — what must not be drawn

v1.0 has no telemetry, no accounts, and no backend. The design system therefore
provides **no components** for the following, so nothing can accidentally ship a
convincing-looking lie:

| Reserved for v1.1+ | Why there is no component |
|---|---|
| OBD-II gauges (oil life, tyre pressure, battery %) | No sensor data exists. A gauge would be fabricated. |
| Cloud sync / "Synced" badges | Offline-first. There is no server to sync with. |
| Receipt OCR progress | Not built. |
| Login / account / security screens | The app has no accounts. |
| Family sharing | Not built. |
| Vehicle valuation | Requires a pricing API. |

When these ship, they get designed then. Until then, a screen may only show
something the user themselves entered.

---

## 13. Applying this in Stitch

1. Upload this file with `upload_design_md`.
2. Build the system with `create_design_system_from_design_md`.
3. Regenerate with `apply_design_system` — dark first, then light parity.
4. Delete every screen listed in §12 from the reference project.

Screens generated before this system exists are exploration, not reference.
