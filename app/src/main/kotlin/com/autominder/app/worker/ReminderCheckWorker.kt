package com.autominder.app.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.autominder.app.R
import com.autominder.app.core.notifications.NotificationHelper
import com.autominder.app.core.util.localizedLabel
import com.autominder.app.data.local.dao.ReminderDao
import com.autominder.app.data.local.dao.VehicleDao
import com.autominder.app.data.local.preferences.UserPreferences
import com.autominder.app.domain.model.ServiceStatus
import com.autominder.app.domain.usecase.StatusCalculator
import com.autominder.app.domain.util.DistanceUtil
import com.autominder.app.ui.util.DistanceFormat
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import timber.log.Timber

/**
 * Periodic worker that checks all pending reminders and fires notifications
 * for any that are OVERDUE or DUE_SOON.
 *
 * Runs every 6 hours (scheduled via [WorkScheduler]) and is re-armed by
 * [SystemEventReceiver] after boot, clock/timezone changes and app updates.
 *
 * On every clean pass it records a heartbeat
 * ([UserPreferences.setLastSuccessfulCheckAt]). WorkManager is best-effort —
 * Doze, App Standby and OEM process killers can stop it with no signal to the
 * app — so a stale heartbeat is the only evidence available that this engine
 * has been silenced.
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
        /** OVERDUE is urgent — daily re-notification is correct. */
        private const val OVERDUE_COOLDOWN_MS = 24 * 60 * 60 * 1000L

        /** DUE_SOON is planning info — every 3 days gives time without spam. */
        private const val DUE_SOON_COOLDOWN_MS = 3 * 24 * 60 * 60 * 1000L
    }

    override suspend fun doWork(): Result {
        Timber.d("ReminderCheckWorker: starting reminder check")

        // Respect the user's notification preference — never send if disabled
        if (!userPreferences.notificationsEnabled.first()) {
            Timber.d("ReminderCheckWorker: notifications disabled by user, skipping")
            // The engine still ran. The heartbeat tracks whether background work
            // is being executed at all, which is a different question from
            // whether the user wants notifications — recording it here keeps a
            // deliberate opt-out from masquerading as a killed worker.
            userPreferences.setLastSuccessfulCheckAt(System.currentTimeMillis())
            return Result.success()
        }

        return try {
            val now = System.currentTimeMillis()
            val distanceUnit = userPreferences.distanceUnit.first()
            val pendingReminders = reminderDao.getAllPendingRemindersOnce()

            Timber.d("ReminderCheckWorker: found ${pendingReminders.size} pending reminders")

            for (reminder in pendingReminders) {
                // Fetch the vehicle for this reminder to get currentOdometer
                // One-shot read, not a Flow collection: data.md requires the
                // getXOnce() form in workers so the reactive and one-shot paths
                // never blur. Collecting a Flow here also opens and abandons a
                // collector for every reminder in the loop.
                val vehicle = vehicleDao.getVehicleByIdOnce(reminder.vehicleId)
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

                // Severity-gated cooldown: urgent items daily, planning items
                // every 3 days — notification fatigue is the #1 uninstall cause.
                val cooldown = if (status == ServiceStatus.OVERDUE) OVERDUE_COOLDOWN_MS else DUE_SOON_COOLDOWN_MS
                val lastNotified = reminder.lastNotifiedAt
                if (lastNotified != null && (now - lastNotified) < cooldown) {
                    Timber.d("ReminderCheckWorker: skipping reminder ${reminder.id}, notified ${(now - lastNotified) / 3600000}h ago")
                    continue
                }

                // Build notification content. Everything user-visible comes from
                // strings.xml — this block previously emitted hardcoded English
                // and a raw enum name, so every locale received English text.
                val vehicleName = "${vehicle.year} ${vehicle.make} ${vehicle.model}"
                val serviceLabel = reminder.customLabel
                    ?: reminder.serviceType.localizedLabel(applicationContext)

                val title = applicationContext.getString(
                    if (status == ServiceStatus.OVERDUE) {
                        R.string.notification_title_overdue
                    } else {
                        R.string.notification_title_due_soon
                    },
                    serviceLabel
                )

                val body = buildNotificationBody(
                    vehicleName = vehicleName,
                    dueDateMillis = reminder.nextDueDate,
                    dueOdometer = reminder.nextDueOdometer,
                    currentOdometer = vehicle.currentOdometer,
                    distanceUnit = distanceUnit
                )

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

            // Heartbeat: only on a clean pass. Recording it in the catch block
            // would let a worker that runs but always fails look healthy, which
            // is precisely the silent failure this signal exists to expose.
            userPreferences.setLastSuccessfulCheckAt(now)

            Timber.d("ReminderCheckWorker: completed successfully")
            Result.success()
        } catch (e: Exception) {
            // Per CLAUDE.md cooldown rules: never retry-storm on partial notification failure.
            // The next scheduled run (6h cadence) will pick up missed reminders cleanly.
            Timber.e(e, "ReminderCheckWorker: failed; deferring to next scheduled run")
            Result.success()
        }
    }

    /**
     * Builds the notification body from localized resources.
     *
     * Two rules are load-bearing here. Quantities use plurals, because "1 days
     * overdue" is wrong in English and unrepresentable in languages with more
     * than two plural forms. Distances go through [DistanceFormat] so the
     * number is grouped for the locale and carries the user's chosen unit
     * rather than a hardcoded "km".
     */
    private fun buildNotificationBody(
        vehicleName: String,
        dueDateMillis: Long?,
        dueOdometer: Int?,
        currentOdometer: Int,
        distanceUnit: String
    ): String {
        val res = applicationContext.resources
        val parts = mutableListOf(vehicleName)

        if (dueDateMillis != null) {
            val daysUntil = ((dueDateMillis - System.currentTimeMillis()) / (24 * 60 * 60 * 1000)).toInt()
            parts += when {
                daysUntil < 0 ->
                    res.getQuantityString(R.plurals.notification_days_overdue, -daysUntil, -daysUntil)
                daysUntil == 0 ->
                    applicationContext.getString(R.string.notification_due_today)
                else ->
                    res.getQuantityString(R.plurals.notification_days_until, daysUntil, daysUntil)
            }
        }

        if (dueOdometer != null) {
            val remainingKm = dueOdometer - currentOdometer
            // Storage is always km; the user may read in miles. Convert first,
            // then group for the locale, then append their unit label — the
            // previous code hardcoded "km" for everyone.
            fun format(km: Int): String {
                val display = DistanceUtil.kmToDisplay(km, distanceUnit)
                return "${DistanceFormat.grouped(display)} ${DistanceUtil.unitLabel(distanceUnit)}"
            }
            parts += when {
                remainingKm < 0 -> applicationContext.getString(
                    R.string.notification_distance_overdue, format(-remainingKm)
                )
                remainingKm == 0 -> applicationContext.getString(R.string.notification_due_by_odometer)
                else -> applicationContext.getString(
                    R.string.notification_distance_remaining, format(remainingKm)
                )
            }
        }

        return parts.joinToString(applicationContext.getString(R.string.notification_body_separator))
    }
}
