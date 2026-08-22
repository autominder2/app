package com.autominder.app.data.repository

import androidx.room.withTransaction
import com.autominder.app.data.local.dao.VehicleDao
import com.autominder.app.data.local.database.AppDatabase
import com.autominder.app.data.mapper.toDomain
import com.autominder.app.data.mapper.toEntity
import com.autominder.app.domain.model.Vehicle
import com.autominder.app.domain.repository.IVehicleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository implementation for Vehicle data.
 * Expertise: Uses AppDatabase directly for cross-DAO transactions
 * to ensure data atomicity during vehicle onboarding.
 */
@Singleton
class VehicleRepositoryImpl @Inject constructor(
    private val database: AppDatabase,
    private val vehicleDao: VehicleDao
) : IVehicleRepository {

    override fun getAllVehicles(): Flow<List<Vehicle>> {
        return vehicleDao.getAllVehicles().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getAllVehiclesIncludingArchived(): Flow<List<Vehicle>> {
        return vehicleDao.getAllVehiclesIncludingArchived().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getVehicleById(id: Long): Flow<Vehicle?> {
        return vehicleDao.getVehicleById(id).map { it?.toDomain() }
    }

    override suspend fun insertVehicle(vehicle: Vehicle): Long {
        return vehicleDao.insertVehicle(vehicle.toEntity())
    }

    /**
     * Expertise: Atomic operation ensuring a vehicle and its maintenance
     * schedule are created together or not at all.
     */
    override suspend fun insertVehicleWithInitialState(
        vehicle: Vehicle,
        initialState: suspend (Long) -> Unit
    ): Long {
        return database.withTransaction {
            val id = vehicleDao.insertVehicle(vehicle.toEntity())
            initialState(id)
            id
        }
    }

    override suspend fun updateVehicle(vehicle: Vehicle) {
        vehicleDao.updateVehicle(vehicle.toEntity())
    }

    override suspend fun archiveVehicle(id: Long) {
        vehicleDao.archiveVehicle(id)
    }

    override suspend fun deleteVehicle(vehicle: Vehicle) {
        vehicleDao.deleteVehicle(vehicle.toEntity())
    }

    override suspend fun updateOdometer(id: Long, odometer: Int) {
        vehicleDao.updateOdometer(id, odometer)
    }
}
