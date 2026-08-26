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

        // No analytics event here on purpose.
        //
        // This used to call logEvent("session_start"). `session_start` is a
        // RESERVED Firebase Analytics name, so Firebase rejected every one of
        // them. Confirmed on a device running the release build:
        //
        //   E FA: Name is reserved. Type, name: event, session_start
        //   E FA: Invalid public event name. Event will not be logged (FE): session_start
        //
        // Nothing surfaced this in the app — the call succeeded, the SDK
        // discarded the event, and the dashboard simply had no sessions. It was
        // also redundant: Firebase collects `session_start` automatically.
        // Removing the call loses nothing and stops a silent no-op.
    }

    override fun onStop(owner: LifecycleOwner) {
        Timber.d("App entered background")
        val sessionDuration = (System.currentTimeMillis() - sessionStartTime) / 1000

        // Deliberately NOT "session_end" — that sits in the same reserved
        // namespace as `session_start` and risks the same silent drop. The
        // `am_` prefix marks it as first-party and cannot collide with a
        // Google-reserved or auto-collected name.
        analyticsHelper.logEvent(
            "am_session_end",
            mapOf("duration_seconds" to sessionDuration)
        )
    }
}
