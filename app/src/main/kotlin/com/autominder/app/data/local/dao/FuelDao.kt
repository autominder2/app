package com.autominder.app.data.local.dao

import androidx.room.*
import com.autominder.app.data.local.entity.FuelEntryEntity
import kotlinx.coroutines.flow.Flow

/**
 * Expert DAO for fuel management.
 * Section 3.5 — query by vehicle odometer descending.
 */
@Dao
interface FuelDao {
    @Query("SELECT * FROM fuel_entries WHERE vehicleId = :vehicleId ORDER BY odometer DESC")
    fun getFuelEntriesForVehicle(vehicleId: Long): Flow<List<FuelEntryEntity>>

    @Query("SELECT * FROM fuel_entries WHERE vehicleId = :vehicleId ORDER BY odometer DESC LIMIT 1")
    fun getLatestFuelEntryForVehicle(vehicleId: Long): Flow<FuelEntryEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFuelEntry(fuelEntry: FuelEntryEntity): Long

    @Update
    suspend fun updateFuelEntry(fuelEntry: FuelEntryEntity)

    @Delete
    suspend fun deleteFuelEntry(fuelEntry: FuelEntryEntity)
}
