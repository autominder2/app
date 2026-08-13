package com.autominder.app.domain.repository

import com.autominder.app.domain.model.Service
import com.autominder.app.domain.model.ServiceCompletion
import com.autominder.app.domain.model.ServiceCompletionResult
import kotlinx.coroutines.flow.Flow

interface IServiceRepository {
    fun getAllServices(): Flow<List<Service>>
    fun getServicesForVehicle(vehicleId: Long): Flow<List<Service>>
    fun getServiceById(id: Long): Flow<Service?>
    fun getTotalCostForVehicle(vehicleId: Long): Flow<Int>
    fun getCostSince(vehicleId: Long, sinceMillis: Long): Flow<Int>
    suspend fun insertService(service: Service): Long

    /**
     * Persists a completed service, its odometer effect and its reminder rebase as
     * ONE atomic operation. Any failure leaves the database exactly as it was.
     */
    suspend fun completeService(completion: ServiceCompletion): ServiceCompletionResult
    suspend fun updateService(service: Service)
    suspend fun deleteService(service: Service)
}
