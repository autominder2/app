package com.autominder.app.data.repository

import com.autominder.app.data.local.dao.MileageLogDao
import com.autominder.app.data.mapper.toDomain
import com.autominder.app.data.mapper.toEntity
import com.autominder.app.domain.model.MileageLogEntry
import com.autominder.app.domain.repository.IMileageLogRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MileageLogRepositoryImpl @Inject constructor(
    private val mileageLogDao: MileageLogDao
) : IMileageLogRepository {

    override fun getLogsForVehicle(vehicleId: Long): Flow<List<MileageLogEntry>> {
        return mileageLogDao.getLogsForVehicle(vehicleId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getLatestLogForVehicle(vehicleId: Long): MileageLogEntry? {
        return mileageLogDao.getLatestLogForVehicle(vehicleId)?.toDomain()
    }

    override suspend fun insertLog(log: MileageLogEntry): Long {
        return mileageLogDao.insertLog(log.toEntity())
    }

    override suspend fun deleteLog(log: MileageLogEntry) {
        mileageLogDao.deleteLog(log.toEntity())
    }
}
