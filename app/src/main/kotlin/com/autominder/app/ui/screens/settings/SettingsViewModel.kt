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
import kotlinx.coroutines.flow.map
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
