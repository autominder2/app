package com.autominder.app.ui.screens.settings

import android.app.Activity
import android.content.ContentResolver
import android.net.Uri
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.ProductDetails
import com.autominder.app.R
import com.autominder.app.billing.PurchaseState
import com.autominder.app.billing.RestoreState
import com.autominder.app.billing.SubscriptionManager
import com.autominder.app.data.backup.BackupRestoreSummary
import com.autominder.app.data.backup.ManualBackupManager
import com.autominder.app.data.local.preferences.UserPreferences
import com.autominder.app.domain.usecase.DeleteAllDataUseCase
import com.autominder.app.domain.usecase.GarageSummary
import com.autominder.app.domain.usecase.GetGarageSummaryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Whether maintenance reminders will actually arrive.
 *
 * The stored preference alone is not the answer. A user can grant
 * POST_NOTIFICATIONS, switch reminders on, and later revoke the permission in
 * Android settings - at which point the preference still says "on" while the
 * OS silently drops every notification. Modelling both halves is what stops the
 * switch from lying.
 */
sealed interface NotificationsState {
    /** Preference on and the OS permission granted: reminders will arrive. */
    data object Active : NotificationsState

    /** Preference off. The user chose this; nothing is wrong. */
    data object Off : NotificationsState

    /**
     * Preference on but the OS permission is denied. Reminders are silently
     * dropped, so the UI must say so and offer the system settings screen -
     * a permission request will not re-prompt once permanently denied.
     */
    data object BlockedBySystem : NotificationsState
}

sealed interface DeleteAllState {
    data object Idle : DeleteAllState
    data object InProgress : DeleteAllState
    data object Success : DeleteAllState
    data class Error(@StringRes val messageRes: Int) : DeleteAllState
}

sealed interface BackupOpState {
    data object Idle : BackupOpState
    data object InProgress : BackupOpState
    data class ExportSuccess(val totalRecords: Int) : BackupOpState
    data class ImportSuccess(val summary: BackupRestoreSummary) : BackupOpState
    data class Error(@StringRes val messageRes: Int) : BackupOpState
}

data class SettingsUiState(
    val notificationsEnabled: Boolean = true,
    /**
     * True when the OS has granted POST_NOTIFICATIONS. Recomputed on every
     * resume via [refreshNotificationPermission], because the user can change
     * it in Android settings while this screen is in the background.
     */
    val hasNotificationPermission: Boolean = true,
    val themeMode: String = "system",
    val distanceUnit: String = "km",
    val backupState: BackupOpState = BackupOpState.Idle,
    val deleteAllState: DeleteAllState = DeleteAllState.Idle
) {
    val notificationsState: NotificationsState
        get() = when {
            !notificationsEnabled -> NotificationsState.Off
            hasNotificationPermission -> NotificationsState.Active
            else -> NotificationsState.BlockedBySystem
        }
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferences: UserPreferences,
    private val subscriptionManager: SubscriptionManager,
    private val manualBackupManager: ManualBackupManager,
    private val deleteAllData: DeleteAllDataUseCase,
    getGarageSummary: GetGarageSummaryUseCase
) : ViewModel() {

    /**
     * Counts of what the user owns, for the card at the top of Settings.
     *
     * Kept as its own StateFlow rather than folded into [uiState]: it is driven
     * by three Room flows and changes for entirely different reasons than the
     * preference values, so combining them would rebuild the whole settings
     * state every time a service record is added.
     */
    val garageSummary: StateFlow<GarageSummary> = getGarageSummary()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = GarageSummary()
        )

    val isProUser: StateFlow<Boolean> = subscriptionManager.isProUser
    val productDetails: StateFlow<List<ProductDetails>> = subscriptionManager.productDetails
    val purchaseState: StateFlow<PurchaseState> = subscriptionManager.purchaseState
    val restoreState: StateFlow<RestoreState> = subscriptionManager.restoreState

    private val _backupOpState = MutableStateFlow<BackupOpState>(BackupOpState.Idle)
    val backupOpState: StateFlow<BackupOpState> = _backupOpState.asStateFlow()

    private val _deleteAllState = MutableStateFlow<DeleteAllState>(DeleteAllState.Idle)
    val deleteAllState: StateFlow<DeleteAllState> = _deleteAllState.asStateFlow()

    /**
     * Seeded optimistically so the switch does not flash "blocked" for one
     * frame before the first [refreshNotificationPermission] lands.
     */
    private val _hasNotificationPermission = MutableStateFlow(true)

    // Product-details-driven prices for the paywall — null until Play's
    // product query resolves. Never hardcoded, never assumed.
    val monthlyPriceText: StateFlow<String?> = productDetails
        .map { extractPrice(it, SubscriptionManager.PRODUCT_MONTHLY) }
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5_000), initialValue = null)

    val yearlyPriceText: StateFlow<String?> = productDetails
        .map { extractPrice(it, SubscriptionManager.PRODUCT_YEARLY) }
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5_000), initialValue = null)

    val lifetimePriceText: StateFlow<String?> = productDetails
        .map { extractPrice(it, SubscriptionManager.PRODUCT_LIFETIME) }
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5_000), initialValue = null)

    /**
     * For subscriptions, takes the last pricing phase (the steady-state
     * recurring price after any trial/intro phase Play Console may apply —
     * none is assumed or claimed by app copy). For the one-time lifetime
     * product, reads the one-time offer's formatted price directly.
     */
    private fun extractPrice(details: List<ProductDetails>, productId: String): String? {
        val product = details.find { it.productId == productId } ?: return null
        return if (productId == SubscriptionManager.PRODUCT_LIFETIME) {
            product.oneTimePurchaseOfferDetails?.formattedPrice
        } else {
            product.subscriptionOfferDetails
                ?.firstOrNull()
                ?.pricingPhases
                ?.pricingPhaseList
                ?.lastOrNull()
                ?.formattedPrice
        }
    }

    val uiState: StateFlow<SettingsUiState> = combine(
        userPreferences.notificationsEnabled,
        _hasNotificationPermission,
        userPreferences.themeMode,
        userPreferences.distanceUnit,
        _backupOpState,
        _deleteAllState
    ) { values ->
        SettingsUiState(
            notificationsEnabled = values[0] as Boolean,
            hasNotificationPermission = values[1] as Boolean,
            themeMode = values[2] as String,
            distanceUnit = values[3] as String,
            backupState = values[4] as BackupOpState,
            deleteAllState = values[5] as DeleteAllState
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsUiState()
    )

    fun launchPurchase(activity: Activity, productId: String) {
        val details = productDetails.value.find { it.productId == productId } ?: return
        val offerToken = details.subscriptionOfferDetails?.firstOrNull()?.offerToken
        subscriptionManager.launchPurchase(activity, details, offerToken)
    }

    fun resetPurchaseState() {
        subscriptionManager.resetPurchaseState()
    }

    fun restorePurchases() {
        subscriptionManager.restorePurchases()
    }

    fun resetRestoreState() {
        subscriptionManager.resetRestoreState()
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setNotificationsEnabled(enabled)
        }
    }

    /**
     * Re-reads the OS permission. Must be called on every ON_RESUME: the user
     * can revoke POST_NOTIFICATIONS in Android settings and return, and until
     * this runs the switch still claims reminders are on.
     */
    fun refreshNotificationPermission(granted: Boolean) {
        _hasNotificationPermission.value = granted
    }

    // ─── Erase everything ───────────────────────────────────────────────────

    /**
     * Deletes every vehicle and every record attached to one. Irreversible;
     * the UI must have taken an explicit confirmation before calling this.
     */
    fun deleteAllData() {
        if (_deleteAllState.value == DeleteAllState.InProgress) return
        viewModelScope.launch {
            _deleteAllState.value = DeleteAllState.InProgress
            deleteAllData.invoke()
                .onSuccess { _deleteAllState.value = DeleteAllState.Success }
                .onFailure { error ->
                    Timber.e(error, "Delete-all failed")
                    _deleteAllState.value = DeleteAllState.Error(R.string.settings_delete_all_error)
                }
        }
    }

    fun clearDeleteAllState() {
        _deleteAllState.value = DeleteAllState.Idle
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            userPreferences.setThemeMode(mode)
        }
    }

    fun setDistanceUnit(unit: String) {
        viewModelScope.launch {
            userPreferences.setDistanceUnit(unit)
        }
    }

    // ─── Data Sovereignty (Manual Backup & Restore) ─────────────────────────

    fun exportBackup(uri: Uri, contentResolver: ContentResolver) {
        viewModelScope.launch {
            _backupOpState.value = BackupOpState.InProgress
            try {
                val outputStream = contentResolver.openOutputStream(uri)
                if (outputStream == null) {
                    _backupOpState.value = BackupOpState.Error(R.string.backup_error_export_failed)
                    return@launch
                }

                val result = manualBackupManager.exportBackup(outputStream)
                result.fold(
                    onSuccess = { count ->
                        _backupOpState.value = BackupOpState.ExportSuccess(count)
                    },
                    onFailure = { e ->
                        Timber.e(e, "Export failed")
                        _backupOpState.value = BackupOpState.Error(R.string.backup_error_export_failed)
                    }
                )
            } catch (e: Exception) {
                Timber.e(e, "Exception during backup export")
                _backupOpState.value = BackupOpState.Error(R.string.backup_error_export_failed)
            }
        }
    }

    fun importBackup(uri: Uri, contentResolver: ContentResolver) {
        viewModelScope.launch {
            _backupOpState.value = BackupOpState.InProgress
            try {
                val inputStream = contentResolver.openInputStream(uri)
                if (inputStream == null) {
                    _backupOpState.value = BackupOpState.Error(R.string.backup_error_import_failed)
                    return@launch
                }

                val result = manualBackupManager.importBackup(inputStream)
                result.fold(
                    onSuccess = { summary ->
                        _backupOpState.value = BackupOpState.ImportSuccess(summary)
                    },
                    onFailure = { e ->
                        Timber.e(e, "Import failed")
                        _backupOpState.value = BackupOpState.Error(R.string.backup_error_import_failed)
                    }
                )
            } catch (e: Exception) {
                Timber.e(e, "Exception during backup import")
                _backupOpState.value = BackupOpState.Error(R.string.backup_error_import_failed)
            }
        }
    }

    fun clearBackupState() {
        _backupOpState.value = BackupOpState.Idle
    }
}
