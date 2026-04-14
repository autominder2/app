package com.autominder.app.ads

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.autominder.app.BuildConfig
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

/**
 * Adaptive banner ad for Compose.
 * Only renders when ENABLE_ADS = true (release build).
 * Placement: bottom of Dashboard and VehicleDetail screens.
 */
@Composable
fun BannerAdView(
    adUnitId: String,
    modifier: Modifier = Modifier
) {
    if (!BuildConfig.ENABLE_ADS) return

    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp),
        factory = { context ->
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                setAdUnitId(adUnitId)
                loadAd(AdRequest.Builder().build())
            }
        },
        update = { /* no-op: ad is loaded once in factory */ }
    )
}
