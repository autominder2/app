package com.autominder.app.domain.repository

import com.autominder.app.domain.model.MileageLogEntry
import kotlinx.coroutines.flow.Flow

interface IMileageLogRepository {
    fun getLogsForVehicle(vehicleId: Long): Flow<List<MileageLogEntry>>
    suspend fun getLatestLogForVehicle(vehicleId: Long): MileageLogEntry?
    suspend fun insertLog(log: MileageLogEntry): Long
    suspend fun deleteLog(log: MileageLogEntry)
}
