package com.autominder.app.data.mapper

import com.autominder.app.data.local.entity.ReminderEntity
import com.autominder.app.domain.model.Reminder

/**
 * Mappers for Reminder entities and domain models.
 */
fun ReminderEntity.toDomain(): Reminder {
    return Reminder(
        id = id,
        vehicleId = vehicleId,
        serviceType = serviceType,
        customLabel = customLabel,
        intervalDays = intervalDays,
        intervalKm = intervalKm,
        nextDueDate = nextDueDate,
        nextDueOdometer = nextDueOdometer,
        snoozeUntil = snoozeUntil,
        notifyDaysBefore = notifyDaysBefore,
        lastNotifiedAt = lastNotifiedAt,
        isCompleted = isCompleted,
        completedAt = completedAt,
        notes = notes,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun Reminder.toEntity(): ReminderEntity {
    return ReminderEntity(
        id = id,
        vehicleId = vehicleId,
        serviceType = serviceType,
        customLabel = customLabel,
        intervalDays = intervalDays,
        intervalKm = intervalKm,
        nextDueDate = nextDueDate,
        nextDueOdometer = nextDueOdometer,
        snoozeUntil = snoozeUntil,
        notifyDaysBefore = notifyDaysBefore,
        lastNotifiedAt = lastNotifiedAt,
        isCompleted = isCompleted,
        completedAt = completedAt,
        notes = notes,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

