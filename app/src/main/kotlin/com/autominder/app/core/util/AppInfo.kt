package com.autominder.app.core.util

import android.content.Context
import android.content.pm.PackageManager
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Install-time facts about this app.
 *
 * Exists so ViewModels can reason about install age without holding a
 * [Context] — the value is read once at construction and never changes.
 */
@Singleton
class AppInfo @Inject constructor(
    @ApplicationContext context: Context
) {

    /**
     * Epoch millis of first install.
     *
     * This is the floor for every "has the background engine been silent too
     * long?" question. Without it, a fresh install — which has genuinely never
     * run a check — is indistinguishable from an install whose worker has been
     * killed, and every new user would be greeted by a warning about a failure
     * that has not happened.
     */
    val firstInstallTimeMillis: Long = runCatching {
        context.packageManager.getPackageInfo(context.packageName, 0).firstInstallTime
    }.getOrElse { e ->
        Timber.w(e as? PackageManager.NameNotFoundException ?: e, "AppInfo: install time unavailable")
        // Treating an unreadable install time as "just now" suppresses the
        // staleness banner rather than firing it on bad data. A missed warning
        // is recoverable; a false one teaches the user to ignore real ones.
        System.currentTimeMillis()
    }
}
