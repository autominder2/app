package com.autominder.app

import android.app.Application
import android.os.StrictMode
import androidx.hilt.work.HiltWorkerFactory
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.work.Configuration
import coil3.SingletonImageLoader
import coil3.request.allowHardware
import com.autominder.app.billing.SubscriptionManager
import com.autominder.app.core.notifications.NotificationHelper
import com.autominder.app.core.util.AppLifecycleObserver
import com.autominder.app.core.util.CrashlyticsTree
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

    @Inject
    lateinit var lifecycleObserver: AppLifecycleObserver

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()

        setupLogging()
        setupStrictMode()

        // Expertise: Observe global app lifecycle for session tracking and ad behavior
        ProcessLifecycleOwner.get().lifecycle.addObserver(lifecycleObserver)

        // Initialize Core Services
        NotificationHelper.createChannel(this)
        WorkScheduler.scheduleReminderChecks(this)
        WorkScheduler.scheduleWeeklyDigest(this)
        subscriptionManager.initialize()

        setupGlobalExceptionHandler()
    }

    /**
     * Expertise: Handle system memory pressure proactively to prevent LMK (Low Memory Killer).
     */
    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        Timber.w("onTrimMemory level: $level")
        // Clear Coil image caches when memory is tight
        SingletonImageLoader.get(this).apply {
            memoryCache?.clear()
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        Timber.e("onLowMemory triggered! Clearing all non-essential resources.")
        SingletonImageLoader.get(this).apply {
            memoryCache?.clear()
        }
    }

    private fun setupLogging() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            // Expertise: Production logging builds a searchable diagnostic trail in Crashlytics
            Timber.plant(CrashlyticsTree())
        }
    }

    private fun setupStrictMode() {
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build()
            )
            StrictMode.setVmPolicy(
                StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    .detectLeakedClosableObjects()
                    .penaltyLog()
                    .build()
            )
        }
    }

    private fun setupGlobalExceptionHandler() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Timber.e(throwable, "Uncaught exception on thread: ${thread.name}")
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }
}
