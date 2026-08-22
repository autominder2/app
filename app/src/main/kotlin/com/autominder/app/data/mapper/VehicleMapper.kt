package com.autominder.app.data.mapper

import com.autominder.app.data.local.entity.VehicleEntity
import com.autominder.app.domain.model.Vehicle

/**
 * Maps between [VehicleEntity] and [Vehicle].
 */
fun VehicleEntity.toDomain(): Vehicle {
    return Vehicle(
        id = id,
        make = make,
        model = model,
        year = year,
        plateNumber = plateNumber,
        vin = vin,
        currentOdometer = currentOdometer,
        photoUri = photoUri,
        isArchived = isArchived,
        notes = notes,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun Vehicle.toEntity(): VehicleEntity {
    return VehicleEntity(
        id = id,
        make = make,
        model = model,
        year = year,
        plateNumber = plateNumber,
        vin = vin,
        currentOdometer = currentOdometer,
        photoUri = photoUri,
        isArchived = isArchived,
        notes = notes,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
