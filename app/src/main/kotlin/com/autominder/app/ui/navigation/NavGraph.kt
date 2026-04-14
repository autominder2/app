package com.autominder.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
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

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = NavRoutes.Dashboard,
        modifier = modifier
    ) {
        // ── Bottom Nav Tabs ────────────────────────────────────────────────
        composable<NavRoutes.Dashboard> {
            DashboardScreen(
                onNavigateToVehicleDetail = { vehicleId ->
                    navController.navigate(NavRoutes.VehicleDetail(vehicleId))
                },
                onNavigateToAddVehicle = {
                    navController.navigate(NavRoutes.AddVehicle)
                }
            )
        }

        composable<NavRoutes.VehicleList> {
            VehicleListScreen(
                onNavigateToVehicleDetail = { vehicleId ->
                    navController.navigate(NavRoutes.VehicleDetail(vehicleId))
                },
                onNavigateToAddVehicle = {
                    navController.navigate(NavRoutes.AddVehicle)
                }
            )
        }

        composable<NavRoutes.ServiceHistory> {
            ServiceHistoryScreen(
                onNavigateToServiceDetail = { serviceId ->
                    navController.navigate(NavRoutes.ServiceDetail(serviceId))
                }
            )
        }

        composable<NavRoutes.Settings> {
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
