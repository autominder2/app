package com.autominder.app.data.repository

import androidx.room.withTransaction
import com.autominder.app.data.local.dao.FuelDao
import com.autominder.app.data.local.dao.VehicleDao
import com.autominder.app.data.local.database.AppDatabase
import com.autominder.app.data.mapper.toDomain
import com.autominder.app.data.mapper.toEntity
import com.autominder.app.domain.model.FuelEntry
import com.autominder.app.domain.repository.IFuelRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class FuelRepositoryImpl @Inject constructor(
    private val db: AppDatabase,
    private val fuelDao: FuelDao,
    private val vehicleDao: VehicleDao
) : IFuelRepository {

    override fun getFuelEntriesForVehicle(vehicleId: Long): Flow<List<FuelEntry>> =
        fuelDao.getFuelEntriesForVehicle(vehicleId).map { entities ->
            entities.map { it.toDomain() }
        }

    override fun getLatestFuelEntryForVehicle(vehicleId: Long): Flow<FuelEntry?> =
        fuelDao.getLatestFuelEntryForVehicle(vehicleId).map { it?.toDomain() }

    /**
     * Inserts a fuel entry and conditionally advances the vehicle's odometer reading
     * atomically. Wrapped in a Room transaction so partial failure cannot leave the
     * fuel log and vehicle state out of sync (CLAUDE.md multi-table write law).
     *
     * Vehicle odometer only advances if the fuel-entry odometer is strictly higher than
     * the stored value — back-dated fuel entries cannot roll the odometer backwards.
     */
    override suspend fun insertFuelEntry(fuelEntry: FuelEntry): Long = db.withTransaction {
        val id = fuelDao.insertFuelEntry(fuelEntry.toEntity())
        vehicleDao.updateOdometerIfHigher(fuelEntry.vehicleId, fuelEntry.odometer)
        id
    }

    override suspend fun updateFuelEntry(fuelEntry: FuelEntry) {
        fuelDao.updateFuelEntry(fuelEntry.toEntity())
    }

    override suspend fun deleteFuelEntry(fuelEntry: FuelEntry) {
        fuelDao.deleteFuelEntry(fuelEntry.toEntity())
    }
}
