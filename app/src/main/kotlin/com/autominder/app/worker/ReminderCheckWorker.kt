package com.autominder.app.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.autominder.app.core.notifications.NotificationHelper
import com.autominder.app.data.local.dao.ReminderDao
import com.autominder.app.data.local.dao.VehicleDao
import com.autominder.app.data.local.preferences.UserPreferences
import com.autominder.app.domain.model.ServiceStatus
import com.autominder.app.domain.usecase.StatusCalculator
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import timber.log.Timber

/**
 * Periodic worker that checks all pending reminders and fires notifications
 * for any that are OVERDUE or DUE_SOON.
 *
 * Runs every 6 hours (scheduled via [WorkScheduler]) and after device boot
 * (via [BootReceiver]).
 */
@HiltWorker
class ReminderCheckWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val reminderDao: ReminderDao,
    private val vehicleDao: VehicleDao,
    private val userPreferences: UserPreferences
) : CoroutineWorker(context, params) {

    companion object {
        /** Don't re-notify if we notified less than 24 hours ago. */
        private const val NOTIFICATION_COOLDOWN_MS = 24 * 60 * 60 * 1000L
    }

    override suspend fun doWork(): Result {
        Timber.d("ReminderCheckWorker: starting reminder check")

        // Respect the user's notification preference — never send if disabled
        if (!userPreferences.notificationsEnabled.first()) {
            Timber.d("ReminderCheckWorker: notifications disabled by user, skipping")
            return Result.success()
        }

        return try {
            val now = System.currentTimeMillis()
            val pendingReminders = reminderDao.getAllPendingRemindersOnce()

            Timber.d("ReminderCheckWorker: found ${pendingReminders.size} pending reminders")

            for (reminder in pendingReminders) {
                // Fetch the vehicle for this reminder to get currentOdometer
                val vehicle = vehicleDao.getVehicleById(reminder.vehicleId).firstOrNull()
                if (vehicle == null) {
                    Timber.w("ReminderCheckWorker: vehicle ${reminder.vehicleId} not found for reminder ${reminder.id}")
                    continue
                }

                val status = StatusCalculator.calculate(
                    nowMillis = now,
                    currentOdometer = vehicle.currentOdometer,
                    dueDateMillis = reminder.nextDueDate,
                    dueOdometer = reminder.nextDueOdometer,
                    snoozeUntilMillis = reminder.snoozeUntil,
                    isCompleted = reminder.isCompleted
                )

                // Only notify for OVERDUE or DUE_SOON
                if (status != ServiceStatus.OVERDUE && status != ServiceStatus.DUE_SOON) {
                    continue
                }

                // Check cooldown: skip if we notified less than 24 hours ago
                val lastNotified = reminder.lastNotifiedAt
                if (lastNotified != null && (now - lastNotified) < NOTIFICATION_COOLDOWN_MS) {
                    Timber.d("ReminderCheckWorker: skipping reminder ${reminder.id}, notified ${(now - lastNotified) / 3600000}h ago")
                    continue
                }

                // Build notification content
                val vehicleName = "${vehicle.year} ${vehicle.make} ${vehicle.model}"
                val serviceLabel = reminder.customLabel
                    ?: reminder.serviceType.name.replace('_', ' ')
                        .lowercase()
                        .replaceFirstChar { it.uppercase() }

                val title = if (status == ServiceStatus.OVERDUE) {
                    "Overdue: $serviceLabel"
                } else {
                    "Due Soon: $serviceLabel"
                }

                val body = buildNotificationBody(status, vehicleName, reminder.nextDueDate, reminder.nextDueOdometer, vehicle.currentOdometer)

                Timber.d("ReminderCheckWorker: notifying reminder ${reminder.id} ($status) for $vehicleName")

                NotificationHelper.showReminderNotification(
                    context = applicationContext,
                    reminderId = reminder.id,
                    vehicleId = reminder.vehicleId,
                    title = title,
                    body = body
                )

                // Update lastNotifiedAt so we don't spam
                reminderDao.updateLastNotifiedAt(reminder.id, now)
            }

            Timber.d("ReminderCheckWorker: completed successfully")
            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "ReminderCheckWorker: failed")
            Result.retry()
        }
    }

    private fun buildNotificationBody(
        status: ServiceStatus,
        vehicleName: String,
        dueDateMillis: Long?,
        dueOdometer: Int?,
        currentOdometer: Int
    ): String {
        val parts = mutableListOf(vehicleName)

        if (dueDateMillis != null) {
            val daysUntil = ((dueDateMillis - System.currentTimeMillis()) / (24 * 60 * 60 * 1000)).toInt()
            when {
                daysUntil < 0 -> parts.add("${-daysUntil} days overdue")
                daysUntil == 0 -> parts.add("due today")
                else -> parts.add("due in $daysUntil days")
            }
        }

        if (dueOdometer != null) {
            val kmRemaining = dueOdometer - currentOdometer
            when {
                kmRemaining < 0 -> parts.add("${-kmRemaining} km overdue")
                kmRemaining == 0 -> parts.add("due now by odometer")
                else -> parts.add("$kmRemaining km remaining")
            }
        }

        return parts.joinToString(" - ")
    }
}
