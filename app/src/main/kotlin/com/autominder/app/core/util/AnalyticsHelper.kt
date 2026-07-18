package com.autominder.app.core.util

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

interface AnalyticsHelper {
    fun logEvent(name: String, params: Map<String, Any?> = emptyMap())
    fun setUserProperty(name: String, value: String?)
}

@Singleton
class FirebaseAnalyticsHelper @Inject constructor(
    @ApplicationContext context: Context
) : AnalyticsHelper {
    private val firebaseAnalytics = FirebaseAnalytics.getInstance(context)

    override fun logEvent(name: String, params: Map<String, Any?>) {
        val bundle = Bundle().apply {
            params.forEach { (key, value) ->
                when (value) {
                    is String -> putString(key, value)
                    is Int -> putInt(key, value)
                    is Long -> putLong(key, value)
                    is Double -> putDouble(key, value)
                    is Boolean -> putString(key, value.toString())
                    else -> putString(key, value?.toString())
                }
            }
        }
        firebaseAnalytics.logEvent(name, bundle)
    }

    override fun setUserProperty(name: String, value: String?) {
        firebaseAnalytics.setUserProperty(name, value)
    }
}

object AnalyticsEvents {
    const val ONBOARDING_STARTED = "onboarding_started"
    const val ONBOARDING_COMPLETED = "onboarding_completed"
    const val VEHICLE_ADDED = "vehicle_added"
    const val VEHICLE_ARCHIVED = "vehicle_archived"
    const val REMINDER_CREATED = "reminder_created"
    const val SERVICE_LOGGED = "service_logged"
    const val ODOMETER_UPDATED = "odometer_updated"
    const val HISTORY_EXPORTED = "history_exported"
    const val TRIAL_STARTED = "trial_started"
    const val PURCHASE_SUCCESS = "purchase_success"
    const val PURCHASE_FAILED = "purchase_failed"
    const val ADS_CONSENT_GIVEN = "ads_consent_given"
    const val ADS_CONSENT_DENIED = "ads_consent_denied"

    // Notification -> action funnel (privacy-safe: no reminder content logged)
    const val NOTIF_ACTION_DONE = "notif_action_done"
    const val NOTIF_ACTION_SNOOZE = "notif_action_snooze"
}

object AnalyticsParams {
    const val VEHICLE_MAKE = "vehicle_make"
    const val VEHICLE_MODEL = "vehicle_model"
    const val REMINDER_TYPE = "reminder_type"
    const val SERVICE_TYPE = "service_type"
    const val PRODUCT_ID = "product_id"
    const val ERROR_MESSAGE = "error_message"
    const val ODOMETER_VALUE = "odometer_value"
}
