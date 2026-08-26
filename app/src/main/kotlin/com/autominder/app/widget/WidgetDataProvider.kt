package com.autominder.app.widget

import android.content.Context
import androidx.glance.appwidget.updateAll
import com.autominder.app.data.local.dao.FuelDao
import com.autominder.app.data.local.dao.ReminderDao
import com.autominder.app.data.local.dao.VehicleDao
import com.autominder.app.data.local.preferences.UserPreferences
import com.autominder.app.domain.model.ServiceStatus
import com.autominder.app.domain.model.ServiceType
import com.autominder.app.domain.usecase.StatusCalculator
import com.autominder.app.domain.util.DistanceUtil
import com.autominder.app.ui.util.DateFormatUtil
import com.autominder.app.ui.util.DistanceFormat
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import timber.log.Timber
import java.util.Locale

enum class WidgetStateType {
    LOADING,
    EMPTY,
    SETUP_INCOMPLETE,
    HEALTHY,
    DUE_SOON,
    OVERDUE,
    ERROR
}

data class UrgentReminderInfo(
    val id: Long,
    val title: String,
    val status: ServiceStatus,
    val dueDateFormatted: String?,
    val distanceRemainingKm: Int?,
    val isOverdue: Boolean
)

data class AutoMinderWidgetState(
    val stateType: WidgetStateType = WidgetStateType.LOADING,
    val vehicleId: Long? = null,
    val vehicleName: String? = null,
    val currentOdometerFormatted: String? = null,
    val distanceUnit: String = "km",
    val urgentReminder: UrgentReminderInfo? = null,
    val overdueCount: Int = 0,
    val dueSoonCount: Int = 0,
    val totalActiveReminders: Int = 0,
    val avgEfficiencyText: String? = null,
    val errorMessage: String? = null
)

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun reminderDao(): ReminderDao
    fun vehicleDao(): VehicleDao
    fun fuelDao(): FuelDao
    fun userPreferences(): UserPreferences
}

object WidgetDataProvider {

    suspend fun loadWidgetState(context: Context): AutoMinderWidgetState {
        return try {
            val entryPoint = EntryPointAccessors.fromApplication(
                context.applicationContext,
                WidgetEntryPoint::class.java
            )
            val vehicleDao = entryPoint.vehicleDao()
            val reminderDao = entryPoint.reminderDao()
            val fuelDao = entryPoint.fuelDao()
            val userPreferences = entryPoint.userPreferences()

            val primaryVehicle = vehicleDao.getPrimaryVehicleOnce()
            if (primaryVehicle == null) {
                return AutoMinderWidgetState(stateType = WidgetStateType.EMPTY)
            }

            val distanceUnit = try {
                userPreferences.distanceUnit.first()
            } catch (_: Exception) {
                "km"
            }

            val unitLabel = DistanceUtil.unitLabel(distanceUnit)
            val displayOdometer = DistanceUtil.kmToDisplay(primaryVehicle.currentOdometer, distanceUnit)
            val formattedOdo = "${DistanceFormat.grouped(displayOdometer)} $unitLabel"
            val vehicleName = "${primaryVehicle.make} ${primaryVehicle.model}"

            // Read all reminders for this primary vehicle
            val reminders = reminderDao.getAllRemindersForVehicleOnce(primaryVehicle.id)
            val now = System.currentTimeMillis()

            var overdueCount = 0
            var dueSoonCount = 0
            var activeRemindersCount = 0

            var mostUrgentReminder: UrgentReminderInfo? = null
            var highestPriorityScore = -1 // 3: Overdue, 2: Due Soon, 1: OK

            for (reminder in reminders) {
                if (reminder.isCompleted) continue
                activeRemindersCount++

                val status = StatusCalculator.calculate(
                    nowMillis = now,
                    currentOdometer = primaryVehicle.currentOdometer,
                    dueDateMillis = reminder.nextDueDate,
                    dueOdometer = reminder.nextDueOdometer,
                    snoozeUntilMillis = reminder.snoozeUntil,
                    isCompleted = reminder.isCompleted
                )

                val distanceRemainingKm = if (reminder.nextDueOdometer != null && primaryVehicle.currentOdometer > 0) {
                    reminder.nextDueOdometer - primaryVehicle.currentOdometer
                } else null

                val title = reminder.customLabel ?: reminder.serviceType.label
                val dueFormatted = reminder.nextDueDate?.let { DateFormatUtil.formatDate(it) }

                when (status) {
                    ServiceStatus.OVERDUE -> {
                        overdueCount++
                        if (highestPriorityScore < 3) {
                            highestPriorityScore = 3
                            mostUrgentReminder = UrgentReminderInfo(
                                id = reminder.id,
                                title = title,
                                status = status,
                                dueDateFormatted = dueFormatted,
                                distanceRemainingKm = distanceRemainingKm,
                                isOverdue = true
                            )
                        }
                    }
                    ServiceStatus.DUE_SOON -> {
                        dueSoonCount++
                        if (highestPriorityScore < 2) {
                            highestPriorityScore = 2
                            mostUrgentReminder = UrgentReminderInfo(
                                id = reminder.id,
                                title = title,
                                status = status,
                                dueDateFormatted = dueFormatted,
                                distanceRemainingKm = distanceRemainingKm,
                                isOverdue = false
                            )
                        }
                    }
                    ServiceStatus.OK, ServiceStatus.SNOOZED -> {
                        if (highestPriorityScore < 1) {
                            highestPriorityScore = 1
                            mostUrgentReminder = UrgentReminderInfo(
                                id = reminder.id,
                                title = title,
                                status = status,
                                dueDateFormatted = dueFormatted,
                                distanceRemainingKm = distanceRemainingKm,
                                isOverdue = false
                            )
                        }
                    }
                    else -> Unit
                }
            }

            // Calculate fuel economy if available
            val fuelEntries = fuelDao.getFuelEntriesForVehicleOnce(primaryVehicle.id)
            val avgEfficiencyText = if (fuelEntries.size >= 2) {
                val sortedAsc = fuelEntries.sortedBy { it.odometer }
                val totalDistance = sortedAsc.last().odometer - sortedAsc.first().odometer
                val totalLiters = fuelEntries.sumOf { it.volumeMilliliters } / 1000.0
                if (totalDistance > 0 && totalLiters > 0) {
                    if (distanceUnit == "mi") {
                        val miles = totalDistance * 0.621371
                        val gallons = totalLiters * 0.264172
                        String.format(Locale.US, "%.1f MPG", miles / gallons)
                    } else {
                        String.format(Locale.US, "%.1f km/L", totalDistance / totalLiters)
                    }
                } else null
            } else null

            // Determine final widget state
            val stateType = when {
                primaryVehicle.currentOdometer <= 0 && activeRemindersCount == 0 -> WidgetStateType.SETUP_INCOMPLETE
                overdueCount > 0 -> WidgetStateType.OVERDUE
                dueSoonCount > 0 -> WidgetStateType.DUE_SOON
                else -> WidgetStateType.HEALTHY
            }

            AutoMinderWidgetState(
                stateType = stateType,
                vehicleId = primaryVehicle.id,
                vehicleName = vehicleName,
                currentOdometerFormatted = formattedOdo,
                distanceUnit = distanceUnit,
                urgentReminder = mostUrgentReminder,
                overdueCount = overdueCount,
                dueSoonCount = dueSoonCount,
                totalActiveReminders = activeRemindersCount,
                avgEfficiencyText = avgEfficiencyText
            )
        } catch (e: Exception) {
            Timber.e(e, "WidgetDataProvider: failed to load widget state")
            AutoMinderWidgetState(
                stateType = WidgetStateType.ERROR,
                errorMessage = e.localizedMessage
            )
        }
    }

    suspend fun updateAllWidgets(context: Context) {
        try {
            AutoMinderWidget().updateAll(context)
        } catch (e: Exception) {
            Timber.w(e, "WidgetDataProvider: updateAll failed")
        }
    }
}
