package com.autominder.app.data.repository

import com.autominder.app.data.local.dao.ServiceDao
import com.autominder.app.data.mapper.toDomain
import com.autominder.app.data.mapper.toEntity
import com.autominder.app.domain.model.Service
import com.autominder.app.domain.repository.IServiceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ServiceRepositoryImpl @Inject constructor(
    private val serviceDao: ServiceDao
) : IServiceRepository {

    override fun getAllServices(): Flow<List<Service>> {
        return serviceDao.getAllServices().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getServicesForVehicle(vehicleId: Long): Flow<List<Service>> {
        return serviceDao.getServicesForVehicle(vehicleId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getServiceById(id: Long): Flow<Service?> {
        return serviceDao.getServiceById(id).map { it?.toDomain() }
    }

    override suspend fun insertService(service: Service): Long {
        return serviceDao.insertService(service.toEntity())
    }

    override suspend fun updateService(service: Service) {
        serviceDao.updateService(service.toEntity())
    }

    override suspend fun deleteService(service: Service) {
        serviceDao.deleteService(service.toEntity())
    }

    override fun getTotalCostForVehicle(vehicleId: Long): Flow<Int> {
        return serviceDao.getTotalCostForVehicle(vehicleId)
    }

    override fun getCostSince(vehicleId: Long, sinceMillis: Long): Flow<Int> {
        return serviceDao.getCostSince(vehicleId, sinceMillis)
    }
}
