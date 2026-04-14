package com.autominder.app.domain.usecase

import com.autominder.app.domain.model.Reminder
import com.autominder.app.domain.model.ServiceStatus
import com.autominder.app.domain.model.Vehicle
import com.autominder.app.domain.repository.IReminderRepository
import com.autominder.app.domain.repository.IVehicleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

/**
 * Domain model for a vehicle + its calculated maintenance status.
 */
data class VehicleWithStatus(
    val vehicle: Vehicle,
    val status: ServiceStatus,
    val overdueCount: Int = 0,
    val dueSoonCount: Int = 0
)

data class ReminderWithStatus(
    val reminder: Reminder,
    val vehicle: Vehicle?,
    val status: ServiceStatus
)

data class DashboardData(
    val vehiclesWithStatus: List<VehicleWithStatus>,
    val alertsCount: Int,
    val upcomingReminders: List<ReminderWithStatus>
)

/**
 * Aggregates vehicles and reminders into a high-level dashboard view.
 */
class GetDashboardDataUseCase @Inject constructor(
    private val vehicleRepository: IVehicleRepository,
    private val reminderRepository: IReminderRepository
) {
    operator fun invoke(): Flow<DashboardData> {
        return combine(
            vehicleRepository.getAllVehicles(),
            reminderRepository.getAllPendingReminders()
        ) { vehicles, allReminders ->
            val now = System.currentTimeMillis()
            
            // 1. Calculate status for all reminders
            val remindersWithStatus = allReminders.map { reminder ->
                val vehicle = vehicles.find { it.id == reminder.vehicleId }
                val status = StatusCalculator.calculate(
                    nowMillis = now,
                    currentOdometer = vehicle?.currentOdometer ?: 0,
                    dueDateMillis = reminder.nextDueDate,
                    dueOdometer = reminder.nextDueOdometer,
                    snoozeUntilMillis = reminder.snoozeUntil,
                    isCompleted = reminder.isCompleted
                )
                ReminderWithStatus(reminder, vehicle, status)
            }

            // 2. Aggregate status by vehicle
            val vehiclesWithStatus = vehicles.map { vehicle ->
                val vehicleReminders = remindersWithStatus.filter { it.reminder.vehicleId == vehicle.id }
                
                // Overall vehicle status is the highest severity of its active reminders
                val overallStatus = vehicleReminders
                    .map { it.status }
                    .maxByOrNull { it.severity } ?: ServiceStatus.OK

                val overdueCount = vehicleReminders.count { it.status == ServiceStatus.OVERDUE }
                val dueSoonCount = vehicleReminders.count { it.status == ServiceStatus.DUE_SOON }

                VehicleWithStatus(
                    vehicle = vehicle,
                    status = overallStatus,
                    overdueCount = overdueCount,
                    dueSoonCount = dueSoonCount
                )
            }

            // 3. Overall dashboard alert count
            val totalAlerts = remindersWithStatus.count { 
                it.status == ServiceStatus.OVERDUE || it.status == ServiceStatus.DUE_SOON 
            }

            // 4. Sorted list of upcoming/urgent reminders
            val sortedReminders = remindersWithStatus
                .sortedByDescending { it.status.severity }
                .take(5)

            DashboardData(
                vehiclesWithStatus = vehiclesWithStatus,
                alertsCount = totalAlerts,
                upcomingReminders = sortedReminders
            )
        }
    }
}
