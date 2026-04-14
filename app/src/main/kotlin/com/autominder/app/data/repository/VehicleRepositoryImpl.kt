package com.autominder.app.data.repository

import com.autominder.app.data.local.dao.VehicleDao
import com.autominder.app.data.mapper.toDomain
import com.autominder.app.data.mapper.toEntity
import com.autominder.app.domain.model.Vehicle
import com.autominder.app.domain.repository.IVehicleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VehicleRepositoryImpl @Inject constructor(
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
