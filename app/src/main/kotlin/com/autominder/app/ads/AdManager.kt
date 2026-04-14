package com.autominder.app.ads

import android.app.Activity
import android.content.Context
import com.autominder.app.BuildConfig
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Centralized AdMob manager.
 * - Ads are only loaded/shown when [BuildConfig.ENABLE_ADS] is true
 * - Interstitial and Rewarded ads are preloaded and reloaded automatically after showing
 * - Banner ads are managed directly in Compose via [BannerAdView]
 */
@Singleton
class AdManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var interstitialAd: InterstitialAd? = null
    private var rewardedAd: RewardedAd? = null
    private var rewardedInterstitialAd: RewardedInterstitialAd? = null

    private val interstitialAdUnitId: String
        get() = context.getString(com.autominder.app.R.string.admob_interstitial_id)

    private val rewardedAdUnitId: String
        get() = context.getString(com.autominder.app.R.string.admob_rewarded_id)

    private val rewardedInterstitialAdUnitId: String
        get() = context.getString(com.autominder.app.R.string.admob_rewarded_interstitial_id)

    private var interstitialShowCount = 0
    private var userActionCount = 0
    private val AD_SHOW_RATIO = 3 // Show ad every 3rd action

    fun shouldShowInterstitial(): Boolean {
        if (!BuildConfig.ENABLE_ADS) return false
        
        userActionCount++
        
        // Never on first action
        if (userActionCount == 1) return false
        
        // Show every 3rd action
        val shouldShow = userActionCount % AD_SHOW_RATIO == 0
        
        if (shouldShow) {
            interstitialShowCount++
        }
        
        return shouldShow
    }

    /** Call once after MobileAds.initialize() completes */
    fun preloadAds() {
        if (!BuildConfig.ENABLE_ADS) return
        loadInterstitial()
        loadRewarded()
        loadRewardedInterstitial()
    }

    // ─── Interstitial ────────────────────────────────────────────────────────

    private fun loadInterstitial() {
        InterstitialAd.load(
            context,
            interstitialAdUnitId,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    Timber.d("Interstitial loaded")
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                    Timber.w("Interstitial failed: ${error.message}")
                }
            }
        )
    }

    /**
     * Show the interstitial on natural transition points (e.g., after adding a vehicle).
     * Reloads automatically after dismissal.
     */
    fun showInterstitial(activity: Activity, onDismissed: () -> Unit = {}) {
        if (!BuildConfig.ENABLE_ADS || interstitialAd == null) {
            onDismissed()
            return
        }
        interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                interstitialAd = null
                loadInterstitial()
                onDismissed()
            }
            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                interstitialAd = null
                loadInterstitial()
                onDismissed()
            }
        }
        interstitialAd?.show(activity)
    }

    // ─── Rewarded ────────────────────────────────────────────────────────────

    private fun loadRewarded() {
        RewardedAd.load(
            context,
            rewardedAdUnitId,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    Timber.d("RewardedAd loaded")
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewardedAd = null
                    Timber.w("RewardedAd failed: ${error.message}")
                }
            }
        )
    }

    /**
     * Show a rewarded ad (e.g., unlock extra reminder slots or vehicle slots).
     * [onRewarded] called only when user earns the reward.
     */
    fun showRewarded(activity: Activity, onRewarded: () -> Unit, onDismissed: () -> Unit = {}) {
        if (!BuildConfig.ENABLE_ADS || rewardedAd == null) {
            onDismissed()
            return
        }
        rewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                rewardedAd = null
                loadRewarded()
                onDismissed()
            }
            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                rewardedAd = null
                loadRewarded()
                onDismissed()
            }
        }
        rewardedAd?.show(activity) { rewardItem ->
            Timber.d("User earned reward: ${rewardItem.amount} ${rewardItem.type}")
            onRewarded()
        }
    }

    // ─── Rewarded Interstitial ────────────────────────────────────────────────

    private fun loadRewardedInterstitial() {
        RewardedInterstitialAd.load(
            context,
            rewardedInterstitialAdUnitId,
            AdRequest.Builder().build(),
            object : RewardedInterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedInterstitialAd) {
                    rewardedInterstitialAd = ad
                    Timber.d("RewardedInterstitialAd loaded")
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewardedInterstitialAd = null
                    Timber.w("RewardedInterstitialAd failed: ${error.message}")
                }
            }
        )
    }

    /** Show on session milestones (e.g., after completing 5 reminders) */
    fun showRewardedInterstitial(activity: Activity, onRewarded: () -> Unit, onDismissed: () -> Unit = {}) {
        if (!BuildConfig.ENABLE_ADS || rewardedInterstitialAd == null) {
            onDismissed()
            return
        }
        rewardedInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                rewardedInterstitialAd = null
                loadRewardedInterstitial()
                onDismissed()
            }
            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                rewardedInterstitialAd = null
                loadRewardedInterstitial()
                onDismissed()
            }
        }
        rewardedInterstitialAd?.show(activity) { rewardItem ->
            Timber.d("RewardedInterstitial reward: ${rewardItem.amount} ${rewardItem.type}")
            onRewarded()
        }
    }
}
