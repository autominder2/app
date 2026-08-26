package com.autominder.app.domain.usecase.cockpit

import com.autominder.app.domain.intelligence.VehicleConfidenceEngine
import com.autominder.app.domain.model.MileageLogEntry
import com.autominder.app.domain.model.Reminder
import com.autominder.app.domain.model.Service
import com.autominder.app.domain.model.ServiceStatus
import com.autominder.app.domain.model.Vehicle
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Re-exported so callers in `ui/screens/vehicle` can import the whole cockpit
 * surface from one package. The types themselves live in `domain.intelligence`
 * alongside the engine that produces them.
 */
typealias ConfidenceState = com.autominder.app.domain.intelligence.ConfidenceState
typealias ConfidenceSignal = com.autominder.app.domain.intelligence.ConfidenceSignal
typealias ConfidenceFactor = com.autominder.app.domain.intelligence.ConfidenceFactor
typealias VehicleConfidence = com.autominder.app.domain.intelligence.VehicleConfidence

@Singleton
class CalculateConfidenceUseCase @Inject constructor(
    private val engine: VehicleConfidenceEngine
) {

    fun execute(
        vehicle: Vehicle,
        reminders: List<Reminder>,
        statuses: Map<Long, ServiceStatus>,
        mileageLogs: List<MileageLogEntry>,
        services: List<Service>,
        nowMillis: Long = System.currentTimeMillis()
    ): VehicleConfidence = engine.evaluate(
        vehicle = vehicle,
        reminders = reminders,
        statuses = statuses,
        mileageLogs = mileageLogs,
        services = services,
        nowMillis = nowMillis
    )
}
