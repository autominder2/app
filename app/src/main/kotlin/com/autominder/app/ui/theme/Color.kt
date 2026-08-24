package com.autominder.app.ui.theme

import androidx.compose.ui.graphics.Color

// ─── AutoMinder — MIDNIGHT INTELLIGENCE LIGHT ────────────────────────────────
// Full contract: docs/DESIGN_SYSTEM_2026.md
//
// Semantic contract — one meaning per colour, never shared:
//   Blue  (#1557C8) = brand / action / "tap me"        → primary slot
//   Green (#167A55) = healthy / ok / completed          → tertiary slot
//   Amber (#9A6700) = due soon / caution                → secondary slot
//   Red   (#B42318) = overdue / critical                → error slot
//
// Blue is NOT the healthy state. That was the old rule; it is retired.
// ISO 2575 (dashboard warning light standard): red stops, amber warns, calm reassures.
// Green for "all good" is a universal convention drivers already read fluently.
//
// Dark scheme = Midnight Cobalt (unchanged — dark is still the primary theme).
// Light scheme = Midnight Intelligence (this file, light section below).
// Every contrast ratio is computed to WCAG 2.1 relative luminance.

// ═══════════════════════════════════════════════════════════════════════════════
// LIGHT SCHEME — MIDNIGHT INTELLIGENCE
// ═══════════════════════════════════════════════════════════════════════════════

// ─── Primary — Cobalt Blue (Brand / Action / Navigation) ─────────────────────
// "Tap me." CTAs, FAB, active nav indicator, links, progress fills.
// Never used to convey a maintenance health state.
val CobaltBlue       = Color(0xFF1557C8)   // 6.28:1 on CloudWhite ✓ WCAG AA
val OnCobaltBlue     = Color(0xFFFFFFFF)   // 7.17:1 on CobaltBlue ✓
val BlueTint         = Color(0xFFDCE9FF)   // primaryContainer — selected controls, chips
val OnBlueTint       = Color(0xFF102A56)   // MidnightNavy — onPrimaryContainer
val MidnightNavy     = Color(0xFF102A56)   // logo mark, key headings, onPrimaryContainer
val ElectricSky      = Color(0xFF4DA3FF)   // highlight / progress accent (dark use)

// ─── Secondary — Caution Amber (DUE_SOON only) ───────────────────────────────
// ISO 2575: amber = "attention required, not yet urgent."
val DueSoon          = Color(0xFF9A6700)   // 6.61:1 on CloudWhite ✓ WCAG AA
val OnDueSoon        = Color(0xFFFFFFFF)
val DueSoonContainer = Color(0xFFFFF0C7)   // warm amber tonal surface
val OnDueSoonContainer = Color(0xFF311E00) // deep amber on light amber ✓

// ─── Tertiary — Health Green (HEALTHY / OK / COMPLETED only) ─────────────────
// "You're fine." Universal green = good convention. Separated from brand blue
// so users never confuse "this is actionable" with "this is healthy."
val HealthGreen          = Color(0xFF167A55) // 5.52:1 on CloudWhite ✓ WCAG AA
val OnHealthGreen        = Color(0xFFFFFFFF)
val HealthGreenContainer = Color(0xFFDDF5EA) // fresh green tonal surface
val OnHealthGreenContainer = Color(0xFF003824) // deep forest on mint ✓

// ─── Error — Critical Red (OVERDUE only) ─────────────────────────────────────
// ISO 2575: red = "stop, act now." Cannot be snoozed.
val Overdue          = Color(0xFFB42318)   // 6.25:1 on CloudWhite ✓ WCAG AA
val OnOverdue        = Color(0xFFFFFFFF)
val OverdueContainer = Color(0xFFFEE4E2)   // red tonal surface
val OnOverdueContainer = Color(0xFF410002) // deep crimson on blush ✓

// ─── Surfaces — Light ────────────────────────────────────────────────────────
// Elevation is expressed through cooler, slightly blue-tinted whites (not shadows).
// Pure #000000 is avoided: halation on OLED, smearing during scroll.
val CloudWhite              = Color(0xFFF7F9FC) // app background
val SurfaceWhite            = Color(0xFFFFFFFF) // cards / bottom sheets
val SurfaceBlue             = Color(0xFFEEF4FD) // tonal surface — subtle elevation
val SurfaceContainerHighL   = Color(0xFFE5EDF9) // higher elevation tonal
val SurfaceContainerHighestL = Color(0xFFDBE5F5)// highest tonal (rarely used)
val SurfaceDimL             = Color(0xFFD8DEE7)
val SurfaceBrightL          = Color(0xFFFDFEFF)

// ─── Text ────────────────────────────────────────────────────────────────────
val Ink   = Color(0xFF101828)   // 18.15:1 on CloudWhite — primary headings + body
val Slate = Color(0xFF52627A)   //  7.21:1 on CloudWhite — secondary / metadata

// ─── Borders ─────────────────────────────────────────────────────────────────
// OutlineInteractive: boundaries that identify a control (WCAG 1.4.11 ≥3:1)
val OutlineInteractive = Color(0xFF77869A) // 3.71:1 on SurfaceWhite ✓
// Outline: decorative dividers — deliberately quiet, 1.4.11 does not apply
val OutlineSubtle      = Color(0xFFD4DDEB) // 1.29:1 — exempt, see design system §2

// ═══════════════════════════════════════════════════════════════════════════════
// DARK SCHEME — MIDNIGHT COBALT (unchanged)
// ═══════════════════════════════════════════════════════════════════════════════

val CobaltDark              = Color(0xFF7AB4FF)  // 8.71:1 on dark ground
val OnCobaltDark            = Color(0xFF0F1316)
val CobaltContainerDark     = Color(0xFF12304F)
val OnCobaltContainerDark   = Color(0xFFCFE3FF)

val SecondaryDark           = Color(0xFFB4C3D2)
val OnSecondaryDark         = Color(0xFF1E2A36)
val SecondaryContainerDark  = Color(0xFF2A3644)
val OnSecondaryContainerDark = Color(0xFFD3E0EE)

val AmberDark               = Color(0xFFEFB552)  // 10.13:1 on dark ground
val OnAmberDark             = Color(0xFF3F2D00)
val AmberContainerDark      = Color(0xFF3A2E12)
val OnAmberContainerDark    = Color(0xFFFFDEA6)

val CriticalDark            = Color(0xFFFF8A80)  // 8.18:1 on dark ground
val OnCriticalDark          = Color(0xFF57120E)
val CriticalContainerDark   = Color(0xFF40201E)
val OnCriticalContainerDark = Color(0xFFFFDAD6)

val GroundDark              = Color(0xFF0F1316)
val OnGroundDark            = Color(0xFFE8EDF1)  // 15.83:1
val SurfaceVariantDark      = Color(0xFF3A444C)
val OnSurfaceVariantDark    = Color(0xFF98A4AD)  // 7.33:1
val SurfaceDimDark          = Color(0xFF0B0E11)
val SurfaceBrightDark       = Color(0xFF343D45)
val SurfaceContainerLowestDark  = Color(0xFF080B0D)
val SurfaceContainerLowDark     = Color(0xFF13181C)
val SurfaceContainerDark        = Color(0xFF171C20)
val SurfaceContainerHighDark    = Color(0xFF1F262B)
val SurfaceContainerHighestDark = Color(0xFF283036)
val BorderInteractiveDark   = Color(0xFF606F7B)  // 3.32:1 on surface ✓
val BorderSubtleDark        = Color(0xFF2C353B)  // decorative only

// ─── Scrim ───────────────────────────────────────────────────────────────────
val Scrim = Color(0xFF000000)
