package com.autominder.app.domain.usecase

import com.autominder.app.domain.model.Reminder
import com.autominder.app.domain.model.Service
import com.autominder.app.domain.model.ServiceStatus
import com.autominder.app.domain.model.ServiceType
import com.autominder.app.domain.model.Vehicle
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

enum class ReminderUrgency {
    OVERDUE,
    DUE_SOON,
    SAFETY_CRITICAL,
    TIME_SENSITIVE,
    MILEAGE_BASED,
    FUTURE
}

enum class DataConfidence {
    HIGH,
    MEDIUM,
    ESTIMATED,
    INCOMPLETE_DATA
}

data class PrioritizedReminder(
    val reminderWithStatus: ReminderWithStatus,
    val urgency: ReminderUrgency,
    val remainingKm: Int? = null,
    val remainingDays: Long? = null,
    val isSafetyCritical: Boolean = false,
    val categoryLabel: String = ""
)

data class ReminderExplanation(
    val serviceTitle: String,
    val isSafetyCritical: Boolean,
    val lastServiceDate: Long?,
    val lastServiceOdometer: Int?,
    val intervalKm: Int?,
    val intervalDays: Int?,
    val currentOdometer: Int,
    val targetDueOdometer: Int?,
    val targetDueDate: Long?,
    val remainingKm: Int?,
    val remainingDays: Long?,
    val confidence: DataConfidence,
    val ruleDescription: String
)

@Singleton
class ReminderPriorityEngine @Inject constructor() {

    fun rankReminders(
        items: List<ReminderWithStatus>,
        lastServices: Map<ServiceType, Service> = emptyMap(),
        nowMillis: Long = System.currentTimeMillis()
    ): List<PrioritizedReminder> {
        return items.map { item ->
            val reminder = item.reminder
            val currentOdo = item.vehicle?.currentOdometer ?: 0

            val remainingKm = if (reminder.nextDueOdometer != null && currentOdo > 0) {
                reminder.nextDueOdometer - currentOdo
            } else null

            val remainingDays = if (reminder.nextDueDate != null) {
                TimeUnit.MILLISECONDS.toDays(reminder.nextDueDate - nowMillis)
            } else null

            val isSafety = isSafetyCritical(reminder.serviceType)

            val urgency = when {
                item.status == ServiceStatus.OVERDUE -> ReminderUrgency.OVERDUE
                item.status == ServiceStatus.DUE_SOON -> ReminderUrgency.DUE_SOON
                isSafety -> ReminderUrgency.SAFETY_CRITICAL
                remainingDays != null && remainingDays in 0..30 -> ReminderUrgency.TIME_SENSITIVE
                remainingKm != null && remainingKm in 0..1500 -> ReminderUrgency.MILEAGE_BASED
                else -> ReminderUrgency.FUTURE
            }

            PrioritizedReminder(
                reminderWithStatus = item,
                urgency = urgency,
                remainingKm = remainingKm,
                remainingDays = remainingDays,
                isSafetyCritical = isSafety,
                categoryLabel = formatCategoryTitle(reminder.serviceType, reminder.customLabel)
            )
        }.sortedWith(
            compareBy<PrioritizedReminder> { it.urgency.ordinal }
                .thenBy { it.remainingKm ?: Int.MAX_VALUE }
                .thenBy { it.remainingDays ?: Long.MAX_VALUE }
        )
    }

    fun buildExplanation(
        item: ReminderWithStatus,
        lastService: Service?,
        nowMillis: Long = System.currentTimeMillis()
    ): ReminderExplanation {
        val reminder = item.reminder
        val vehicle = item.vehicle
        val currentOdo = vehicle?.currentOdometer ?: 0

        val remainingKm = if (reminder.nextDueOdometer != null && currentOdo > 0) {
            reminder.nextDueOdometer - currentOdo
        } else null

        val remainingDays = if (reminder.nextDueDate != null) {
            TimeUnit.MILLISECONDS.toDays(reminder.nextDueDate - nowMillis)
        } else null

        val confidence = determineConfidence(vehicle, lastService, nowMillis)
        val isSafety = isSafetyCritical(reminder.serviceType)

        val ruleDesc = when {
            reminder.intervalKm != null && reminder.intervalDays != null -> {
                val months = reminder.intervalDays / 30
                if (months > 0) {
                    "${formatNumber(reminder.intervalKm)} km or $months months"
                } else {
                    "${formatNumber(reminder.intervalKm)} km or ${reminder.intervalDays} days"
                }
            }
            reminder.intervalKm != null ->
                "Every ${formatNumber(reminder.intervalKm)} km"
            reminder.intervalDays != null -> {
                val months = reminder.intervalDays / 30
                if (months > 0) "Every $months months" else "Every ${reminder.intervalDays} days"
            }
            else -> "Scheduled maintenance rule"
        }

        return ReminderExplanation(
            serviceTitle = formatCategoryTitle(reminder.serviceType, reminder.customLabel),
            isSafetyCritical = isSafety,
            lastServiceDate = lastService?.serviceDate,
            lastServiceOdometer = lastService?.odometerAtService,
            intervalKm = reminder.intervalKm,
            intervalDays = reminder.intervalDays,
            currentOdometer = currentOdo,
            targetDueOdometer = reminder.nextDueOdometer,
            targetDueDate = reminder.nextDueDate,
            remainingKm = remainingKm,
            remainingDays = remainingDays,
            confidence = confidence,
            ruleDescription = ruleDesc
        )
    }

    fun isSafetyCritical(type: ServiceType): Boolean {
        return when (type) {
            ServiceType.BRAKE_SERVICE,
            ServiceType.TIRE_ROTATION,
            ServiceType.WIPER_BLADES -> true
            else -> false
        }
    }

    fun formatCategoryTitle(type: ServiceType, customLabel: String?): String {
        if (!customLabel.isNullOrBlank()) return customLabel
        return when (type) {
            ServiceType.OIL_CHANGE -> "Oil service"
            ServiceType.TIRE_ROTATION -> "Tire rotation"
            ServiceType.BRAKE_SERVICE -> "Brake inspection"
            ServiceType.AIR_FILTER -> "Air filter"
            ServiceType.CABIN_FILTER -> "Cabin filter"
            ServiceType.BATTERY -> "Battery check"
            ServiceType.WIPER_BLADES -> "Wiper blades"
            ServiceType.SPARK_PLUGS -> "Spark plugs"
            ServiceType.TRANSMISSION -> "Transmission service"
            ServiceType.COOLANT -> "Coolant flush"
            ServiceType.TIMING_BELT -> "Timing belt"
            ServiceType.INSPECTION -> "Vehicle inspection"
            ServiceType.INSURANCE -> "Insurance"
            ServiceType.REGISTRATION -> "Registration"
            ServiceType.EMISSIONS_TEST -> "Emissions test"
            ServiceType.CUSTOM -> "Maintenance service"
        }
    }

    private fun determineConfidence(
        vehicle: Vehicle?,
        lastService: Service?,
        nowMillis: Long
    ): DataConfidence {
        if (vehicle == null || vehicle.currentOdometer <= 0) return DataConfidence.INCOMPLETE_DATA

        val daysSinceVehicleUpdate = TimeUnit.MILLISECONDS.toDays(nowMillis - vehicle.updatedAt)
        return when {
            daysSinceVehicleUpdate <= 30 -> DataConfidence.HIGH
            daysSinceVehicleUpdate <= 90 -> DataConfidence.MEDIUM
            else -> DataConfidence.ESTIMATED
        }
    }

    private fun formatNumber(num: Int): String {
        return String.format("%,d", num)
    }
}
