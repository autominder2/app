package com.autominder.app.data.mapper

import com.autominder.app.data.local.entity.MileageLogEntity
import com.autominder.app.domain.model.MileageLogEntry

/**
 * Maps between [MileageLogEntity] and [MileageLogEntry].
 */
fun MileageLogEntity.toDomain(): MileageLogEntry {
    return MileageLogEntry(
        id = id,
        vehicleId = vehicleId,
        odometer = odometer,
        loggedAt = loggedAt,
        notes = notes
    )
}

fun MileageLogEntry.toEntity(): MileageLogEntity {
    return MileageLogEntity(
        id = id,
        vehicleId = vehicleId,
        odometer = odometer,
        loggedAt = loggedAt,
        notes = notes
    )
}
