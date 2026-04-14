package com.autominder.app.domain.repository

import com.autominder.app.domain.model.Service
import kotlinx.coroutines.flow.Flow

interface IServiceRepository {
    fun getAllServices(): Flow<List<Service>>
    fun getServicesForVehicle(vehicleId: Long): Flow<List<Service>>
    fun getServiceById(id: Long): Flow<Service?>
    fun getTotalCostForVehicle(vehicleId: Long): Flow<Int>
    fun getCostSince(vehicleId: Long, sinceMillis: Long): Flow<Int>
    suspend fun insertService(service: Service): Long
    suspend fun updateService(service: Service)
    suspend fun deleteService(service: Service)
}
