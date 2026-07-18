package com.autominder.app.core.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.autominder.app.core.util.AnalyticsEvents
import com.autominder.app.core.util.AnalyticsHelper
import com.autominder.app.domain.repository.IReminderRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Handles Done / Snooze taps directly from a reminder notification —
 * the user never has to open the app to act. This is the core retention
 * surface: notification-delivered -> action is a primary product metric.
 *
 * Snooze uses the DUE_SOON re-notify window (3 days). OVERDUE items will
 * still surface again through the daily ReminderCheckWorker if the snooze
 * expires — the StatusCalculator invariant (OVERDUE wins over SNOOZED)
 * remains the single source of truth.
 */
@AndroidEntryPoint
class NotificationActionReceiver : BroadcastReceiver() {

    @Inject lateinit var reminderRepository: IReminderRepository
    @Inject lateinit var analytics: AnalyticsHelper

    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getLongExtra(NotificationHelper.EXTRA_REMINDER_ID, -1L)
        if (reminderId <= 0L) return
        val action = intent.action ?: return

        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                // goAsync grants ~10s before the system may kill the process;
                // finish inside 8s so we always exit cleanly, never via ANR.
                withTimeout(8_000L) {
                    when (action) {
                        NotificationHelper.ACTION_MARK_DONE -> {
                            // DAO is an absolute UPDATE (isCompleted=1) — a
                            // double-tap is idempotent by construction.
                            reminderRepository.markCompleted(reminderId)
                            analytics.logEvent(AnalyticsEvents.NOTIF_ACTION_DONE)
                        }
                        NotificationHelper.ACTION_SNOOZE -> {
                            // Fixed 72h epoch offset: immune to time zones and
                            // DST; persisted in Room so it survives reboot.
                            reminderRepository.snoozeReminder(
                                reminderId,
                                System.currentTimeMillis() + TimeUnit.DAYS.toMillis(3)
                            )
                            analytics.logEvent(AnalyticsEvents.NOTIF_ACTION_SNOOZE)
                        }
                    }
                    NotificationManagerCompat.from(context).cancel(reminderId.toInt())
                }
            } catch (t: Throwable) {
                Timber.e(t, "Notification action failed: %s", action)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
