package com.autominder.app.domain.util

/**
 * Backward-compatible bridge delegating to [VehicleDisplayNameFormatter].
 * When a year is provided it is included in the output (matching legacy behaviour).
 */
object VehicleDisplayName {
    fun format(make: String?, model: String?, year: Int? = null): String {
        val includeYear = year != null && year > 0
        return VehicleDisplayNameFormatter.format(make = make, model = model, year = year, includeYear = includeYear)
    }
}
