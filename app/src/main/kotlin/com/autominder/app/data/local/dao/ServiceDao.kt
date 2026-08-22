package com.autominder.app.data.local.dao

import androidx.room.*
import com.autominder.app.data.local.entity.ServiceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ServiceDao {

    @Query("SELECT * FROM services ORDER BY serviceDate DESC")
    fun getAllServices(): Flow<List<ServiceEntity>>

    @Query("SELECT * FROM services ORDER BY serviceDate DESC")
    suspend fun getAllServicesOnce(): List<ServiceEntity>

    @Query("SELECT * FROM services WHERE vehicleId = :vehicleId ORDER BY serviceDate DESC")
    fun getServicesForVehicle(vehicleId: Long): Flow<List<ServiceEntity>>

    @Query("SELECT * FROM services WHERE vehicleId = :vehicleId ORDER BY serviceDate DESC")
    suspend fun getServicesForVehicleOnce(vehicleId: Long): List<ServiceEntity>

    @Query("SELECT * FROM services WHERE id = :id")
    fun getServiceById(id: Long): Flow<ServiceEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertService(service: ServiceEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertServices(services: List<ServiceEntity>): List<Long>

    @Update
    suspend fun updateService(service: ServiceEntity)

    @Delete
    suspend fun deleteService(service: ServiceEntity)

    @Query("SELECT COALESCE(SUM(costCents), 0) FROM services WHERE vehicleId = :vehicleId")
    fun getTotalCostForVehicle(vehicleId: Long): Flow<Int>

    @Query("SELECT COALESCE(SUM(costCents), 0) FROM services WHERE vehicleId = :vehicleId AND serviceDate >= :sinceMillis")
    fun getCostSince(vehicleId: Long, sinceMillis: Long): Flow<Int>
}
