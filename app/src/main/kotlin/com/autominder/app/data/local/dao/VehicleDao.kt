package com.autominder.app.data.local.dao

import androidx.room.*
import com.autominder.app.data.local.entity.VehicleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VehicleDao {

    @Query("SELECT * FROM vehicles WHERE isArchived = 0 ORDER BY updatedAt DESC")
    fun getAllVehicles(): Flow<List<VehicleEntity>>

    @Query("SELECT * FROM vehicles ORDER BY updatedAt DESC")
    fun getAllVehiclesIncludingArchived(): Flow<List<VehicleEntity>>

    @Query("SELECT * FROM vehicles WHERE isArchived = 0 ORDER BY updatedAt DESC LIMIT 1")
    suspend fun getPrimaryVehicleOnce(): VehicleEntity?

    @Query("SELECT * FROM vehicles WHERE isArchived = 0 ORDER BY updatedAt DESC")
    suspend fun getAllActiveVehiclesOnce(): List<VehicleEntity>

    @Query("SELECT * FROM vehicles ORDER BY updatedAt DESC")
    suspend fun getAllVehiclesOnce(): List<VehicleEntity>

    @Query("SELECT * FROM vehicles WHERE id = :id")
    fun getVehicleById(id: Long): Flow<VehicleEntity?>

    /**
     * One-shot existence/state read for transactional writes. A Flow cannot be
     * collected inside a Room transaction, so multi-table operations use this.
     */
    @Query("SELECT * FROM vehicles WHERE id = :id")
    suspend fun getVehicleByIdOnce(id: Long): VehicleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVehicle(vehicle: VehicleEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVehicles(vehicles: List<VehicleEntity>): List<Long>

    @Update
    suspend fun updateVehicle(vehicle: VehicleEntity)

    @Query("UPDATE vehicles SET isArchived = 1, updatedAt = :nowMillis WHERE id = :id")
    suspend fun archiveVehicle(id: Long, nowMillis: Long = System.currentTimeMillis())

    @Delete
    suspend fun deleteVehicle(vehicle: VehicleEntity)

    @Query("UPDATE vehicles SET currentOdometer = :odometer, updatedAt = :nowMillis WHERE id = :id")
    suspend fun updateOdometer(id: Long, odometer: Int, nowMillis: Long = System.currentTimeMillis())

    /**
     * Atomic conditional update — only writes if the new odometer reading is strictly higher
     * than the stored one. Prevents fuel/service back-entries from rolling the vehicle odometer
     * backwards. SQL-level guard, no read-then-write race.
     */
    @Query("UPDATE vehicles SET currentOdometer = :odometer, updatedAt = :nowMillis WHERE id = :id AND currentOdometer < :odometer")
    suspend fun updateOdometerIfHigher(id: Long, odometer: Int, nowMillis: Long = System.currentTimeMillis())

    @Query("SELECT COUNT(*) FROM vehicles WHERE isArchived = 0")
    suspend fun getActiveVehicleCount(): Int

    /**
     * Erases every vehicle, and with it every reminder, service, mileage log and
     * fuel entry: all four child tables declare
     * `ForeignKey(onDelete = CASCADE)` on `vehicleId`, so this single statement
     * empties the database. Archived vehicles are included - "delete everything"
     * has to mean everything, or the promise is false.
     *
     * Only [com.autominder.app.domain.usecase.DeleteAllDataUseCase] should call
     * this. There is no undo.
     */
    @Query("DELETE FROM vehicles")
    suspend fun deleteAllVehicles()
}
