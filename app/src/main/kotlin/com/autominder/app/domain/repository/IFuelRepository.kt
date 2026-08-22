package com.autominder.app.domain.repository

import com.autominder.app.domain.model.FuelEntry
import kotlinx.coroutines.flow.Flow

/**
 * Expert repository interface for fuel management.
 * Follows [AGENTS.md] for suspend + Flow logic.
 */
interface IFuelRepository {
    fun getFuelEntriesForVehicle(vehicleId: Long): Flow<List<FuelEntry>>
    fun getLatestFuelEntryForVehicle(vehicleId: Long): Flow<FuelEntry?>
    suspend fun insertFuelEntry(fuelEntry: FuelEntry): Long
    suspend fun updateFuelEntry(fuelEntry: FuelEntry)
    suspend fun deleteFuelEntry(fuelEntry: FuelEntry)
}
