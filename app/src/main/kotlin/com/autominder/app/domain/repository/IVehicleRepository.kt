package com.autominder.app.domain.repository

import com.autominder.app.domain.model.Vehicle
import kotlinx.coroutines.flow.Flow

/**
 * Domain repository for vehicle management.
 * com.autominder.app, Kotlin 2.1.21, KSP not kapt, Long PKs
 */
interface IVehicleRepository {
    fun getAllVehicles(): Flow<List<Vehicle>>
    fun getAllVehiclesIncludingArchived(): Flow<List<Vehicle>>
    fun getVehicleById(id: Long): Flow<Vehicle?>

    suspend fun insertVehicle(vehicle: Vehicle): Long

    /**
     * Expertise: Atomic operation to ensure vehicle and initial state (reminders)
     * are created together or not at all.
     */
    suspend fun insertVehicleWithInitialState(
        vehicle: Vehicle,
        initialState: suspend (Long) -> Unit
    ): Long

    suspend fun updateVehicle(vehicle: Vehicle)
    suspend fun archiveVehicle(id: Long)
    suspend fun deleteVehicle(vehicle: Vehicle)
    suspend fun updateOdometer(id: Long, odometer: Int)
}
