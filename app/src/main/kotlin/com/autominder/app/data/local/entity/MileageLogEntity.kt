package com.autominder.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity for manual odometer readings.
 * PRD schema — Section 3.4.
 *
 * Each manual entry creates a row here; the latest row drives
 * VehicleEntity.currentOdometer updates.
 */
@Entity(
    tableName = "mileage_logs",
    foreignKeys = [
        ForeignKey(
            entity = VehicleEntity::class,
            parentColumns = ["id"],
            childColumns  = ["vehicleId"],
            onDelete      = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["vehicleId"])]
)
data class MileageLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val vehicleId: Long,
    val odometer: Int,
    val loggedAt: Long = System.currentTimeMillis(),  // epoch millis
    val notes: String? = null
)
