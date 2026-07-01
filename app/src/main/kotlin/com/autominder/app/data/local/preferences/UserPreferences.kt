package com.autominder.app.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
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
     */
    val isProCached: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[IS_PRO_CACHED] ?: false
    }

    suspend fun setProCached(isPro: Boolean) {
        context.dataStore.edit { preferences ->
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
