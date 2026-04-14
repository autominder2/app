package com.autominder.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.autominder.app.domain.model.ServiceType

/**
 * Room entity for maintenance reminders.
 * PRD schema — Section 3.2.
 *
 * Business rule: [ServiceType] stored as String via TypeConverter (not ordinal — ordinal is
 * fragile if enum is reordered).
 */
@Entity(
    tableName = "reminders",
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
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val vehicleId: Long,                       // FK — must be Long to match parent
    val serviceType: ServiceType,              // stored via TypeConverter
    val customLabel: String? = null,           // only for ServiceType.CUSTOM
    val intervalDays: Int? = null,             // null if date-interval not used
    val intervalKm: Int? = null,               // null if km-interval not used
    val nextDueDate: Long? = null,             // epoch millis
    val nextDueOdometer: Int? = null,
    val snoozeUntil: Long? = null,             // epoch millis — null if not snoozed
    val notifyDaysBefore: Int = 7,
    val lastNotifiedAt: Long? = null,
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
