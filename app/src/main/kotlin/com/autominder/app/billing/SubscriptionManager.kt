package com.autominder.app.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.PendingPurchasesParams
import com.autominder.app.data.local.preferences.UserPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubscriptionManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userPreferences: UserPreferences
) : PurchasesUpdatedListener {

    companion object {
        const val PRODUCT_MONTHLY = "autominder_pro_monthly"
        const val PRODUCT_YEARLY = "autominder_pro_yearly"
        const val PRODUCT_LIFETIME = "autominder_pro_lifetime"
        private const val MAX_RECONNECT_ATTEMPTS = 5
        private const val RECONNECT_BASE_DELAY_MS = 1_000L
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var reconnectAttempts = 0

    private val _isProUser = MutableStateFlow(false)
    val isProUser: StateFlow<Boolean> = _isProUser.asStateFlow()

    private val _productDetails = MutableStateFlow<List<ProductDetails>>(emptyList())
    val productDetails: StateFlow<List<ProductDetails>> = _productDetails.asStateFlow()

    private val _purchaseState = MutableStateFlow<PurchaseState>(PurchaseState.Idle)
    val purchaseState: StateFlow<PurchaseState> = _purchaseState.asStateFlow()

    private var billingClient: BillingClient? = null

    fun initialize() {
        // Offline cold start: honor the last entitlement Play confirmed so a
        // paying user is never locked out; Play reconciles when we connect.
        scope.launch {
            if (userPreferences.isProCached.first()) {
                _isProUser.value = true
            }
        }

        billingClient = BillingClient.newBuilder(context)
            .setListener(this)
            .enablePendingPurchases(
                PendingPurchasesParams.newBuilder()
                    .enableOneTimeProducts()
                    .enablePrepaidPlans()
                    .build()
            )
            .build()

        connect()
    }

    private fun connect() {
        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Timber.d("Billing connected")
                    reconnectAttempts = 0
                    queryProducts()
                    queryExistingPurchases()
                } else {
                    Timber.w("Billing setup failed: ${billingResult.debugMessage}")
                }
            }

            override fun onBillingServiceDisconnected() {
                Timber.w("Billing disconnected")
                if (reconnectAttempts < MAX_RECONNECT_ATTEMPTS) {
                    val backoff = RECONNECT_BASE_DELAY_MS * (1L shl reconnectAttempts)
                    reconnectAttempts++
                    scope.launch {
                        delay(backoff)
                        connect()
                    }
                }
            }
        })
    }

    private fun setProEntitlement(isPro: Boolean) {
        _isProUser.value = isPro
        scope.launch { userPreferences.setProCached(isPro) }
    }

    private fun queryProducts() {
        val subProducts = listOf(PRODUCT_MONTHLY, PRODUCT_YEARLY).map { productId ->
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(productId)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        }
        val inAppProducts = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PRODUCT_LIFETIME)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )

        val allProducts = subProducts + inAppProducts
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(allProducts)
            .build()

        billingClient?.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                _productDetails.value = productDetailsList
                Timber.d("Products loaded: ${productDetailsList.size}")
            }
        }
    }

    private fun queryExistingPurchases() {
        val subsParams = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()

        billingClient?.queryPurchasesAsync(subsParams) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                // Acknowledge anything that slipped through (e.g. app killed
                // right after purchase) — unacknowledged purchases are
                // auto-refunded by Play after 3 days.
                purchases.filter { it.purchaseState == Purchase.PurchaseState.PURCHASED }
                    .forEach { acknowledgePurchase(it) }
                val hasActiveSub = purchases.any {
                    it.purchaseState == Purchase.PurchaseState.PURCHASED
                }
                if (hasActiveSub) {
                    setProEntitlement(true)
                    return@queryPurchasesAsync
                }
            }

            val inAppParams = QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build()

            billingClient?.queryPurchasesAsync(inAppParams) { inAppResult, inAppPurchases ->
                if (inAppResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    inAppPurchases.filter { it.purchaseState == Purchase.PurchaseState.PURCHASED }
                        .forEach { acknowledgePurchase(it) }
                    val hasLifetime = inAppPurchases.any {
                        it.products.contains(PRODUCT_LIFETIME) &&
                            it.purchaseState == Purchase.PurchaseState.PURCHASED
                    }
                    setProEntitlement(hasLifetime)
                }
            }
        }
    }

    fun launchPurchase(activity: Activity, productDetails: ProductDetails, offerToken: String? = null) {
        val productDetailsParamsBuilder = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(productDetails)

        if (offerToken != null) {
            productDetailsParamsBuilder.setOfferToken(offerToken)
        }

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productDetailsParamsBuilder.build()))
            .build()

        _purchaseState.value = PurchaseState.Processing

        billingClient?.launchBillingFlow(activity, billingFlowParams)
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                purchases?.forEach { purchase ->
                    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                        acknowledgePurchase(purchase)
                        setProEntitlement(true)
                        _purchaseState.value = PurchaseState.Success
                    }
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                _purchaseState.value = PurchaseState.Cancelled
            }
            else -> {
                _purchaseState.value = PurchaseState.Error(billingResult.debugMessage)
            }
        }
    }

    private fun acknowledgePurchase(purchase: Purchase) {
        if (!purchase.isAcknowledged) {
            val params = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()

            billingClient?.acknowledgePurchase(params) { billingResult ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Timber.d("Purchase acknowledged")
                }
            }
        }
    }

    fun resetPurchaseState() {
        _purchaseState.value = PurchaseState.Idle
    }

    fun destroy() {
        billingClient?.endConnection()
        billingClient = null
    }
}

sealed class PurchaseState {
    object Idle : PurchaseState()
    object Processing : PurchaseState()
    object Success : PurchaseState()
    object Cancelled : PurchaseState()
    data class Error(val message: String?) : PurchaseState()
}
