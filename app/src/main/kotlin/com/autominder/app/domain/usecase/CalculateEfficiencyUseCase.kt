package com.autominder.app.domain.usecase

import com.autominder.app.domain.model.FuelEntry
import javax.inject.Inject

/**
 * Expert Use Case for calculating fuel efficiency.
 * Handles the math between odometer gap and fill volume.
 */
class CalculateEfficiencyUseCase @Inject constructor() {

    enum class EfficiencyUnit {
        MPG_US,
        MPG_UK,
        L_100KM,
        KM_L
    }

    /**
     * Calculates efficiency between two sequential fuel entries.
     * Entries must be "Full Tank" for this to be accurate.
     */
    fun calculate(
        current: FuelEntry,
        previous: FuelEntry?,
        unit: EfficiencyUnit = EfficiencyUnit.KM_L
    ): Double {
        if (previous == null) return 0.0
        
        val distance = current.odometer - previous.odometer
        if (distance <= 0) return 0.0
        
        val liters = current.volumeMilliliters / 1000.0
        if (liters <= 0.0) return 0.0

        return when (unit) {
            EfficiencyUnit.KM_L -> distance / liters
            EfficiencyUnit.L_100KM -> (liters / distance) * 100.0
            EfficiencyUnit.MPG_US -> {
                val miles = distance * 0.621371
                val gallons = liters * 0.264172
                miles / gallons
            }
            EfficiencyUnit.MPG_UK -> {
                val miles = distance * 0.621371
                val gallons = liters * 0.219969
                miles / gallons
            }
        }
    }

    /**
     * Average efficiency over a list of entries.
     */
    fun calculateAverage(
        entries: List<FuelEntry>,
        unit: EfficiencyUnit = EfficiencyUnit.KM_L
    ): Double {
        if (entries.size < 2) return 0.0
        
        // Sort by odometer to ensure sequential gaps
        val sorted = entries.sortedBy { it.odometer }
        var totalDistance = 0.0
        var totalLiters = 0.0
        
        for (i in 1 until sorted.size) {
            val dist = sorted[i].odometer - sorted[i-1].odometer
            if (dist > 0) {
                totalDistance += dist
                totalLiters += (sorted[i].volumeMilliliters / 1000.0)
            }
        }
        
        if (totalDistance <= 0 || totalLiters <= 0) return 0.0
        
        return when (unit) {
            EfficiencyUnit.KM_L -> totalDistance / totalLiters
            EfficiencyUnit.L_100KM -> (totalLiters / totalDistance) * 100.0
            EfficiencyUnit.MPG_US -> {
                val miles = totalDistance * 0.621371
                val gallons = totalLiters * 0.264172
                miles / gallons
            }
            EfficiencyUnit.MPG_UK -> {
                val miles = totalDistance * 0.621371
                val gallons = totalLiters * 0.219969
                miles / gallons
            }
        }
    }
}
