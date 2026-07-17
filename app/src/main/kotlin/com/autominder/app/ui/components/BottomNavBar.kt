package com.autominder.app.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
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

    NavigationBar {
        bottomNavItems.forEach { item ->
            val selected = currentDestination?.hasRoute(item.route::class) == true
            val label = stringResource(item.labelRes)

            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(NavRoutes.Dashboard) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = label
                    )
                },
                label = { Text(text = label) }
            )
        }
    }
}
