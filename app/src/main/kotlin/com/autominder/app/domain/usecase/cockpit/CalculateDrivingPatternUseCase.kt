package com.autominder.app.domain.usecase.cockpit

import androidx.compose.runtime.Immutable
import com.autominder.app.domain.model.FuelEntry
import com.autominder.app.domain.model.MileageLogEntry
import com.autominder.app.domain.usecase.OdometerPoint
import com.autominder.app.domain.usecase.PredictDueUseCase
import javax.inject.Inject
import javax.inject.Singleton

@Immutable
data class DrivingPattern(
    val dailyKmRate: Double? = null,
    val annualProjectionKm: Int? = null,
    val paceLabel: String = "Normal usage",
    val observationPoints: Int = 0
)

/**
 * Calculates real-world driving velocity and yearly projections
 * from historical mileage logs and fuel fill-up odometer observations.
 */
@Singleton
class CalculateDrivingPatternUseCase @Inject constructor(
    private val predictDue: PredictDueUseCase
) {

    fun execute(
        mileageLogs: List<MileageLogEntry>,
        fuelEntries: List<FuelEntry>
    ): DrivingPattern {
        val odometerPoints =
            mileageLogs.map { OdometerPoint(it.odometer, it.loggedAt) } +
            fuelEntries.map { OdometerPoint(it.odometer, it.date.time) }

        val dailyRate = predictDue.dailyKmRate(odometerPoints)

        if (dailyRate == null || dailyRate <= 0) {
            return DrivingPattern(
                dailyKmRate = null,
                annualProjectionKm = null,
                paceLabel = "Awaiting more logs",
                observationPoints = odometerPoints.size
            )
        }

        val annualProjection = (dailyRate * 365.25).toInt()
        val paceLabel = when {
            dailyRate < 20 -> "Low mileage (Weekend/Local)"
            dailyRate <= 60 -> "Standard commute (~${annualProjection.div(1000)}k km/yr)"
            else -> "High mileage road warrior"
        }

        return DrivingPattern(
            dailyKmRate = dailyRate,
            annualProjectionKm = annualProjection,
            paceLabel = paceLabel,
            observationPoints = odometerPoints.size
        )
    }
}
