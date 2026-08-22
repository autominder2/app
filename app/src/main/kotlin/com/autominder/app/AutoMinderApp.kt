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
import com.autominder.app.worker.ReminderAlarmScheduler
import com.autominder.app.worker.WorkScheduler
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Provider

@HiltAndroidApp
class AutoMinderApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var subscriptionManager: Provider<SubscriptionManager>

    @Inject
    lateinit var lifecycleObserver: Provider<AppLifecycleObserver>

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()

        setupLogging()
        setupStrictMode()

        NotificationHelper.createChannel(this)

        // These services are not required to draw the first screen.
        applicationScope.launch {
            delay(1_000)
            WorkScheduler.scheduleReminderChecks(this@AutoMinderApp)
            WorkScheduler.scheduleWeeklyDigest(this@AutoMinderApp)
            ReminderAlarmScheduler.schedule(this@AutoMinderApp)
            subscriptionManager.get().initialize()
            withContext(Dispatchers.Main.immediate) {
                ProcessLifecycleOwner.get().lifecycle.addObserver(lifecycleObserver.get())
            }
        }

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
