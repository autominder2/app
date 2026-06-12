package com.autominder.app.ads

import android.app.Activity
import com.google.android.gms.ads.MobileAds
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Google UMP consent gate. EEA/UK users must give GDPR consent before any
 * ad request — Mobile Ads is only initialized once `canRequestAds()` says so.
 * Outside consent regions the form never shows and ads start immediately.
 */
@Singleton
class ConsentManager @Inject constructor(
    private val adManager: AdManager
) {

    private val isMobileAdsInitialized = AtomicBoolean(false)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun gatherConsentAndInitAds(activity: Activity) {
        val consentInformation: ConsentInformation =
            UserMessagingPlatform.getConsentInformation(activity)

        val params = ConsentRequestParameters.Builder()
            .setTagForUnderAgeOfConsent(false)
            .build()

        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
                    if (formError != null) {
                        Timber.w("Consent form error: ${formError.errorCode} ${formError.message}")
                    }
                    if (consentInformation.canRequestAds()) {
                        initializeMobileAds(activity)
                    }
                }
            },
            { requestError ->
                Timber.w("Consent info update failed: ${requestError.errorCode} ${requestError.message}")
                // Offline or transient failure — fall back to the stored
                // consent state from a previous session if it allows ads.
                if (consentInformation.canRequestAds()) {
                    initializeMobileAds(activity)
                }
            }
        )
    }

    private fun initializeMobileAds(activity: Activity) {
        if (isMobileAdsInitialized.getAndSet(true)) return
        val appContext = activity.applicationContext
        scope.launch {
            MobileAds.initialize(appContext) { initStatus ->
                Timber.d("MobileAds init: ${initStatus.adapterStatusMap}")
                adManager.preloadAds()
            }
        }
    }
}
