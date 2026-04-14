package com.autominder.app.domain.repository

import com.autominder.app.domain.model.Vehicle
import kotlinx.coroutines.flow.Flow

interface IVehicleRepository {
    fun getAllVehicles(): Flow<List<Vehicle>>
    fun getAllVehiclesIncludingArchived(): Flow<List<Vehicle>>
    fun getVehicleById(id: Long): Flow<Vehicle?>
    suspend fun insertVehicle(vehicle: Vehicle): Long
    suspend fun updateVehicle(vehicle: Vehicle)
    suspend fun archiveVehicle(id: Long)
    suspend fun deleteVehicle(vehicle: Vehicle)
    suspend fun updateOdometer(id: Long, odometer: Int)
}
