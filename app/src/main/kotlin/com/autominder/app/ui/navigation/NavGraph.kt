package com.autominder.app.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.autominder.app.ui.theme.Motion
import com.autominder.app.ui.screens.about.AboutScreen
import com.autominder.app.ui.screens.dashboard.DashboardScreen
import com.autominder.app.ui.screens.mileage.MileageLogScreen
import com.autominder.app.ui.screens.fuel.AddFuelScreen
import com.autominder.app.ui.screens.fuel.FuelHistoryScreen
import com.autominder.app.ui.screens.onboarding.OnboardingScreen
import com.autominder.app.ui.screens.reminder.AddReminderScreen
import com.autominder.app.ui.screens.reminder.EditReminderScreen
import com.autominder.app.ui.screens.service.AddServiceScreen
import com.autominder.app.ui.screens.service.ServiceDetailScreen
import com.autominder.app.ui.screens.service.ServiceHistoryScreen
import com.autominder.app.ui.screens.settings.SettingsScreen
import com.autominder.app.ui.screens.vehicle.AddVehicleScreen
import com.autominder.app.ui.screens.vehicle.EditVehicleScreen
import com.autominder.app.ui.screens.vehicle.VehicleDetailScreen
import com.autominder.app.ui.screens.vehicle.VehicleListScreen

// Forward navigation slides the new screen in from the right while the old one
// recedes slightly; popping reverses it with a subtle scale-down so depth reads
// correctly during predictive-back gestures.
// When "Remove animations" is on, every destination cross-fades instantly
// instead of sliding — no horizontal travel, no scale.
private val forwardEnter: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
    if (Motion.reduceMotion) fadeIn(tween(0))
    else slideInHorizontally(
        animationSpec = tween(Motion.DurationMedium, easing = Motion.EmphasizedDecelerate)
    ) { it / 4 } + fadeIn(tween(Motion.DurationMedium))
}

private val forwardExit: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
    if (Motion.reduceMotion) fadeOut(tween(0))
    else slideOutHorizontally(
        animationSpec = tween(Motion.DurationMedium, easing = Motion.EmphasizedAccelerate)
    ) { -it / 8 } + fadeOut(tween(Motion.DurationShort))
}

private val popEnter: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
    if (Motion.reduceMotion) fadeIn(tween(0))
    else slideInHorizontally(
        animationSpec = tween(Motion.DurationMedium, easing = Motion.EmphasizedDecelerate)
    ) { -it / 8 } + fadeIn(tween(Motion.DurationMedium))
}

private val popExit: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
    if (Motion.reduceMotion) fadeOut(tween(0))
    else slideOutHorizontally(
        animationSpec = tween(Motion.DurationMedium, easing = Motion.EmphasizedAccelerate)
    ) { it / 4 } + scaleOut(targetScale = 0.96f, animationSpec = tween(Motion.DurationMedium)) +
        fadeOut(tween(Motion.DurationMedium))
}

// Switching bottom-nav tabs is a lateral move, not a push — pure crossfade.
private val tabEnter: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
    fadeIn(tween(Motion.DurationShort))
}

private val tabExit: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
    fadeOut(tween(Motion.DurationShort))
}

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = NavRoutes.Dashboard,
        modifier = modifier,
        enterTransition = forwardEnter,
        exitTransition = forwardExit,
        popEnterTransition = popEnter,
        popExitTransition = popExit
    ) {
        // ── Bottom Nav Tabs ────────────────────────────────────────────────
        composable<NavRoutes.Dashboard>(
            enterTransition = tabEnter,
            exitTransition = tabExit,
            popEnterTransition = tabEnter,
            popExitTransition = tabExit
        ) {
            DashboardScreen(
                onNavigateToVehicleDetail = { vehicleId ->
                    navController.navigate(NavRoutes.VehicleDetail(vehicleId))
                },
                onNavigateToAddVehicle = {
                    navController.navigate(NavRoutes.AddVehicle)
                },
                onNavigateToAddService = { vehicleId ->
                    navController.navigate(NavRoutes.AddService(vehicleId))
                },
                onNavigateToAddFuel = { vehicleId ->
                    navController.navigate(NavRoutes.AddFuel(vehicleId))
                }
            )
        }

        composable<NavRoutes.VehicleList>(
            enterTransition = tabEnter,
            exitTransition = tabExit,
            popEnterTransition = tabEnter,
            popExitTransition = tabExit
        ) {
            VehicleListScreen(
                onNavigateToVehicleDetail = { vehicleId ->
                    navController.navigate(NavRoutes.VehicleDetail(vehicleId))
                },
                onNavigateToAddVehicle = {
                    navController.navigate(NavRoutes.AddVehicle)
                }
            )
        }

        composable<NavRoutes.ServiceHistory>(
            enterTransition = tabEnter,
            exitTransition = tabExit,
            popEnterTransition = tabEnter,
            popExitTransition = tabExit
        ) {
            ServiceHistoryScreen(
                onNavigateToServiceDetail = { serviceId ->
                    navController.navigate(NavRoutes.ServiceDetail(serviceId))
                }
            )
        }

        composable<NavRoutes.Settings>(
            enterTransition = tabEnter,
            exitTransition = tabExit,
            popEnterTransition = tabEnter,
            popExitTransition = tabExit
        ) {
            SettingsScreen(
                onNavigateToAbout = {
                    navController.navigate(NavRoutes.About)
                }
            )
        }

        // ── Vehicle Flow ───────────────────────────────────────────────────
        composable<NavRoutes.AddVehicle> {
            AddVehicleScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<NavRoutes.VehicleDetail> {
            VehicleDetailScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddReminder = { vehicleId ->
                    navController.navigate(NavRoutes.AddReminder(vehicleId))
                },
                onNavigateToEditVehicle = { vehicleId ->
                    navController.navigate(NavRoutes.EditVehicle(vehicleId))
                },
                onNavigateToAddService = { vehicleId ->
                    navController.navigate(NavRoutes.AddService(vehicleId))
                },
                onNavigateToMileageLog = { vehicleId ->
                    navController.navigate(NavRoutes.MileageLog(vehicleId))
                },
                onNavigateToEditReminder = { reminderId ->
                    navController.navigate(NavRoutes.EditReminder(reminderId))
                },
                onNavigateToAddFuel = { vehicleId ->
                    navController.navigate(NavRoutes.AddFuel(vehicleId))
                },
                onNavigateToFuelHistory = { vehicleId ->
                    navController.navigate(NavRoutes.FuelHistory(vehicleId))
                }
            )
        }

        composable<NavRoutes.EditVehicle> {
            EditVehicleScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ── Reminder Flow ──────────────────────────────────────────────────
        composable<NavRoutes.AddReminder> {
            AddReminderScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<NavRoutes.EditReminder> {
            EditReminderScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ── Service Flow ───────────────────────────────────────────────────
        composable<NavRoutes.ServiceDetail> {
            ServiceDetailScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<NavRoutes.AddService> {
            AddServiceScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ── Mileage Log ────────────────────────────────────────────────────
        composable<NavRoutes.MileageLog> {
            MileageLogScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ── Fuel Log ────────────────────────────────────────────────────
        composable<NavRoutes.AddFuel> {
            AddFuelScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<NavRoutes.FuelHistory> {
            FuelHistoryScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ── Utility ────────────────────────────────────────────────────────
        composable<NavRoutes.About> {
            AboutScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<NavRoutes.Onboarding> {
            OnboardingScreen(
                onFinished = {
                    navController.navigate(NavRoutes.Dashboard) {
                        popUpTo(NavRoutes.Onboarding) { inclusive = true }
                    }
                }
            )
        }
    }
}
