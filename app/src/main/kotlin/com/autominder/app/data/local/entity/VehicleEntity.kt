package com.autominder.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for vehicles.
 * PRD schema — all fields match Section 3.1 exactly.
 *
 * RULES:
 * • @PrimaryKey id → Long (NEVER Int)
 * • currentOdometer as Int (km or mi — app uses one unit consistently)
 * • ZERO nullable Doubles — money, measurements all stored as Int/Long
 */
@Entity(tableName = "vehicles")
data class VehicleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val make: String,
    val model: String,
    val year: Int,
    val plateNumber: String,
    val vin: String? = null,
    val currentOdometer: Int,
    val photoUri: String? = null,
    val isArchived: Boolean = false,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
