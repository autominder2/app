package com.autominder.app.ui.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Settings
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
// Garage: Vehicle management mental model.
// Activity: Chronological history mental model.
private val bottomNavItems = listOf(
    BottomNavItem(R.string.nav_home, Icons.Rounded.Dashboard, NavRoutes.Dashboard),
    BottomNavItem(R.string.nav_garage, Icons.Rounded.DirectionsCar, NavRoutes.VehicleList),
    BottomNavItem(R.string.nav_activity, Icons.Rounded.History, NavRoutes.ServiceHistory),
    BottomNavItem(R.string.nav_settings, Icons.Rounded.Settings, NavRoutes.Settings())
)

@Composable
fun BottomNavBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val haptic = LocalHapticFeedback.current

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = NavigationBarDefaults.Elevation,
        // Consume NO system inset here. NavigationBar's default
        // (NavigationBarDefaults.windowInsets) pads its own bottom for the
        // gesture bar, which is correct only when nothing sits beneath it. In
        // MainActivity a banner ad is stacked below this bar for non-Pro users,
        // so that padding landed BETWEEN the tabs and the ad while the ad —
        // the thing actually touching the screen edge — got no inset at all.
        // The wrapping Column owns navigationBarsPadding() instead, so the
        // inset always ends up under whatever is genuinely last. This is the
        // bar's only call site (MainActivity), so zeroing it here is safe.
        windowInsets = WindowInsets(0)
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
