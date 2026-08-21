package com.autominder.app.ads

import android.app.Activity
import com.autominder.app.BuildConfig
import com.autominder.app.core.util.AnalyticsEvents
import com.autominder.app.core.util.AnalyticsHelper
import com.google.android.gms.ads.MobileAds
import com.google.android.ump.ConsentDebugSettings
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
 * ad request — Mobile Ads is only initialized once canRequestAds() says so.
 * Outside consent regions the form never shows and ads start immediately.
 */
@Singleton
class ConsentManager @Inject constructor(
    private val adManager: AdManager,
    private val analyticsHelper: AnalyticsHelper
) {

    private val isMobileAdsInitialized = AtomicBoolean(false)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun gatherConsentAndInitAds(activity: Activity) {
        val consentInformation: ConsentInformation =
            UserMessagingPlatform.getConsentInformation(activity)

        val paramsBuilder = ConsentRequestParameters.Builder()
            .setTagForUnderAgeOfConsent(false)

        if (BuildConfig.DEBUG) {
            val debugSettings = ConsentDebugSettings.Builder(activity)
                .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_NOT_EEA)
                .build()
            paramsBuilder.setConsentDebugSettings(debugSettings)
        }

        val params = paramsBuilder.build()

        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
                    if (formError != null) {
                        Timber.w("Consent form error:  ")
                    }
                    if (consentInformation.canRequestAds() || BuildConfig.DEBUG) {
                        analyticsHelper.logEvent(AnalyticsEvents.ADS_CONSENT_GIVEN)
                        initializeMobileAds(activity)
                    } else {
                        analyticsHelper.logEvent(AnalyticsEvents.ADS_CONSENT_DENIED)
                    }
                }
            },
            { requestError ->
                Timber.w("Consent info update failed:  ")
                if (consentInformation.canRequestAds() || BuildConfig.DEBUG) {
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
                Timber.d("MobileAds init: ")
                adManager.preloadAds()
            }
        }
    }
}
