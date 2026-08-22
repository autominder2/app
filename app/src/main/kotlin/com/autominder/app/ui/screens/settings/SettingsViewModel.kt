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

sealed interface BackupOpState {
    data object Idle : BackupOpState
    data object InProgress : BackupOpState
    data class ExportSuccess(val totalRecords: Int) : BackupOpState
    data class ImportSuccess(val summary: BackupRestoreSummary) : BackupOpState
    data class Error(@StringRes val messageRes: Int) : BackupOpState
}

data class SettingsUiState(
    val notificationsEnabled: Boolean = true,
    val themeMode: String = "system",
    val distanceUnit: String = "km",
    val backupState: BackupOpState = BackupOpState.Idle
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferences: UserPreferences,
    private val subscriptionManager: SubscriptionManager,
    private val manualBackupManager: ManualBackupManager
) : ViewModel() {

    val isProUser: StateFlow<Boolean> = subscriptionManager.isProUser
    val productDetails: StateFlow<List<ProductDetails>> = subscriptionManager.productDetails
    val purchaseState: StateFlow<PurchaseState> = subscriptionManager.purchaseState
    val restoreState: StateFlow<RestoreState> = subscriptionManager.restoreState

    private val _backupOpState = MutableStateFlow<BackupOpState>(BackupOpState.Idle)
    val backupOpState: StateFlow<BackupOpState> = _backupOpState.asStateFlow()

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
        userPreferences.themeMode,
        userPreferences.distanceUnit,
        _backupOpState
    ) { notificationsEnabled, themeMode, distanceUnit, backupState ->
        SettingsUiState(
            notificationsEnabled = notificationsEnabled,
            themeMode = themeMode,
            distanceUnit = distanceUnit,
            backupState = backupState
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
