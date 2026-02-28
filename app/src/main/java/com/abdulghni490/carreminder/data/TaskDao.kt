package com.abdulghni490.carreminder.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM maintenance_tasks ORDER BY dateDue ASC")
    fun getAllTasks(): Flow<List<MaintenanceTask>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: MaintenanceTask)

    @Update
    suspend fun updateTask(task: MaintenanceTask)

    @Delete
    suspend fun deleteTask(task: MaintenanceTask)
}
