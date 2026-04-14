package com.autominder.app.ui.theme

import androidx.compose.runtime.compositionLocalOf

/**
 * CompositionLocal providing the user's distance unit preference ("km" or "mi").
 * Provided at the app level from UserPreferences, consumed by any screen that
 * displays or accepts odometer values.
 */
val LocalDistanceUnit = compositionLocalOf { "km" }
