package com.autominder.app.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.autominder.app.R
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
 * Weekly planning digest: one grouped notification listing everything that is
 * OVERDUE or DUE_SOON across all vehicles. Complements (does not replace) the
 * per-reminder alerts from [ReminderCheckWorker] — urgent items still ping
 * individually; this is the calm Sunday-morning overview.
 *
 * Tesla's curation principle applies: when nothing needs attention, we send
 * nothing. Silence is the feature.
 */
@HiltWorker
class WeeklyDigestWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val reminderDao: ReminderDao,
    private val vehicleDao: VehicleDao,
    private val userPreferences: UserPreferences
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        if (!userPreferences.notificationsEnabled.first()) {
            Timber.d("WeeklyDigestWorker: notifications disabled, skipping")
            return Result.success()
        }

        return try {
            val now = System.currentTimeMillis()
            val pending = reminderDao.getAllPendingRemindersOnce()

            val lines = mutableListOf<String>()
            for (reminder in pending) {
                val vehicle = vehicleDao.getVehicleById(reminder.vehicleId).firstOrNull() ?: continue
                val status = StatusCalculator.calculate(
                    nowMillis = now,
                    currentOdometer = vehicle.currentOdometer,
                    dueDateMillis = reminder.nextDueDate,
                    dueOdometer = reminder.nextDueOdometer,
                    snoozeUntilMillis = reminder.snoozeUntil,
                    isCompleted = reminder.isCompleted
                )
                if (status != ServiceStatus.OVERDUE && status != ServiceStatus.DUE_SOON) continue

                val serviceLabel = reminder.customLabel ?: reminder.serviceType.label
                val vehicleName = listOf(vehicle.make, vehicle.model)
                    .filter { it.isNotBlank() }
                    .joinToString(" ")
                val statusWord = applicationContext.getString(
                    if (status == ServiceStatus.OVERDUE) R.string.digest_status_overdue
                    else R.string.digest_status_due_soon
                )
                lines.add("$serviceLabel · $vehicleName — $statusWord")
            }

            if (lines.isEmpty()) {
                Timber.d("WeeklyDigestWorker: all clear, staying silent")
                return Result.success()
            }

            NotificationHelper.showWeeklyDigest(
                context = applicationContext,
                title = applicationContext.getString(R.string.digest_title),
                summary = applicationContext.resources.getQuantityString(
                    R.plurals.digest_summary,
                    lines.size,
                    lines.size
                ),
                lines = lines
            )
            Timber.d("WeeklyDigestWorker: sent digest with ${lines.size} lines")
            Result.success()
        } catch (e: Exception) {
            // Never retry-storm a summary — next week's run covers it.
            Timber.e(e, "WeeklyDigestWorker: failed; deferring to next scheduled run")
            Result.success()
        }
    }
}
