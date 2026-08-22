# AutoMinder — Complete UI Audit (2026 Standards)

**Date:** 2026-07-17 · **Scope:** All 16 screens + design system + shared components · **Standard:** Material 3 Expressive (BOM 2025.06.01), WCAG 2.1 AA, 2026 mobile design conventions
**Verdict in one line:** A genuinely strong M3 Expressive foundation (top 10% of indie apps) undermined by *inconsistent application* — three flagship premium components are built but never used, two loading patterns coexist, and the same concept (vehicle card, service-type picker, numeric typography) renders differently on different screens.

**System score: 78/100** — Tokens 92 · Components 80 · Consistency 62 · Accessibility 76 · Modernity (2026) 72

---

## 1. Design System Audit

### Token coverage (excellent)
| Category | Defined | Hardcoded violations found |
|----------|---------|---------------------------|
| Colors | Full M3 scheme incl. all surfaceContainer tiers | **0** `Color(0x..)` outside theme ✅ |
| Typography | Exo 2 / Nunito Sans / JetBrains Mono, full scale | 0 custom sizes ✅ — but mono is **not applied** on several money/odometer texts (see 1.3) |
| Strings | strings.xml throughout | **0** hardcoded `Text("...")` ✅ |
| Shapes | 4/8/16/28/40 scale | ~6 instances of off-scale `RoundedCornerShape(14.dp)` in text fields (Onboarding, AddVehicle) and `12.dp` photo clip |
| Motion | `Motion.kt` — springs, easings, stagger, **reduce-motion aware** ✅ | Dashboard FAB uses inline `spring(0.6f, ...)` instead of `Motion.springSnappy()` |

### 1.1 CRITICAL — Orphaned premium components (built, never wired)
| Component | Status | Should power |
|-----------|--------|--------------|
| `PremiumPaywallPlanCard` + `PremiumPriceDisplay` | **Dead code** | `ProPaywall` (still uses 3 stacked plain buttons) |
| `RecordsTimelineCard` | **Dead code** | `ServiceHistoryScreen` (still uses plain `ElevatedCard`) |
| `FormSectionCard` | **Dead code** | Add/Edit form screens (fields float unsectioned) |

This is the single biggest finding: the v1.1 "premium redesign" is ~40% already written and sitting unused in `ui/components/premium/`.

### 1.2 Loading-state inconsistency
Dashboard → `DashboardSkeleton` ✅ · Records → `ListSkeleton` ✅ · **Vehicles → `LoadingState` spinner ❌ · Vehicle Detail → spinner ❌**. 2026 baseline: skeletons everywhere content shape is known.

### 1.3 Numeric typography drift (brand rule: JetBrains Mono for odometer/costs/dates)
| Screen | Money/odometer font | Compliant? |
|--------|--------------------|-----------|
| Vehicle Detail odometer instrument | JetBrains Mono | ✅ |
| VehicleHeroCard meta line | JetBrains Mono | ✅ |
| ServiceHistoryCard cost | Nunito (titleMedium) | ❌ |
| FuelEntryCard cost/odometer/liters | Nunito | ❌ |
| Cost summary card (Vehicle Detail) | Nunito | ❌ |
| InsightMetricCard values | (verify) | — |

### 1.4 Same concept, two patterns
- **Service-type selection:** AddService → `ServiceTypeGrid` (great) · AddReminder → `ExposedDropdownMenuBox` (dated). Unify on the grid.
- **Vehicle cards:** Dashboard → `VehicleHeroCard` (photo, status chip, merged a11y) · Vehicles tab → bespoke `VehicleListCard` (no status chip, no health signal, weaker semantics). The Vehicles tab is strictly worse at the same job.
- **Efficiency display:** FuelHistory `EfficiencyAtAGlance` (primaryContainer, centered) vs Vehicle Detail Pro card (tertiaryContainer, left). Same metric, two visual identities.

---

## 2. Screen-by-Screen Findings

Severity: 🔴 fix for v1.1 launch · 🟡 should fix · 🟢 polish

### 2.1 Onboarding
Strong: activation-first flow (leaves with a real car + reminders), animated segmented progress, step accent gradient, haptic ticks, `imePadding`, reduce-motion-aware transitions, in-context notification ask.
| Finding | Sev | Recommendation |
|---|---|---|
| `GlowHero` static icon-in-circles reads 2020, not 2026 | 🟡 | Replace with animated vector/Lottie or M3 Expressive shape-morph hero (deferred "Garage hero silhouettes" ties in) |
| Step change not announced to TalkBack; progress bars are decoration only | 🟡 | `semantics { liveRegion }` on step title, `progressBarRangeInfo` on the indicator row |
| Text fields `RoundedCornerShape(14.dp)` off token scale | 🟢 | Use `MaterialTheme.shapes.medium` |
| `POPULAR_MAKES` LazyRow `items()` without keys | 🟢 | `key = { it }` |
| No dark-gradient tuning: `primaryContainer→background` gradient in dark mode is muted | 🟢 | Verify dark rendering |

### 2.2 Dashboard (flagship — best screen in the app)
Strong: cockpit verdict headline ("a sentence, not a naked number"), score ring demoted to instrument, skeleton loading, all 4 states, FAB speed dial with rotation, merged TalkBack descriptions, plurals, `animateItem()`.
| Finding | Sev | Recommendation |
|---|---|---|
| FAB speed-dial has **no scrim and no back-press handling** — tapping content doesn't close it | 🔴 | Add scrim + `BackHandler`, or adopt M3 `FloatingActionButtonMenu` (in BOM 2025.06) |
| Speed-dial items not announced as menu; no `expand/collapse` state semantics | 🟡 | `stateDescription` + Role on the FAB |
| Greeting computed once per composition — stale across midnight/resume | 🟢 | Key on lifecycle resume |
| Inline `spring()` for FAB rotation bypasses `Motion` | 🟢 | `Motion.springSnappy()` |

### 2.3 Vehicles tab
| Finding | Sev | Recommendation |
|---|---|---|
| Duplicates Dashboard's job with a weaker card: no status chip, no attention counts, no merged content description | 🔴 | Replace `VehicleListCard` with `VehicleHeroCard(Compact)` + `StatusChip` (needs `VehicleWithStatus` in VM) |
| Spinner instead of skeleton | 🟡 | Reuse `ListSkeleton` |
| Card click semantics: no `Role.Button`, no state announced | 🟡 | Merged semantics like Dashboard |

### 2.4 Vehicle Detail
Strong: hero-owns-the-title app bar handoff, tappable odometer instrument, triage (worst 3 + fold), FIXD-style detail sheet, all-clear banner, honest `HealthSetupCard`, snackbars for every mutation.
| Finding | Sev | Recommendation |
|---|---|---|
| Spinner instead of skeleton on the richest screen | 🟡 | Dedicated `VehicleDetailSkeleton` |
| Pro cost/fuel cards use raw `ElevatedCard` + non-mono numerals | 🟡 | Mono for money; consider `InsightMetricCard` reuse |
| `AllClearBanner` + AddVehicle "magic" banner + EmptyState hint are three bespoke implementations of the same "info banner" | 🟢 | Extract one `InfoBanner` component |
| Odometer `displaySmall` + long km value can clip on small screens at font scale 2.0 | 🟡 | autosize or `titleLarge` fallback |

### 2.5 Records (Service History)
| Finding | Sev | Recommendation |
|---|---|---|
| `RecordsTimelineCard` exists for exactly this screen and is unused | 🔴 | Wire it (service-type icon, mono cost, timeline rail) |
| No service-type icon on rows (`ServiceTypeIcons` util exists, unused here) | 🟡 | Include in timeline card |
| `NumberFormat.getCurrencyInstance` allocated per card recomposition | 🟢 | `remember` it |
| Sticky month headers are plain text — fine, but no year grouping or spend-per-month summary | 🟢 | Month header + monthly total (2026 "insight-dense lists") |
| No search/filter for a cross-vehicle history | 🟡 | v1.1: filter chips by vehicle/type |

### 2.6 Settings
| Finding | Sev | Recommendation |
|---|---|---|
| Flat divider-separated rows read 2019; M3 2026 convention is grouped cards (surfaceContainer sections, 28dp group corners — cf. modern Google apps) | 🟡 | Group Preferences / About into rounded section containers |
| **No dynamic color (Material You) option** — app ignores Android 12+ wallpaper theming entirely | 🟡 | Optional "Use device colors" toggle via `dynamicLightColorScheme()` w/ Racing Teal fallback (theme-layer change) |
| Upgrade card: both card AND inner button clickable → double tap target, TalkBack reads twice | 🟡 | One clickable surface |
| Icons carry `contentDescription` duplicating the row label → TalkBack reads labels twice | 🟡 | `contentDescription = null` for decorative row icons |
| Theme switch applies but no preview affordance | 🟢 | Acceptable |

### 2.7 Add Vehicle (excellent form design)
Strong: tap-don't-type make chips, year quick-pick, progressive disclosure ("More details"), discard-changes guard, morphing `SaveButton`, photo picker with persistable permission.
| Finding | Sev | Recommendation |
|---|---|---|
| Interstitial ad fires immediately after first save — highest-churn moment | 🟡 | Product call: suppress on first-ever vehicle save |
| Field corner 14dp off-token | 🟢 | shapes.medium |
| Make/year chips rows lack `selectableGroup()` semantics | 🟡 | Add for TalkBack radio-group navigation |

### 2.8 Add Service / Add Reminder / Add Fuel (+ Edit variants)
Strong: staggered `FormField` entrances, haptic confirm/reject, save-button-as-confirmation (650ms morph), discard guards, date pickers.
| Finding | Sev | Recommendation |
|---|---|---|
| AddReminder uses dropdown for service type while AddService uses `ServiceTypeGrid` | 🔴 | Unify on grid |
| `FormSectionCard` unused — long forms have no visual grouping | 🟡 | Section forms: What / When / Cost & notes |
| Odometer/cost inputs: no mono font, no unit suffix affordance | 🟢 | Mono input text style |

### 2.9 Fuel History / Mileage Log
| Finding | Sev | Recommendation |
|---|---|---|
| `FuelEntryCard` leads with the date; the user's question is "what mileage am I getting?" | 🟡 | Lead with efficiency + cost; date demoted; mono numerals |
| Swipe-to-delete with undo snackbar ✅ — but no delete confirmation for rows swiped fast; undo is sufficient | 🟢 | OK |
| `EfficiencyAtAGlance` diverges from Vehicle Detail efficiency card | 🟡 | One shared component |

### 2.10 Service Detail / About
About: `LargeTopAppBar` collapse ✅, clean. Service Detail follows detail conventions.
| Finding | Sev | Recommendation |
|---|---|---|
| About is plain text stack — fine for v1.0 | 🟢 | v1.1: app icon hero + link cards |

### 2.11 ProPaywall
| Finding | Sev | Recommendation |
|---|---|---|
| Plain stacked buttons; `PremiumPaywallPlanCard` (selectable cards, badge, mono price, radio semantics) sits unused | 🔴 | Wire plan cards + single "Continue" CTA — the 2026 paywall pattern; typically measurable conversion lift |
| `FeatureCheck` ✓/✕ icons have `contentDescription = null` → TalkBack users cannot hear which tier includes what | 🔴 | Announce "included"/"not included" (WCAG 4.1.2) |
| 8 rows × dividers = visual noise | 🟢 | Zebra or spacing instead of 8 dividers |
| No trial/anchor copy, no per-month equivalence on yearly | 🟡 | "≈ $X.XX/mo" subtitle via plan card `subtitle` |

---

## 3. Accessibility (WCAG 2.1 AA)

**Overall: better than most production apps** (merged descriptions, haptics vocabulary, reduce-motion global, plurals, RTL-safe AutoMirrored icons). Issues:

| # | Issue | WCAG | Sev | Fix |
|---|---|---|---|---|
| 1 | Paywall ✓/✕ convey tier inclusion by icon only, unannounced | 1.1.1 / 4.1.2 | 🔴 | contentDescription per state |
| 2 | `EmptyState` subtitle `onBackground.copy(alpha=.6)` ≈ 3.8:1 on light surface — below 4.5:1 for 16sp | 1.4.3 | 🔴 | Use `onSurfaceVariant` token (4.6:1+), never alpha for text |
| 3 | `StatusReminderCard` `timingSecondary` at `contentColor.copy(alpha=.65)` on tinted containers — likely < 4.5:1 (12sp) | 1.4.3 | 🟡 | ≥ 0.75 alpha or dedicated on-container variant |
| 4 | FAB speed dial: no expanded/collapsed state, no scrim, items unreachable order | 4.1.2 / 2.4.3 | 🔴 | M3 FAB menu or semantics + scrim |
| 5 | Onboarding step change silent for screen readers | 4.1.3 | 🟡 | liveRegion on step title |
| 6 | Settings row icons + trailing chevrons announce duplicate/irrelevant labels | 1.1.1 | 🟡 | null decorative descriptions |
| 7 | Filter-chip rows (makes, years) lack `selectableGroup` | 1.3.1 | 🟡 | Add |
| 8 | Health ring: handled ✅ (merged semantics + scalable center text) | — | — | Keep |
| 9 | Touch targets: IconButtons default 48dp ✅; StatusChip is non-interactive ✅ | 2.5.5 | — | Pass |

Contrast spot-checks (light theme): Primary #006B5F on white ≈ 5.9:1 ✅ · onErrorContainer #410002 on #FFDAD6 ≈ 12:1 ✅ · onTertiaryContainer #281900 on #FFDEA6 ≈ 13:1 ✅ · Outline-on-surface small text (VehicleListCard odometer, ServiceHistoryCard date use `outline` #6F7976 ≈ 4.6:1) — passes but borderline; prefer `onSurfaceVariant`.

---

## 4. What Works Well (keep in v1.1)
- Cockpit verdict pattern — headline sentence + demoted score ring: better than every competitor's naked gauge.
- Status speaks 4 ways (chip text, container color, corner morph 8/16/28dp, accent rail) — color-blind-safe by design.
- Motion system: one physical character app-wide, honest reduce-motion collapse.
- Truth contract on timing lines (overdue-by-mileage never hides behind a future date).
- Activation-first onboarding; tap-don't-type forms; discard guards; morphing save button; undo-based deletes.
- Token discipline: zero hardcoded colors/strings in 16 screens is exceptional.

---

## 5. Prioritized v1.1 Redesign Roadmap

**P0 — wire what's already built + a11y blockers (days)**
1. ProPaywall → `PremiumPaywallPlanCard` selectable cards + accessible ✓/✕ (conversion + WCAG).
2. Records → `RecordsTimelineCard` with `ServiceTypeIcons` + mono costs.
3. Vehicles tab → `VehicleHeroCard(Compact)` + `StatusChip` + status in VM.
4. EmptyState/secondary-text contrast: alpha → tokens.
5. FAB speed dial: scrim + BackHandler + state semantics (or M3 FabMenu).

**P1 — consistency pass (1–2 weeks)**
6. Skeletons everywhere (Vehicles, Vehicle Detail).
7. Mono numerals on every money/odometer/liters text (ServiceHistory, Fuel, cost summary).
8. AddReminder service-type grid; `FormSectionCard` sectioning across all 6 form screens.
9. Settings: grouped-card sections + optional dynamic color (Material You) toggle.
10. One shared `InfoBanner`; one shared efficiency card; off-token corner cleanup (14dp → tokens).

**P2 — 2026 signature moves (the "wow" layer)**
11. Onboarding hero: shape-morph/Lottie animation (pairs with deferred garage silhouettes).
12. Records: month headers with spend totals; vehicle/type filter chips.
13. Predictive-back polish + shared-element transition Dashboard card → Vehicle Detail (Compose 1.7+ `SharedTransitionLayout`).
14. Paywall anchor pricing ("≈ $X/mo") + trial copy.
15. Home-screen "next due" glance widget (Glance dep already present) — separate v1.1 task.

*Deliberately out of scope per CLAUDE.md deferred list: OBD-II, VIN decode, cloud sync, OCR, family sharing.*
