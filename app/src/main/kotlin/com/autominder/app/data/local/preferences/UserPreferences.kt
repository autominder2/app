package com.autominder.app.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "user_preferences"
)

/**
 * Entitlement cache — deliberately a SEPARATE DataStore file from
 * [dataStore] so that backup can exclude it by path.
 *
 * It cannot live in `user_preferences`. Auto Backup and device-to-device
 * transfer include `datastore/` wholesale (see res/xml/backup_rules.xml and
 * data_extraction_rules.xml), and everything else in `user_preferences` —
 * theme, units, onboarding, records liveness — SHOULD be restored: losing it
 * on a phone upgrade is the churn driver those rules exist to prevent.
 *
 * A cached Pro flag must not be, because restoring it grants Pro. The billing
 * reconcile in SubscriptionManager only downgrades on complete evidence (both
 * the SUBS and INAPP queries returning OK), which is the correct choice for a
 * paying user on a flaky connection — but it also means a restored `true`
 * survives indefinitely with no network to contradict it. Backup restore plus
 * airplane mode would otherwise be a permanent, no-tools entitlement bypass.
 *
 * Anything else that gates paid value by counting — a free-tier audit meter,
 * a trial start date — belongs in HERE, not in `user_preferences`, for exactly
 * the same reason: a counter that restores is a counter that resets.
 */
private val Context.entitlementStore: DataStore<Preferences> by preferencesDataStore(
    name = "entitlement_cache"
)

@Singleton
class UserPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val HAS_SEEN_ONBOARDING = booleanPreferencesKey("has_seen_onboarding")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val DISTANCE_UNIT = stringPreferencesKey("distance_unit")
        val IS_PRO_CACHED = booleanPreferencesKey("is_pro_cached")
        val SERVICE_LOG_COUNT = intPreferencesKey("service_log_count")
        val HAS_REQUESTED_REVIEW = booleanPreferencesKey("has_requested_review")

        /**
         * Wall-clock time of the last reminder check that ran to completion.
         *
         * This is the app's own liveness signal. WorkManager is best-effort:
         * Doze, App Standby and the aggressive process killers shipped by
         * several OEMs can stop background work entirely, and the platform
         * gives the app no notification when that happens. Comparing this
         * timestamp against now is the only way to discover we have been
         * silenced — and telling the user beats letting them believe they are
         * covered when they are not.
         */
        val LAST_SUCCESSFUL_CHECK_AT = longPreferencesKey("last_successful_check_at")
    }

    /** Epoch millis of the last completed reminder check, or null if never. */
    val lastSuccessfulCheckAt: Flow<Long?> = context.dataStore.data.map { preferences ->
        preferences[LAST_SUCCESSFUL_CHECK_AT]
    }

    suspend fun setLastSuccessfulCheckAt(epochMillis: Long) {
        context.dataStore.edit { preferences ->
            preferences[LAST_SUCCESSFUL_CHECK_AT] = epochMillis
        }
    }

    val notificationsEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[NOTIFICATIONS_ENABLED] ?: true
    }

    val hasSeenOnboarding: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[HAS_SEEN_ONBOARDING] ?: false
    }

    val themeMode: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[THEME_MODE] ?: "system"
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[NOTIFICATIONS_ENABLED] = enabled
        }
    }

    suspend fun setHasSeenOnboarding(seen: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[HAS_SEEN_ONBOARDING] = seen
        }
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[THEME_MODE] = mode
        }
    }

    val distanceUnit: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[DISTANCE_UNIT] ?: "km"
    }

    suspend fun setDistanceUnit(unit: String) {
        context.dataStore.edit { preferences ->
            preferences[DISTANCE_UNIT] = unit
        }
    }

    /**
     * Last entitlement Google Play confirmed. Lets a paying user keep Pro
     * features on an offline cold start; reconciled against Play whenever
     * the billing client connects.
     *
     * Reads and writes [entitlementStore], NOT [dataStore] — see that
     * property for why this one file is excluded from backup.
     */
    val isProCached: Flow<Boolean> = context.entitlementStore.data.map { preferences ->
        preferences[IS_PRO_CACHED] ?: false
    }

    suspend fun setProCached(isPro: Boolean) {
        context.entitlementStore.edit { preferences ->
            preferences[IS_PRO_CACHED] = isPro
        }
    }

    val serviceLogCount: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[SERVICE_LOG_COUNT] ?: 0
    }

    suspend fun incrementServiceLogCount() {
        context.dataStore.edit { preferences ->
            val current = preferences[SERVICE_LOG_COUNT] ?: 0
            preferences[SERVICE_LOG_COUNT] = current + 1
        }
    }

    val hasRequestedReview: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[HAS_REQUESTED_REVIEW] ?: false
    }

    suspend fun setHasRequestedReview(requested: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[HAS_REQUESTED_REVIEW] = requested
        }
    }
}
