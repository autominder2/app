package com.abdulghni490.carreminder.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "maintenance_tasks")
data class MaintenanceTask(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val carName: String,
    val taskName: String,
    val mileageDue: Int,
    val dateDue: Long,
    val isCompleted: Boolean = false
)
