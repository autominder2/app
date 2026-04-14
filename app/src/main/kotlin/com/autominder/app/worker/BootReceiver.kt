package com.autominder.app.worker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import timber.log.Timber

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Timber.d("BootReceiver: rescheduling reminder worker")
            WorkScheduler.scheduleReminderChecks(context)
        }
    }
}
