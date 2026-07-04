package com.autominder.app.ui.screens.settings

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.ProductDetails
import com.autominder.app.billing.PurchaseState
import com.autominder.app.billing.RestoreState
import com.autominder.app.billing.SubscriptionManager
import com.autominder.app.data.local.preferences.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val notificationsEnabled: Boolean = true,
    val themeMode: String = "system",
    val distanceUnit: String = "km"
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferences: UserPreferences,
    private val subscriptionManager: SubscriptionManager
) : ViewModel() {

    val isProUser: StateFlow<Boolean> = subscriptionManager.isProUser
    val productDetails: StateFlow<List<ProductDetails>> = subscriptionManager.productDetails
    val purchaseState: StateFlow<PurchaseState> = subscriptionManager.purchaseState
    val restoreState: StateFlow<RestoreState> = subscriptionManager.restoreState

    val uiState: StateFlow<SettingsUiState> = combine(
        userPreferences.notificationsEnabled,
        userPreferences.themeMode,
        userPreferences.distanceUnit
    ) { notificationsEnabled, themeMode, distanceUnit ->
        SettingsUiState(
            notificationsEnabled = notificationsEnabled,
            themeMode = themeMode,
            distanceUnit = distanceUnit
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
}
