package com.autominder.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary            = PrimaryGreen,
    onPrimary          = OnPrimaryGreen,
    primaryContainer   = DarkPrimaryContainer,
    onPrimaryContainer = OnDarkPrimaryContainer,
    secondary          = SecondaryAmber,
    onSecondary        = OnSecondaryAmber,
    secondaryContainer = DarkSecondaryContainer,
    onSecondaryContainer = OnDarkSecondaryContainer,
    tertiary           = TertiaryBlue,
    onTertiary         = OnTertiaryBlue,
    tertiaryContainer  = TertiaryContainerDark,
    onTertiaryContainer = OnTertiaryContainerDark,
    error              = ErrorRed,
    onError            = OnErrorRed,
    errorContainer     = DarkErrorContainer,
    onErrorContainer   = OnDarkErrorContainer,
    background         = BackgroundDark,
    surface            = SurfaceDark,
    onBackground       = OnBackgroundLight,
    onSurface          = OnSurfaceLight,
    surfaceVariant     = SurfaceVariantDark,
    outline            = OutlineGrey,
    scrim              = Scrim
)

private val LightColorScheme = lightColorScheme(
    primary            = PrimaryGreen,
    onPrimary          = OnPrimaryGreen,
    primaryContainer   = LightPrimaryContainer,
    onPrimaryContainer = OnLightPrimaryContainer,
    secondary          = SecondaryAmber,
    onSecondary        = OnSecondaryAmber,
    secondaryContainer = LightSecondaryContainer,
    onSecondaryContainer = OnLightSecondaryContainer,
    tertiary           = TertiaryBlue,
    onTertiary         = OnTertiaryBlue,
    tertiaryContainer  = TertiaryContainerLight,
    onTertiaryContainer = OnTertiaryContainerLight,
    error              = ErrorRed,
    onError            = OnErrorRed,
    errorContainer     = LightErrorContainer,
    onErrorContainer   = OnLightErrorContainer,
    background         = LightBackground,
    surface            = LightSurface,
    onBackground       = OnLightBackground,
    onSurface          = OnLightSurface,
    surfaceVariant     = LightSurfaceVariant,
    outline            = LightOutline,
    scrim              = Scrim
)

@Composable
fun AutoMinderTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Dynamic color intentionally disabled — our custom green/amber/cyan palette
    // is the brand identity. Wallpaper-derived colors would override it completely.
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography,
        shapes      = Shapes,
        content     = content
    )
}
