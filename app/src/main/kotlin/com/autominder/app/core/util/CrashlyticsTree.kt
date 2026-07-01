package com.autominder.app.core.util

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber

/**
 * Advanced Timber tree for Production.
 * Expertise: This converts standard logs into a high-fidelity diagnostic
 * trail in Firebase Crashlytics, separating "Breadcrumbs" from "Issues."
 */
class CrashlyticsTree : Timber.Tree() {

    companion object {
        private const val KEY_PRIORITY = "priority"
        private const val KEY_TAG = "tag"
    }

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        // Expertise: Filter out VERBOSE/DEBUG in production to save battery and network bandwidth.
        if (priority <= Log.DEBUG) {
            return
        }

        val crashlytics = FirebaseCrashlytics.getInstance()

        // 1. Breadcrumb Logging
        // crashlytics.log() builds a timeline (up to 64KB) uploaded ONLY when a crash occurs.
        val priorityChar = when (priority) {
            Log.INFO -> "I"
            Log.WARN -> "W"
            Log.ERROR -> "E"
            Log.ASSERT -> "A"
            else -> "U"
        }
        crashlytics.log("[$priorityChar] ${tag ?: "NoTag"}: $message")

        // 2. State-Aware Metadata
        crashlytics.setCustomKey(KEY_PRIORITY, priorityChar)
        tag?.let { crashlytics.setCustomKey(KEY_TAG, it) }

        // 3. Issue Recording
        if (t != null) {
            // An explicit exception was provided (e.g. Timber.e(ex, "msg"))
            crashlytics.recordException(t)
        } else if (priority >= Log.ERROR) {
            // Expertise: Wrap bare ERROR logs in a synthetic exception so they
            // appear in the Firebase "Issues" dashboard for triage.
            crashlytics.recordException(Exception("Non-fatal Error: $message"))
        }
    }
}
