package com.autominder.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.autominder.app.domain.model.ServiceType

/**
 * Room entity for completed service records.
 * PRD schema — Section 3.3.
 *
 * Cost stored as Int cents (never Double — floating-point money is a bug).
 * e.g. $49.99  → 4999 cents.
 */
@Entity(
    tableName = "services",
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
data class ServiceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val vehicleId: Long,
    val serviceType: ServiceType,              // stored via TypeConverter
    val customLabel: String? = null,
    val odometerAtService: Int,
    val serviceDate: Long,                     // epoch millis
    val costCents: Int? = null,                // null if cost not recorded (not zero)
    val shopName: String? = null,
    val notes: String = "",
    val receiptPhotoUri: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
