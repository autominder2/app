package com.autominder.app.ads

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.autominder.app.BuildConfig
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

@Composable
fun BannerAdView(
    adUnitId: String,
    modifier: Modifier = Modifier
) {
    if (!BuildConfig.ENABLE_ADS) return

    val screenWidthDp = LocalConfiguration.current.screenWidthDp

    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { context ->
            val adaptiveSize = AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(
                context,
                screenWidthDp
            )
            AdView(context).apply {
                setAdSize(adaptiveSize)
                setAdUnitId(adUnitId)
                loadAd(AdRequest.Builder().build())
            }
        },
        update = { /* ad loaded once in factory */ }
    )
}
