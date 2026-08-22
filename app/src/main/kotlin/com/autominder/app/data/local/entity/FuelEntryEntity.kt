package com.autominder.app.data.local.entity

import androidx.room.*

/**
 * Room entity for fuel logs.
 * Section 3.5 in future PRD expands.
 *
 * Storing volume in milliliters (Int) and cost in cents (Long) to avoid floating point issues.
 * FK to VehicleEntity.
 */
@Entity(
    tableName = "fuel_entries",
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
data class FuelEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    @ColumnInfo(name = "vehicleId")
    val vehicleId: Long,
    val date: Long,                  // epoch millis
    val odometer: Int,               // odometer at fill-up
    val volumeMilliliters: Int,      // e.g. 45500 ml
    val costCents: Long,             // e.g. 6443 cents
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
