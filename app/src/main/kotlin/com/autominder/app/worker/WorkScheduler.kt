package com.autominder.app.worker

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object WorkScheduler {

    private const val REMINDER_CHECK_WORK = "reminder_check_work"
    private const val WEEKLY_DIGEST_WORK = "weekly_digest_work"

    fun scheduleReminderChecks(context: Context) {
        val request = PeriodicWorkRequestBuilder<ReminderCheckWorker>(
            6, TimeUnit.HOURS
        ).build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            REMINDER_CHECK_WORK,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    /**
     * One planning digest per week. Initial delay of a day keeps the first
     * digest from landing during onboarding minutes after install.
     */
    fun scheduleWeeklyDigest(context: Context) {
        val request = PeriodicWorkRequestBuilder<WeeklyDigestWorker>(
            7, TimeUnit.DAYS
        )
            .setInitialDelay(1, TimeUnit.DAYS)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WEEKLY_DIGEST_WORK,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}
