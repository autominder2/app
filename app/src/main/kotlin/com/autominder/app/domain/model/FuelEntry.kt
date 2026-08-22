package com.autominder.app.domain.model

import java.util.Date

/**
 * Expert domain model for a fuel record.
 * Uses Int for milliliters and Long for cents to ensure mathematical precision (no floating point).
 * Efficiency can be calculated as a helper property.
 */
data class FuelEntry(
    val id: Long = 0,
    val vehicleId: Long,
    val date: Date,
    val odometer: Int,
    val volumeMilliliters: Int, // e.g. 45.5L -> 45500ml
    val costCents: Long,        // e.g. $65.43 -> 6543L
    val notes: String = ""
) {
    /**
     * Calculates efficiency in Kilometers per Liter (km/L).
     * Typically needs the PREVIOUS odometer reading to be accurate (Full-to-Full method).
     */
    fun calculateKmL(previousOdometer: Int): Double {
        val distance = odometer - previousOdometer
        if (distance <= 0 || volumeMilliliters <= 0) return 0.0
        return (distance.toDouble() / (volumeMilliliters.toDouble() / 1000.0))
    }
}
