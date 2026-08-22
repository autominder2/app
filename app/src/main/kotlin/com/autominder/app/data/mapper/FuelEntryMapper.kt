package com.autominder.app.data.mapper

import com.autominder.app.data.local.entity.FuelEntryEntity
import com.autominder.app.domain.model.FuelEntry
import java.util.Date

/**
 * Maps between [FuelEntryEntity] and [FuelEntry].
 * Mirrors the pattern in [VehicleMapper] / [ReminderMapper].
 */
fun FuelEntryEntity.toDomain(): FuelEntry {
    return FuelEntry(
        id = id,
        vehicleId = vehicleId,
        date = Date(date),
        odometer = odometer,
        volumeMilliliters = volumeMilliliters,
        costCents = costCents,
        notes = notes
    )
}

fun FuelEntry.toEntity(): FuelEntryEntity {
    return FuelEntryEntity(
        id = id,
        vehicleId = vehicleId,
        date = date.time,
        odometer = odometer,
        volumeMilliliters = volumeMilliliters,
        costCents = costCents,
        notes = notes,
        createdAt = System.currentTimeMillis()
    )
}
