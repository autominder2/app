package com.autominder.app.worker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import timber.log.Timber

/**
 * Receives the daily backstop alarm from [ReminderAlarmScheduler].
 *
 * It does not check reminders itself. A [BroadcastReceiver] runs on the main
 * thread with roughly ten seconds before the system considers it hung, and
 * this pass touches the database and posts notifications. Instead it hands the
 * work to the same [ReminderCheckWorker] the periodic schedule uses, so there
 * is exactly one implementation of "check the garage" and both paths share its
 * cooldown rules, localization and heartbeat.
 *
 * Re-arming here is not optional: `setAndAllowWhileIdle` is one-shot, so the
 * chain stops the first time this is skipped.
 */
class ReminderAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ReminderAlarmScheduler.ACTION_DAILY_CHECK) {
            Timber.w("ReminderAlarmReceiver: ignoring unexpected action %s", intent.action)
            return
        }

        Timber.d("ReminderAlarmReceiver: daily backstop fired")

        // KEEP, not REPLACE: if the periodic worker is already queued or running,
        // a second pass would only re-read the same rows. The cooldown in
        // ReminderCheckWorker would suppress duplicate notifications anyway, but
        // not enqueueing is cheaper than relying on that.
        WorkManager.getInstance(context).enqueueUniqueWork(
            IMMEDIATE_CHECK_WORK,
            ExistingWorkPolicy.KEEP,
            OneTimeWorkRequestBuilder<ReminderCheckWorker>().build()
        )

        ReminderAlarmScheduler.schedule(context)
    }

    private companion object {
        const val IMMEDIATE_CHECK_WORK = "reminder_check_now"
    }
}
