package com.autominder.app.domain.usecase

import com.autominder.app.domain.model.Reminder
import com.autominder.app.domain.model.ServiceType
import com.autominder.app.domain.repository.IReminderRepository
import javax.inject.Inject

/**
 * Creates standard maintenance reminders for a newly added vehicle.
 * Industry-standard intervals based on manufacturer averages.
 */
class CreateDefaultRemindersUseCase @Inject constructor(
    private val reminderRepository: IReminderRepository
) {
    suspend operator fun invoke(vehicleId: Long, currentOdometer: Int) {
        val now = System.currentTimeMillis()
        STANDARD_TEMPLATES.forEach { template ->
            val reminder = Reminder(
                vehicleId = vehicleId,
                serviceType = template.serviceType,
                intervalKm = template.intervalKm,
                intervalDays = template.intervalDays,
                nextDueOdometer = currentOdometer + template.intervalKm,
                nextDueDate = now + (template.intervalDays.toLong() * 86_400_000L),
                createdAt = now,
                updatedAt = now
            )
            reminderRepository.insertReminder(reminder)
        }
    }

    companion object {
        private data class ReminderTemplate(
            val serviceType: ServiceType,
            val intervalKm: Int,
            val intervalDays: Int
        )

        private val STANDARD_TEMPLATES = listOf(
            ReminderTemplate(ServiceType.OIL_CHANGE, 8000, 180),
            ReminderTemplate(ServiceType.TIRE_ROTATION, 12000, 365),
            ReminderTemplate(ServiceType.AIR_FILTER, 20000, 365),
            ReminderTemplate(ServiceType.CABIN_FILTER, 20000, 365),
            ReminderTemplate(ServiceType.BRAKE_SERVICE, 40000, 730),
            ReminderTemplate(ServiceType.WIPER_BLADES, 15000, 365),
        )
    }
}
