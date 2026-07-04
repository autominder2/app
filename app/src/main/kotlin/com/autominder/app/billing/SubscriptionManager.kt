package com.autominder.app.billing

import android.app.Activity
import android.content.Context
import androidx.annotation.StringRes
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
import com.autominder.app.R
import com.autominder.app.core.util.AnalyticsEvents
import com.autominder.app.core.util.AnalyticsHelper
import com.autominder.app.core.util.AnalyticsParams
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
    private val userPreferences: UserPreferences,
    private val analyticsHelper: AnalyticsHelper
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

    private val _restoreState = MutableStateFlow<RestoreState>(RestoreState.Idle)
    val restoreState: StateFlow<RestoreState> = _restoreState.asStateFlow()

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
        scope.launch {
            userPreferences.setProCached(isPro)
            analyticsHelper.setUserProperty("is_pro", isPro.toString())
        }
    }

    private fun queryProducts() {
        // The Billing library forbids mixing product types in one
        // QueryProductDetailsParams — SUBS and INAPP must be queried
        // separately, then merged.
        val subsParams = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(PRODUCT_MONTHLY, PRODUCT_YEARLY).map { productId ->
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(productId)
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build()
                }
            )
            .build()

        billingClient?.queryProductDetailsAsync(subsParams) { subsResult, subsDetails ->
            val subs = if (subsResult.responseCode == BillingClient.BillingResponseCode.OK) {
                logMissingProducts(listOf(PRODUCT_MONTHLY, PRODUCT_YEARLY), subsDetails)
                subsDetails
            } else {
                Timber.w("Subs product query failed (${subsResult.responseCode}): ${subsResult.debugMessage}")
                emptyList()
            }

            val inAppParams = QueryProductDetailsParams.newBuilder()
                .setProductList(
                    listOf(
                        QueryProductDetailsParams.Product.newBuilder()
                            .setProductId(PRODUCT_LIFETIME)
                            .setProductType(BillingClient.ProductType.INAPP)
                            .build()
                    )
                )
                .build()

            billingClient?.queryProductDetailsAsync(inAppParams) { inAppResult, inAppDetails ->
                val inApp = if (inAppResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    logMissingProducts(listOf(PRODUCT_LIFETIME), inAppDetails)
                    inAppDetails
                } else {
                    Timber.w("In-app product query failed (${inAppResult.responseCode}): ${inAppResult.debugMessage}")
                    emptyList()
                }

                val merged = subs + inApp
                // Keep any previously loaded catalog on a fully failed refresh
                // rather than blanking the paywall.
                if (merged.isNotEmpty()) {
                    _productDetails.value = merged
                }
                Timber.d("Products loaded: ${merged.size} (subs=${subs.size}, inapp=${inApp.size})")
            }
        }
    }

    /** Billing 7 has no unfetched-product callback — diff requested vs returned. */
    private fun logMissingProducts(requested: List<String>, returned: List<ProductDetails>) {
        val returnedIds = returned.map { it.productId }.toSet()
        val missing = requested.filterNot { it in returnedIds }
        if (missing.isNotEmpty()) {
            Timber.w("Products not returned by Play (not created/active in Console?): $missing")
        }
    }

    /**
     * Reconciles local entitlement against Play. [onComplete] is optional and
     * only used by [restorePurchases] to report a user-facing outcome — the
     * automatic reconcile-on-connect call site passes nothing, preserving its
     * existing silent behavior exactly.
     *
     * @param onComplete (found, queryFailed) — found is true if an active
     *   entitlement was confirmed; queryFailed is true if we couldn't get a
     *   trustworthy answer (a query errored), distinct from "queried fine,
     *   genuinely nothing there".
     */
    private fun queryExistingPurchases(onComplete: ((found: Boolean, queryFailed: Boolean) -> Unit)? = null) {
        val client = billingClient
        if (client == null || !client.isReady) {
            onComplete?.invoke(false, true)
            return
        }

        val subsParams = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()

        client.queryPurchasesAsync(subsParams) { billingResult, purchases ->
            val subsQueryOk = billingResult.responseCode == BillingClient.BillingResponseCode.OK
            if (subsQueryOk) {
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
                    onComplete?.invoke(true, false)
                    return@queryPurchasesAsync
                }
            } else {
                Timber.w("Subs purchase query failed (${billingResult.responseCode}): ${billingResult.debugMessage}")
            }

            val inAppParams = QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build()

            billingClient?.queryPurchasesAsync(inAppParams) { inAppResult, inAppPurchases ->
                if (inAppResult.responseCode != BillingClient.BillingResponseCode.OK) {
                    // Query failed — keep the cached entitlement; a transient
                    // Play error must never strip Pro from a paying user.
                    Timber.w("In-app purchase query failed (${inAppResult.responseCode}): ${inAppResult.debugMessage}; keeping cached entitlement")
                    onComplete?.invoke(false, true)
                    return@queryPurchasesAsync
                }

                inAppPurchases.filter { it.purchaseState == Purchase.PurchaseState.PURCHASED }
                    .forEach { acknowledgePurchase(it) }
                val hasLifetime = inAppPurchases.any {
                    it.products.contains(PRODUCT_LIFETIME) &&
                        it.purchaseState == Purchase.PurchaseState.PURCHASED
                }
                when {
                    hasLifetime -> {
                        setProEntitlement(true)
                        onComplete?.invoke(true, false)
                    }
                    // Downgrade only on complete evidence: both queries
                    // succeeded and neither found an entitlement.
                    subsQueryOk -> {
                        setProEntitlement(false)
                        onComplete?.invoke(false, false)
                    }
                    else -> {
                        Timber.w("Skipping entitlement downgrade: subs query failed; keeping cached entitlement")
                        onComplete?.invoke(false, true)
                    }
                }
            }
        }
    }

    /**
     * Public restore entry point — re-queries Play and reports a user-facing
     * [RestoreState] outcome. Reuses [queryExistingPurchases] so restore and
     * the silent startup reconcile share one entitlement/downgrade-safety
     * code path.
     */
    fun restorePurchases() {
        val client = billingClient
        if (client == null || !client.isReady) {
            _restoreState.value = RestoreState.Error(R.string.billing_error_no_connection)
            return
        }
        _restoreState.value = RestoreState.InProgress
        queryExistingPurchases { found, queryFailed ->
            _restoreState.value = when {
                queryFailed -> RestoreState.Error(R.string.restore_error)
                found -> RestoreState.Success
                else -> RestoreState.NotFound
            }
        }
    }

    fun resetRestoreState() {
        _restoreState.value = RestoreState.Idle
    }

    fun launchPurchase(activity: Activity, productDetails: ProductDetails, offerToken: String? = null) {
        val client = billingClient
        if (client == null || !client.isReady) {
            // Never leave the UI at Processing with no way out.
            _purchaseState.value = PurchaseState.Error(R.string.billing_error_no_connection)
            return
        }

        val productDetailsParamsBuilder = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(productDetails)

        if (offerToken != null) {
            productDetailsParamsBuilder.setOfferToken(offerToken)
        }

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productDetailsParamsBuilder.build()))
            .build()

        _purchaseState.value = PurchaseState.Processing

        val launchResult = client.launchBillingFlow(activity, billingFlowParams)
        if (launchResult.responseCode != BillingClient.BillingResponseCode.OK) {
            Timber.w("launchBillingFlow failed immediately (${launchResult.responseCode}): ${launchResult.debugMessage}")
            _purchaseState.value = if (launchResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
                PurchaseState.Cancelled
            } else {
                PurchaseState.Error(launchResult.toErrorRes())
            }
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                purchases?.forEach { purchase ->
                    when (purchase.purchaseState) {
                        Purchase.PurchaseState.PURCHASED -> {
                            acknowledgePurchase(purchase)
                            setProEntitlement(true)
                            _purchaseState.value = PurchaseState.Success

                            analyticsHelper.logEvent(
                                AnalyticsEvents.PURCHASE_SUCCESS,
                                mapOf(AnalyticsParams.PRODUCT_ID to purchase.products.joinToString())
                            )
                        }
                        Purchase.PurchaseState.PENDING -> {
                            // Do not grant Pro and do not acknowledge — Play
                            // requires acknowledgment only after the purchase
                            // transitions to PURCHASED, which the next
                            // queryExistingPurchases() reconcile will pick up.
                            _purchaseState.value = PurchaseState.Pending
                        }
                        else -> Unit // UNSPECIFIED_STATE — nothing to do
                    }
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                _purchaseState.value = PurchaseState.Cancelled
            }
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                // User already owns this (e.g. reinstall before reconcile) —
                // that's an entitlement to restore, not an error to show.
                Timber.i("Purchase reported already-owned; re-querying entitlements")
                queryExistingPurchases()
                _purchaseState.value = PurchaseState.Success
            }
            else -> {
                _purchaseState.value = PurchaseState.Error(billingResult.toErrorRes())
                analyticsHelper.logEvent(
                    AnalyticsEvents.PURCHASE_FAILED,
                    mapOf(AnalyticsParams.ERROR_MESSAGE to billingResult.debugMessage)
                )
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
                } else {
                    // Not fatal: queryExistingPurchases() re-acks on every
                    // connect, well inside Play's 3-day refund window.
                    Timber.w("Acknowledge failed (${billingResult.responseCode}): ${billingResult.debugMessage}; will retry on next purchase query")
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

/**
 * Maps a Billing response code to a stable, localized string resource.
 * BillingResult.debugMessage is a developer diagnostic string, never shown
 * to users — only logged.
 */
@StringRes
private fun BillingResult.toErrorRes(): Int = when (responseCode) {
    BillingClient.BillingResponseCode.SERVICE_DISCONNECTED,
    BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE,
    BillingClient.BillingResponseCode.SERVICE_TIMEOUT -> R.string.billing_error_no_connection
    BillingClient.BillingResponseCode.BILLING_UNAVAILABLE,
    BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED -> R.string.billing_error_unavailable
    BillingClient.BillingResponseCode.ITEM_UNAVAILABLE -> R.string.billing_error_item_unavailable
    BillingClient.BillingResponseCode.NETWORK_ERROR -> R.string.billing_error_network
    else -> R.string.billing_error_generic
}

sealed class PurchaseState {
    object Idle : PurchaseState()
    object Processing : PurchaseState()
    object Success : PurchaseState()
    object Cancelled : PurchaseState()
    object Pending : PurchaseState()
    data class Error(@StringRes val messageRes: Int) : PurchaseState()
}

sealed class RestoreState {
    object Idle : RestoreState()
    object InProgress : RestoreState()
    object Success : RestoreState()
    object NotFound : RestoreState()
    data class Error(@StringRes val messageRes: Int) : RestoreState()
}
