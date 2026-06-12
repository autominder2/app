package com.autominder.app

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.autominder.app.billing.SubscriptionManager
import com.autominder.app.core.notifications.NotificationHelper
import com.autominder.app.worker.WorkScheduler
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class AutoMinderApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var subscriptionManager: SubscriptionManager

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        // Create notification channel and schedule periodic reminder checks
        NotificationHelper.createChannel(this)
        WorkScheduler.scheduleReminderChecks(this)

        subscriptionManager.initialize()

        // Mobile Ads is intentionally NOT initialized here — MainActivity
        // runs the UMP consent gate first (GDPR), then initializes ads.
    }
}
