package com.autominder.app.data.repository

import com.autominder.app.data.local.dao.FuelDao
import com.autominder.app.data.local.dao.VehicleDao
import com.autominder.app.data.local.entity.FuelEntryEntity
import com.autominder.app.domain.model.FuelEntry
import com.autominder.app.domain.repository.IFuelRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Date
import javax.inject.Inject

class FuelRepositoryImpl @Inject constructor(
    private val fuelDao: FuelDao,
    private val vehicleDao: VehicleDao
) : IFuelRepository {

    override fun getFuelEntriesForVehicle(vehicleId: Long): Flow<List<FuelEntry>> =
        fuelDao.getFuelEntriesForVehicle(vehicleId).map { entities ->
            entities.map { it.toDomain() }
        }

    override fun getLatestFuelEntryForVehicle(vehicleId: Long): Flow<FuelEntry?> =
        fuelDao.getLatestFuelEntryForVehicle(vehicleId).map { it?.toDomain() }

    override suspend fun insertFuelEntry(fuelEntry: FuelEntry): Long {
        val id = fuelDao.insertFuelEntry(fuelEntry.toEntity())
        // Expert rule: Update vehicle odometer matches fuel odometer if higher
        vehicleDao.updateOdometer(fuelEntry.vehicleId, fuelEntry.odometer)
        return id
    }

    override suspend fun updateFuelEntry(fuelEntry: FuelEntry) {
        fuelDao.updateFuelEntry(fuelEntry.toEntity())
    }

    override suspend fun deleteFuelEntry(fuelEntry: FuelEntry) {
        fuelDao.deleteFuelEntry(fuelEntry.toEntity())
    }

    private fun FuelEntryEntity.toDomain() = FuelEntry(
        id = id,
        vehicleId = vehicleId,
        date = Date(date),
        odometer = odometer,
        volumeMilliliters = volumeMilliliters,
        costCents = costCents,
        notes = notes
    )

    private fun FuelEntry.toEntity() = FuelEntryEntity(
        id = id,
        vehicleId = vehicleId,
        date = date.time,
        odometer = odometer,
        volumeMilliliters = volumeMilliliters,
        costCents = costCents,
        notes = notes
    )
}
