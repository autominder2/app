package com.autominder.app.domain.model

/**
 * Pure Kotlin domain model for a Maintenance Reminder.
 */
data class Reminder(
    val id: Long = 0,
    val vehicleId: Long,
    val serviceType: ServiceType,
    val customLabel: String? = null,
    val intervalDays: Int? = null,
    val intervalKm: Int? = null,
    val nextDueDate: Long? = null,
    val nextDueOdometer: Int? = null,
    val snoozeUntil: Long? = null,
    val notifyDaysBefore: Int = 7,
    val lastNotifiedAt: Long? = null,
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
    val notes: String = "",
    val createdAt: Long = 0,
    val updatedAt: Long = 0
)
