package com.autominder.app.core.util

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import androidx.core.content.getSystemService
import timber.log.Timber

/**
 * The permission-free route to Android's battery optimization settings.
 *
 * Battery optimization is the single largest cause of missed reminders on
 * real devices, and it is the one cause the app cannot fix on its own — only
 * the user can grant the exemption.
 *
 * Two intents exist for this and only one of them is safe:
 *
 * - [Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS] opens the system
 *   list and needs no permission. This is what we use.
 * - `ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` shows a one-tap dialog but
 *   requires holding `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`, which Play treats
 *   as a policy-restricted permission with a narrow list of eligible use cases.
 *   A maintenance reminder app is not on that list, so we do not declare it.
 */
object PowerSettings {

    /**
     * Whether this app is currently exempt from battery optimization.
     *
     * Used to keep the guidance honest: telling an already-exempt user to go
     * change a setting they have already changed reads as the app not knowing
     * its own state.
     */
    fun isIgnoringBatteryOptimizations(context: Context): Boolean {
        val powerManager = context.getSystemService<PowerManager>() ?: return false
        return powerManager.isIgnoringBatteryOptimizations(context.packageName)
    }

    /**
     * Opens the battery optimization settings list, falling back to this app's
     * own settings page.
     *
     * The fallback is not defensive padding. Several OEM ROMs — including the
     * ones whose aggressive process management makes this screen necessary in
     * the first place — ship without a handler for the standard action, and an
     * unhandled intent throws [ActivityNotFoundException] rather than failing
     * quietly.
     *
     * @return true if some settings screen was opened.
     */
    fun openBatteryOptimizationSettings(context: Context): Boolean {
        val opened = launch(context, Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
        if (opened) return true

        Timber.w("PowerSettings: battery optimization screen unavailable, opening app details")
        return launch(
            context,
            Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", context.packageName, null)
            )
        )
    }

    private fun launch(context: Context, intent: Intent): Boolean = try {
        // A non-Activity context has no task to launch into. Adding the flag
        // unconditionally would instead detach the settings screen from the
        // app's back stack, so it is applied only where it is required.
        if (context !is Activity) intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
        true
    } catch (e: ActivityNotFoundException) {
        false
    } catch (e: SecurityException) {
        Timber.w(e, "PowerSettings: settings screen refused the launch")
        false
    }
}
