package com.autominder.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.autominder.app.ads.BannerAdView
import com.autominder.app.billing.SubscriptionManager
import com.autominder.app.core.util.UpdateHelper
import com.autominder.app.core.notifications.NotificationHelper
import com.autominder.app.data.local.preferences.UserPreferences
import com.autominder.app.ui.components.BottomNavBar
import com.autominder.app.ui.navigation.NavGraph
import com.autominder.app.ui.navigation.NavRoutes
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.produceState
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.withFrameNanos
import com.autominder.app.ui.theme.AutoMinderTheme
import com.autominder.app.ui.theme.LocalDistanceUnit
import com.autominder.app.ui.components.LocalSnackbarHostState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject
import javax.inject.Provider

val LocalIsProUser = staticCompositionLocalOf { false }

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var consentManager: Provider<com.autominder.app.ads.ConsentManager>

    @Inject
    lateinit var userPreferences: UserPreferences

    @Inject
    lateinit var subscriptionManager: Provider<SubscriptionManager>

    @Inject
    lateinit var updateHelper: Provider<UpdateHelper>

    /** vehicleId to openMileageSheet — notification deep-link payload. */
    private val _deepLinkEvents = kotlinx.coroutines.channels.Channel<VehicleDeepLink>(
        capacity = kotlinx.coroutines.channels.Channel.BUFFERED
    )

    private data class VehicleDeepLink(
        val vehicleId: Long,
        val openMileageSheet: Boolean,
        val requestId: Long
    )

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: android.content.Intent?) {
        val vehicleId = intent?.getLongExtra(NotificationHelper.EXTRA_VEHICLE_ID, -1L) ?: -1L
        if (vehicleId > 0L) {
            val openMileage = intent?.getBooleanExtra(
                NotificationHelper.EXTRA_OPEN_MILEAGE_SHEET,
                false
            ) ?: false
            val requestId = intent?.getLongExtra(
                NotificationHelper.EXTRA_MILEAGE_REQUEST_ID,
                0L
            ) ?: 0L
            if (_deepLinkEvents.trySend(VehicleDeepLink(vehicleId, openMileage, requestId)).isSuccess) {
                intent?.removeExtra(NotificationHelper.EXTRA_VEHICLE_ID)
                intent?.removeExtra(NotificationHelper.EXTRA_OPEN_MILEAGE_SHEET)
                intent?.removeExtra(NotificationHelper.EXTRA_MILEAGE_REQUEST_ID)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleIntent(intent)

        // Honor the system "Remove animations" accessibility setting app-wide.
        com.autominder.app.ui.theme.Motion.reduceMotion = try {
            android.provider.Settings.Global.getFloat(
                contentResolver,
                android.provider.Settings.Global.ANIMATOR_DURATION_SCALE,
                1f
            ) == 0f
        } catch (e: Exception) {
            false
        }

        setContent {
            // One-shot: read once to pick NavGraph's startDestination so the
            // NavHost never composes at Dashboard and then jumps to
            // Onboarding. Do NOT make this reactive — NavHost only honors
            // startDestination at first composition, and a value that keeps
            // changing here would just be misleading, not functional.
            val onboardingResolved by produceState<Boolean?>(initialValue = null) {
                value = userPreferences.hasSeenOnboarding.first()
            }
            // Reactive: gates the deep-link collector below. Must track the
            // live value — a user can finish onboarding mid-session (e.g. a
            // notification deep link arrives right after onboarding
            // completes) and a frozen one-shot read would buffer that event
            // forever, until the next process launch.
            val hasSeenOnboarding by userPreferences.hasSeenOnboarding
                .collectAsStateWithLifecycle(initialValue = false)
            val themeMode by userPreferences.themeMode.collectAsStateWithLifecycle(initialValue = "system")
            val distanceUnit by userPreferences.distanceUnit.collectAsStateWithLifecycle(initialValue = "km")
            val isProUser by produceState(initialValue = false) {
                withFrameNanos { }
                subscriptionManager.get().isProUser.collect { value = it }
            }

            LaunchedEffect(Unit) {
                withFrameNanos { }
                delay(1_000)
                consentManager.get().gatherConsentAndInitAds(this@MainActivity)
                updateHelper.get().checkForUpdates(this@MainActivity)
            }
            val darkTheme = when (themeMode) {
                "light" -> false
                "dark" -> true
                else -> isSystemInDarkTheme()
            }
            AutoMinderTheme(darkTheme = darkTheme) {
            CompositionLocalProvider(
                LocalDistanceUnit provides distanceUnit,
                LocalIsProUser provides isProUser
            ) {
                val navController = rememberNavController()
                val bannerAdUnitId = remember {
                    getString(R.string.admob_banner_id)
                }
                val snackbarHostState = remember { SnackbarHostState() }

                CompositionLocalProvider(LocalSnackbarHostState provides snackbarHostState) {

                // Notification deep link: open vehicle detail; the Update
                // mileage action opens the QuickMileageSheet directly.
                // Gated on the reactive flow so a deep link that arrives the
                // moment onboarding completes is still delivered this
                // session, not buffered until next launch.
                LaunchedEffect(hasSeenOnboarding) {
                    if (hasSeenOnboarding) {
                        _deepLinkEvents.receiveAsFlow().collect { deepLink ->
                            val currentDestinationId = navController.currentDestination?.id
                            val isVehicleDetailOnTop = navController
                                .currentBackStackEntry
                                ?.destination
                                ?.route
                                ?.contains(
                                    NavRoutes.VehicleDetail::class.qualifiedName.orEmpty()
                                ) == true
                            navController.navigate(
                                NavRoutes.VehicleDetail(
                                    vehicleId = deepLink.vehicleId,
                                    openMileageSheet = deepLink.openMileageSheet,
                                    mileageRequestId = deepLink.requestId
                                )
                            ) {
                                if (isVehicleDetailOnTop && currentDestinationId != null) {
                                    popUpTo(currentDestinationId) { inclusive = true }
                                }
                            }
                        }
                    }
                }

                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val showBottomBar by remember {
                    derivedStateOf {
                        val route = navBackStackEntry?.destination?.route ?: ""
                        route.contains(NavRoutes.Dashboard::class.qualifiedName.orEmpty()) ||
                            route.contains(NavRoutes.VehicleList::class.qualifiedName.orEmpty()) ||
                            route.contains(NavRoutes.ServiceHistory::class.qualifiedName.orEmpty()) ||
                            route.contains(NavRoutes.Settings::class.qualifiedName.orEmpty())
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    bottomBar = {
                        AnimatedVisibility(
                            visible = showBottomBar,
                            enter = slideInVertically(initialOffsetY = { it }),
                            exit = slideOutVertically(targetOffsetY = { it })
                        ) {
                            // The Column owns the gesture inset, not the
                            // NavigationBar inside it — so the padding lands
                            // beneath the banner ad when one is shown, and
                            // beneath the tabs when it isn't.
                            Column(modifier = Modifier.navigationBarsPadding()) {
                                BottomNavBar(navController = navController)
                                if (!isProUser) {
                                    BannerAdView(
                                        adUnitId = bannerAdUnitId,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    onboardingResolved?.let { onboardingComplete ->
                        NavGraph(
                            navController = navController,
                            startDestination = if (onboardingComplete) {
                                NavRoutes.Dashboard
                            } else {
                                NavRoutes.Onboarding
                            },
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
                } // End LocalSnackbarHostState Provider
            }
            } // End LocalDistanceUnit Provider
        }
    }
}
