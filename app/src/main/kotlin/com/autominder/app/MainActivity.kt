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
import com.autominder.app.ads.AdManager
import com.autominder.app.ads.BannerAdView
import com.autominder.app.billing.SubscriptionManager
import com.autominder.app.core.util.UpdateHelper
import com.autominder.app.data.local.preferences.UserPreferences
import com.autominder.app.ui.components.BottomNavBar
import com.autominder.app.ui.navigation.NavGraph
import com.autominder.app.ui.navigation.NavRoutes
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import com.autominder.app.ui.theme.AutoMinderTheme
import com.autominder.app.ui.theme.LocalDistanceUnit
import com.autominder.app.ui.components.LocalSnackbarHostState
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

val LocalIsProUser = staticCompositionLocalOf { false }

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var adManager: AdManager

    @Inject
    lateinit var consentManager: com.autominder.app.ads.ConsentManager

    @Inject
    lateinit var userPreferences: UserPreferences

    @Inject
    lateinit var subscriptionManager: SubscriptionManager

    @Inject
    lateinit var updateHelper: UpdateHelper

    private val _deepLinkEvents = kotlinx.coroutines.flow.MutableSharedFlow<Long>(extraBufferCapacity = 1)

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: android.content.Intent?) {
        val vehicleId = intent?.getLongExtra("vehicleId", -1L) ?: -1L
        if (vehicleId > 0L) {
            _deepLinkEvents.tryEmit(vehicleId)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleIntent(intent)

        // GDPR consent gate — initializes Mobile Ads only once consent allows
        consentManager.gatherConsentAndInitAds(this)

        // Check for updates on startup
        updateHelper.checkForUpdates(this)

        setContent {
            val themeMode by userPreferences.themeMode.collectAsStateWithLifecycle(initialValue = "system")
            val hasSeenOnboarding by userPreferences.hasSeenOnboarding.collectAsStateWithLifecycle(initialValue = true)
            val distanceUnit by userPreferences.distanceUnit.collectAsStateWithLifecycle(initialValue = "km")
            val isProUser by subscriptionManager.isProUser.collectAsStateWithLifecycle()
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

                // Onboarding: navigate if user hasn't completed it yet
                LaunchedEffect(hasSeenOnboarding) {
                    if (!hasSeenOnboarding) {
                        navController.navigate(NavRoutes.Onboarding) {
                            launchSingleTop = true
                        }
                    }
                }

                // Notification deep link: open vehicle detail
                LaunchedEffect(hasSeenOnboarding) {
                    if (hasSeenOnboarding) {
                        _deepLinkEvents.collect { vehicleId ->
                            navController.navigate(NavRoutes.VehicleDetail(vehicleId)) {
                                launchSingleTop = true
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
                            Column {
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
                    NavGraph(
                        navController = navController,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
                } // End LocalSnackbarHostState Provider
            }
            } // End LocalDistanceUnit Provider
        }
    }
}
