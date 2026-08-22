package com.autominder.app.ui.theme

import androidx.compose.ui.graphics.Color

// ─── AutoMinder — NIGHT GARAGE / MIDNIGHT COBALT ─────────────────────────────
// Full contract: docs/DESIGN_SYSTEM_2026.md
//
// Dark is the primary theme — the app is used in driveways and garages, often
// after dark. Light is a first-class requirement, not an afterthought.
//
// The accent doubles as the healthy state, so amber and red are the only hues
// that ever interrupt a screen. Cobalt sits 176° from the caution band — the
// maximum possible separation — which is what lets one palette carry both a
// brand and a safety-status system.
//
// Every ratio below is computed to WCAG 2.1 relative luminance.

// ─── Primary — Midnight Cobalt ──────────────────────────────────────────────
val CobaltDark = Color(0xFF7AB4FF)             // 8.71:1 on dark ground
val OnCobaltDark = Color(0xFF0F1316)           // near-black label on a cobalt fill
val CobaltContainerDark = Color(0xFF12304F)
val OnCobaltContainerDark = Color(0xFFCFE3FF)

val CobaltLight = Color(0xFF0B4FC4)            // 6.80:1 on light ground
val OnCobaltLight = Color(0xFFFFFFFF)          // 7.17:1
val CobaltContainerLight = Color(0xFFD8E4FF)
val OnCobaltContainerLight = Color(0xFF001B3F)

// ─── Secondary — cool graphite, never a second accent ───────────────────────
val SecondaryDark = Color(0xFFB4C3D2)
val OnSecondaryDark = Color(0xFF1E2A36)
val SecondaryContainerDark = Color(0xFF2A3644)
val OnSecondaryContainerDark = Color(0xFFD3E0EE)

val SecondaryLight = Color(0xFF4E5F72)
val OnSecondaryLight = Color(0xFFFFFFFF)
val SecondaryContainerLight = Color(0xFFDDE6F2)
val OnSecondaryContainerLight = Color(0xFF0B1B29)

// ─── Tertiary — caution amber, DUE_SOON only (ISO 2575) ─────────────────────
val AmberDark = Color(0xFFEFB552)              // 10.13:1 on dark ground
val OnAmberDark = Color(0xFF3F2D00)
val AmberContainerDark = Color(0xFF3A2E12)
val OnAmberContainerDark = Color(0xFFFFDEA6)

val AmberLight = Color(0xFF7A5300)             // 6.50:1 on light ground
val OnAmberLight = Color(0xFFFFFFFF)
val AmberContainerLight = Color(0xFFFCEBC8)
val OnAmberContainerLight = Color(0xFF281900)

// ─── Error — critical red, OVERDUE only (ISO 2575) ──────────────────────────
val CriticalDark = Color(0xFFFF8A80)           // 8.18:1 on dark ground
val OnCriticalDark = Color(0xFF57120E)
val CriticalContainerDark = Color(0xFF40201E)
val OnCriticalContainerDark = Color(0xFFFFDAD6)

val CriticalLight = Color(0xFFB3261E)          // 6.20:1 on light ground
val OnCriticalLight = Color(0xFFFFFFFF)
val CriticalContainerLight = Color(0xFFFBDAD7)
val OnCriticalContainerLight = Color(0xFF410002)

// ─── Surfaces (Dark) — cool neutral, lifted off pure black ──────────────────
// #000000 is avoided deliberately: it maximises halation for readers with
// astigmatism, smears on OLED during scroll, and leaves no headroom to build
// elevation. Depth here is a lighter surface, never a shadow.
val GroundDark = Color(0xFF0F1316)
val OnGroundDark = Color(0xFFE8EDF1)           // 15.83:1
val SurfaceVariantDark = Color(0xFF3A444C)
val OnSurfaceVariantDark = Color(0xFF98A4AD)   // 7.33:1
val SurfaceDimDark = Color(0xFF0B0E11)
val SurfaceBrightDark = Color(0xFF343D45)
val SurfaceContainerLowestDark = Color(0xFF080B0D)
val SurfaceContainerLowDark = Color(0xFF13181C)
val SurfaceContainerDark = Color(0xFF171C20)
val SurfaceContainerHighDark = Color(0xFF1F262B)
val SurfaceContainerHighestDark = Color(0xFF283036)

// border/interactive — the boundary that makes a control identifiable.
// 3.32:1 on surface, clearing WCAG 1.4.11.
val BorderInteractiveDark = Color(0xFF606F7B)
// border/subtle — decorative dividers only. Deliberately quiet; 1.4.11 does not
// apply to boundaries that carry no required information.
val BorderSubtleDark = Color(0xFF2C353B)

// ─── Surfaces (Light) ───────────────────────────────────────────────────────
val GroundLight = Color(0xFFF7F9FC)
val OnGroundLight = Color(0xFF0E1420)          // 17.47:1
val SurfaceVariantLight = Color(0xFFDFE6F0)
val OnSurfaceVariantLight = Color(0xFF4A5568)  // 7.14:1
val SurfaceDimLight = Color(0xFFD8DEE7)
val SurfaceBrightLight = Color(0xFFFDFEFF)
val SurfaceContainerLowestLight = Color(0xFFFFFFFF)
val SurfaceContainerLowLight = Color(0xFFF2F5FA)
val SurfaceContainerLight = Color(0xFFECF1F8)
val SurfaceContainerHighLight = Color(0xFFE5EBF4)
val SurfaceContainerHighestLight = Color(0xFFDFE6F0)

val BorderInteractiveLight = Color(0xFF77869A) // 3.71:1 on surface
val BorderSubtleLight = Color(0xFFD5DEE8)

// ─── Scrim ──────────────────────────────────────────────────────────────────
val Scrim = Color(0xFF000000)
