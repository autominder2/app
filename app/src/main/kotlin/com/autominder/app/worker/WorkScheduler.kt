package com.autominder.app.worker

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object WorkScheduler {

    private const val REMINDER_CHECK_WORK = "reminder_check_work"

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
}
