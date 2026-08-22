package com.autominder.app.domain.util

import kotlin.math.roundToInt

/**
 * Converts between km (internal storage unit) and display unit (km or mi).
 * All database values are stored in km — conversion only at UI boundaries.
 */
object DistanceUtil {
    private const val MILES_PER_KM = 0.621371
    private const val KM_PER_MILE = 1.60934

    fun kmToDisplay(km: Int, unit: String): Int = when (unit) {
        "mi" -> (km * MILES_PER_KM).roundToInt()
        else -> km
    }

    fun displayToKm(value: Int, unit: String): Int = when (unit) {
        "mi" -> (value * KM_PER_MILE).roundToInt()
        else -> value
    }

    fun unitLabel(unit: String): String = when (unit) {
        "mi" -> "mi"
        else -> "km"
    }
}
