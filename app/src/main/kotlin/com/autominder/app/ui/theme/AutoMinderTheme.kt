package com.autominder.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

/**
 * AutoMinder Theme — Midnight Intelligence Light + Midnight Cobalt Dark
 *
 * M3 slot assignment (intentional overrides of M3 defaults — never "fix" these):
 *   primary   = CobaltBlue     → brand / action / navigation
 *   secondary = DueSoon amber  → caution / due-soon state
 *   tertiary  = HealthGreen    → healthy / ok / completed state
 *   error     = Overdue red    → critical / overdue state
 *
 * Wallpaper-driven dynamic colour is deliberately absent. A maintenance app
 * signals safety state through colour, so the palette cannot be handed to the
 * user's wallpaper: a pink system theme would corrupt the amber/red safety
 * signal. Dynamic colour may return later as an explicit appearance setting,
 * never as a default.
 *
 * `outline` = OutlineInteractive (≥3:1, WCAG 1.4.11 — control boundaries)
 * `outlineVariant` = OutlineSubtle (decorative dividers, deliberately quiet)
 */
private val DarkColorScheme = darkColorScheme(
    // Brand / action — Midnight Cobalt
    primary              = CobaltDark,
    onPrimary            = OnCobaltDark,
    primaryContainer     = CobaltContainerDark,
    onPrimaryContainer   = OnCobaltContainerDark,
    // Neutral secondary
    secondary            = SecondaryDark,
    onSecondary          = OnSecondaryDark,
    secondaryContainer   = SecondaryContainerDark,
    onSecondaryContainer = OnSecondaryContainerDark,
    // Caution amber — DUE_SOON
    tertiary             = AmberDark,
    onTertiary           = OnAmberDark,
    tertiaryContainer    = AmberContainerDark,
    onTertiaryContainer  = OnAmberContainerDark,
    // Critical red — OVERDUE
    error                = CriticalDark,
    onError              = OnCriticalDark,
    errorContainer       = CriticalContainerDark,
    onErrorContainer     = OnCriticalContainerDark,
    // Surfaces
    background           = GroundDark,
    onBackground         = OnGroundDark,
    surface              = GroundDark,
    onSurface            = OnGroundDark,
    surfaceVariant       = SurfaceVariantDark,
    onSurfaceVariant     = OnSurfaceVariantDark,
    surfaceDim           = SurfaceDimDark,
    surfaceBright        = SurfaceBrightDark,
    surfaceContainerLowest  = SurfaceContainerLowestDark,
    surfaceContainerLow     = SurfaceContainerLowDark,
    surfaceContainer        = SurfaceContainerDark,
    surfaceContainerHigh    = SurfaceContainerHighDark,
    surfaceContainerHighest = SurfaceContainerHighestDark,
    outline              = BorderInteractiveDark,
    outlineVariant       = BorderSubtleDark,
    scrim                = Scrim
)

private val LightColorScheme = lightColorScheme(
    // Brand / action — Cobalt Blue ("tap me", never "you're healthy")
    primary              = CobaltBlue,
    onPrimary            = OnCobaltBlue,
    primaryContainer     = BlueTint,
    onPrimaryContainer   = OnBlueTint,
    // Caution amber — DUE_SOON ("attention required")
    secondary            = DueSoon,
    onSecondary          = OnDueSoon,
    secondaryContainer   = DueSoonContainer,
    onSecondaryContainer = OnDueSoonContainer,
    // Health green — HEALTHY / OK / COMPLETED ("you're fine")
    tertiary             = HealthGreen,
    onTertiary           = OnHealthGreen,
    tertiaryContainer    = HealthGreenContainer,
    onTertiaryContainer  = OnHealthGreenContainer,
    // Critical red — OVERDUE ("act now")
    error                = Overdue,
    onError              = OnOverdue,
    errorContainer       = OverdueContainer,
    onErrorContainer     = OnOverdueContainer,
    // Surfaces
    background           = CloudWhite,
    onBackground         = Ink,
    surface              = CloudWhite,
    onSurface            = Ink,
    surfaceVariant       = SurfaceBlue,
    onSurfaceVariant     = Slate,
    surfaceDim           = SurfaceDimL,
    surfaceBright        = SurfaceBrightL,
    surfaceContainerLowest  = SurfaceWhite,
    surfaceContainerLow     = CloudWhite,
    surfaceContainer        = SurfaceBlue,
    surfaceContainerHigh    = SurfaceContainerHighL,
    surfaceContainerHighest = SurfaceContainerHighestL,
    outline              = OutlineInteractive,
    outlineVariant       = OutlineSubtle,
    scrim                = Scrim
)

@Composable
fun AutoMinderTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography,
        shapes      = Shapes,
        content     = content
    )
}
