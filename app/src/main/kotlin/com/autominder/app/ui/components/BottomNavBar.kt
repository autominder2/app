package com.autominder.app.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.autominder.app.R
import com.autominder.app.ui.navigation.NavRoutes

private data class BottomNavItem(
    @androidx.annotation.StringRes val labelRes: Int,
    val icon: ImageVector,
    val route: NavRoutes
)

// 4 tabs, equal-importance top-level destinations (Material 3 supports 3-5).
// Records is cross-vehicle service history — confirmed a real top-level
// destination, not a per-vehicle detail screen.
private val bottomNavItems = listOf(
    BottomNavItem(R.string.nav_home, Icons.Default.Dashboard, NavRoutes.Dashboard),
    BottomNavItem(R.string.nav_vehicles, Icons.Default.DirectionsCar, NavRoutes.VehicleList),
    BottomNavItem(R.string.nav_records, Icons.Default.History, NavRoutes.ServiceHistory),
    BottomNavItem(R.string.nav_settings, Icons.Default.Settings, NavRoutes.Settings())
)

@Composable
fun BottomNavBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val haptic = LocalHapticFeedback.current

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = NavigationBarDefaults.Elevation
    ) {
        bottomNavItems.forEach { item ->
            val selected = currentDestination?.hasRoute(item.route::class) == true
            val label = stringResource(item.labelRes)

            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (!selected) {
                        haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                        navController.navigate(item.route) {
                            popUpTo(NavRoutes.Dashboard) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = label
                    )
                },
                label = {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}
