package com.autominder.app.worker

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.content.getSystemService
import timber.log.Timber
import java.util.Calendar

/**
 * A once-daily backstop under [WorkScheduler]'s 6-hour periodic check.
 *
 * WorkManager is explicitly best-effort. In Doze it defers periodic work to
 * maintenance windows, App Standby buckets stretch those windows further, and
 * several OEM ROMs stop background work outright. A maintenance reminder that
 * arrives four days late has already failed the user.
 *
 * [AlarmManager.setAndAllowWhileIdle] is the one scheduling primitive that
 * pierces Doze **without a restricted permission**. It is deliberately not
 * `setExactAndAllowWhileIdle`: that requires `SCHEDULE_EXACT_ALARM`, which Play
 * grants only to alarm-clock and calendar-class apps and would put this app's
 * listing at risk for no benefit — nothing here needs minute accuracy.
 *
 * Two platform behaviours shape the design:
 *
 * - `setAndAllowWhileIdle` is **one-shot**. There is no allow-while-idle
 *   repeating variant, so [ReminderAlarmReceiver] re-arms on every fire.
 * - Alarms are erased by reboot and by app update, and their wall-clock target
 *   is invalidated by a clock or timezone change — which is why
 *   [SystemEventReceiver] re-arms on all four events.
 *
 * One alarm covers the whole garage. Per-vehicle or per-reminder alarms would
 * multiply PendingIntents against the 500-alarm ceiling and re-arming surface
 * without checking anything the single pass does not already check.
 */
object ReminderAlarmScheduler {

    /** Explicit, app-internal action. Never exported. */
    const val ACTION_DAILY_CHECK = "com.autominder.app.action.DAILY_REMINDER_CHECK"

    private const val REQUEST_CODE = 8021

    /**
     * Local hour of the daily pass. Morning is when a driver can still act on
     * what they are told — an evening alert about a due inspection arrives
     * after every workshop has closed.
     */
    private const val CHECK_HOUR = 9

    /**
     * Arms (or re-arms) the daily pass. Idempotent: `FLAG_UPDATE_CURRENT`
     * replaces any existing alarm rather than stacking a second one, so
     * repeated calls from app start, boot and clock changes converge on one.
     */
    fun schedule(context: Context) {
        val alarmManager = context.getSystemService<AlarmManager>()
        if (alarmManager == null) {
            Timber.w("ReminderAlarmScheduler: AlarmManager unavailable, relying on WorkManager only")
            return
        }

        // getBroadcast is platform-nullable. With FLAG_UPDATE_CURRENT it does
        // not return null in practice, but the contract is the platform's.
        val operation = pendingIntent(context, PendingIntent.FLAG_UPDATE_CURRENT)
        if (operation == null) {
            Timber.w("ReminderAlarmScheduler: could not build the alarm intent")
            return
        }

        val triggerAt = nextTriggerMillis(System.currentTimeMillis())

        try {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, operation)
            Timber.d("ReminderAlarmScheduler: daily check armed for %d", triggerAt)
        } catch (e: SecurityException) {
            // Per-app alarm quotas can reject this. A missing backstop is a
            // degraded schedule, not a broken app — the periodic worker stands.
            Timber.w(e, "ReminderAlarmScheduler: alarm rejected, falling back to WorkManager cadence")
        }
    }

    /** Used when reminders are switched off, so no alarm outlives the feature. */
    fun cancel(context: Context) {
        val alarmManager = context.getSystemService<AlarmManager>() ?: return
        val existing = pendingIntent(context, PendingIntent.FLAG_NO_CREATE) ?: return
        alarmManager.cancel(existing)
        existing.cancel()
        Timber.d("ReminderAlarmScheduler: daily check cancelled")
    }

    /**
     * Next occurrence of [CHECK_HOUR] local time, strictly in the future.
     *
     * Internal rather than private so the roll-to-tomorrow boundary is
     * testable without a device clock.
     */
    internal fun nextTriggerMillis(nowMillis: Long): Long {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = nowMillis
            set(Calendar.HOUR_OF_DAY, CHECK_HOUR)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (calendar.timeInMillis <= nowMillis) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        return calendar.timeInMillis
    }

    private fun pendingIntent(context: Context, extraFlags: Int): PendingIntent? {
        val intent = Intent(context, ReminderAlarmReceiver::class.java).apply {
            action = ACTION_DAILY_CHECK
        }
        return PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            // IMMUTABLE is required from API 31 and correct everywhere: nothing
            // outside this app has any business filling in this intent.
            extraFlags or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
