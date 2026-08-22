package com.autominder.app.data.local.dao

import androidx.room.*
import com.autominder.app.data.local.entity.MileageLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MileageLogDao {

    @Query("SELECT * FROM mileage_logs WHERE vehicleId = :vehicleId ORDER BY loggedAt DESC")
    fun getLogsForVehicle(vehicleId: Long): Flow<List<MileageLogEntity>>

    @Query("SELECT * FROM mileage_logs ORDER BY loggedAt DESC")
    suspend fun getAllLogsOnce(): List<MileageLogEntity>

    @Query("SELECT * FROM mileage_logs WHERE vehicleId = :vehicleId ORDER BY odometer DESC LIMIT 1")
    suspend fun getLatestLogForVehicle(vehicleId: Long): MileageLogEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: MileageLogEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLogs(logs: List<MileageLogEntity>): List<Long>

    @Delete
    suspend fun deleteLog(log: MileageLogEntity)
}
