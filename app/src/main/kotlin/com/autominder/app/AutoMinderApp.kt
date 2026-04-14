package com.autominder.app

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.google.android.gms.ads.MobileAds
import com.autominder.app.ads.AdManager
import com.autominder.app.core.notifications.NotificationHelper
import com.autominder.app.worker.WorkScheduler
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class AutoMinderApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var adManager: AdManager

    private val applicationScope = CoroutineScope(kotlinx.coroutines.SupervisorJob() + Dispatchers.IO)

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

        // Initialize MobileAds off the main thread, then preload full-screen ads
        applicationScope.launch {
            MobileAds.initialize(this@AutoMinderApp) { initStatus ->
                Timber.d("MobileAds init: ${initStatus.adapterStatusMap}")
                adManager.preloadAds()
            }
        }
    }
}
