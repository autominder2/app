package com.autominder.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

/**
 * Night Garage — the AutoMinder identity.
 *
 * Wallpaper-driven dynamic colour is deliberately absent. A maintenance app
 * signals safety state through colour, so the palette cannot be handed to the
 * user's wallpaper: a pink system theme would leave amber and red competing
 * with a pink "healthy". Dynamic colour may return later as an explicit
 * appearance setting, never as a default.
 *
 * `outline` carries border/interactive (the boundary that makes a control
 * identifiable, ≥3:1) and `outlineVariant` carries border/subtle (decorative
 * dividers, deliberately quiet). OutlinedTextField and selectable surfaces read
 * `outline`, which is what gives fields a visible edit affordance.
 */
private val DarkColorScheme = darkColorScheme(
    primary              = CobaltDark,
    onPrimary            = OnCobaltDark,
    primaryContainer     = CobaltContainerDark,
    onPrimaryContainer   = OnCobaltContainerDark,
    secondary            = SecondaryDark,
    onSecondary          = OnSecondaryDark,
    secondaryContainer   = SecondaryContainerDark,
    onSecondaryContainer = OnSecondaryContainerDark,
    tertiary             = AmberDark,
    onTertiary           = OnAmberDark,
    tertiaryContainer    = AmberContainerDark,
    onTertiaryContainer  = OnAmberContainerDark,
    error                = CriticalDark,
    onError              = OnCriticalDark,
    errorContainer       = CriticalContainerDark,
    onErrorContainer     = OnCriticalContainerDark,
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
    primary              = CobaltLight,
    onPrimary            = OnCobaltLight,
    primaryContainer     = CobaltContainerLight,
    onPrimaryContainer   = OnCobaltContainerLight,
    secondary            = SecondaryLight,
    onSecondary          = OnSecondaryLight,
    secondaryContainer   = SecondaryContainerLight,
    onSecondaryContainer = OnSecondaryContainerLight,
    tertiary             = AmberLight,
    onTertiary           = OnAmberLight,
    tertiaryContainer    = AmberContainerLight,
    onTertiaryContainer  = OnAmberContainerLight,
    error                = CriticalLight,
    onError              = OnCriticalLight,
    errorContainer       = CriticalContainerLight,
    onErrorContainer     = OnCriticalContainerLight,
    background           = GroundLight,
    onBackground         = OnGroundLight,
    surface              = GroundLight,
    onSurface            = OnGroundLight,
    surfaceVariant       = SurfaceVariantLight,
    onSurfaceVariant     = OnSurfaceVariantLight,
    surfaceDim           = SurfaceDimLight,
    surfaceBright        = SurfaceBrightLight,
    surfaceContainerLowest  = SurfaceContainerLowestLight,
    surfaceContainerLow     = SurfaceContainerLowLight,
    surfaceContainer        = SurfaceContainerLight,
    surfaceContainerHigh    = SurfaceContainerHighLight,
    surfaceContainerHighest = SurfaceContainerHighestLight,
    outline              = BorderInteractiveLight,
    outlineVariant       = BorderSubtleLight,
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
