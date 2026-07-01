package com.autominder.app.core.util

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppLifecycleObserver @Inject constructor(
    private val analyticsHelper: AnalyticsHelper
) : DefaultLifecycleObserver {

    private var sessionStartTime: Long = 0

    override fun onStart(owner: LifecycleOwner) {
        Timber.d("App entered foreground")
        sessionStartTime = System.currentTimeMillis()
        analyticsHelper.logEvent("session_start")
    }

    override fun onStop(owner: LifecycleOwner) {
        Timber.d("App entered background")
        val sessionDuration = (System.currentTimeMillis() - sessionStartTime) / 1000
        analyticsHelper.logEvent(
            "session_end",
            mapOf("duration_seconds" to sessionDuration)
        )
    }
}
