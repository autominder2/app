package com.autominder.app.worker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import timber.log.Timber

/**
 * Re-arms background scheduling after system events that silently clear it.
 *
 * Four events matter, and only the first was previously handled:
 *
 * - **BOOT_COMPLETED** — all alarms and some scheduled work are lost on reboot.
 * - **TIME_CHANGED / TIMEZONE_CHANGED** — every due *date* in this app is
 *   wall-clock based. A driver who flies to another country, or whose phone
 *   corrects its clock, has just shifted the meaning of every reminder.
 * - **MY_PACKAGE_REPLACED** — an app update clears exact alarms and can leave
 *   scheduled work referencing the previous worker definition.
 *
 * Re-arming is idempotent: [WorkScheduler] enqueues unique work with UPDATE, so
 * repeated broadcasts converge on one correct schedule rather than stacking.
 */
class SystemEventReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return

        if (action !in HANDLED_ACTIONS) {
            // Never act on an unexpected broadcast — a receiver that reschedules
            // on anything it is handed is a denial-of-service on its own app.
            Timber.w("SystemEventReceiver: ignoring unexpected action %s", action)
            return
        }

        Timber.d("SystemEventReceiver: re-arming background work after %s", action)
        WorkScheduler.scheduleReminderChecks(context)
        WorkScheduler.scheduleWeeklyDigest(context)
        // The daily alarm backstop is erased by reboot and app update, and its
        // wall-clock target is invalidated by a clock or timezone change — all
        // four of the actions handled here.
        ReminderAlarmScheduler.schedule(context)
    }

    private companion object {
        val HANDLED_ACTIONS = setOf(
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_MY_PACKAGE_REPLACED
        )
    }
}
