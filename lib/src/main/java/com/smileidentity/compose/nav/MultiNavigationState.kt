package com.smileidentity.compose.nav

import androidx.annotation.RestrictTo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

@Composable
fun rememberMultiNavigationAppState(
    startDestination: Routes,
    navController: NavHostController = rememberNavController(),
) = remember(navController, startDestination) {
    MultiNavigationAppState(navController, startDestination)
}

class MultiNavigationAppState(
    private var navController: NavHostController? = null,
    startDestination: Routes? = null,
) {
    fun setNavController(navController: NavHostController) {
        this.navController = navController
    }

    var getStartDestination: Routes = startDestination!!
        private set

    var getNavController: NavHostController = navController!!
        get() {
            return navController!!
        }
        private set

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    fun navigateTo(route: Routes) {
        getNavController.navigate(route) {
            launchSingleTop = true
            restoreState = true
        }
    }
}

@Composable
fun NavHostController.getDestination(): Sequence<NavDestination>? {
    val navBackStackEntry by this.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    return currentDestination?.hierarchy
}

data class MultiNavigationStates(
    var rootNavigation: MultiNavigationAppState = MultiNavigationAppState(),
    var orchestratedNavigation: MultiNavigationAppState = MultiNavigationAppState(),
    var screensNavigation: MultiNavigationAppState = MultiNavigationAppState(),
)

lateinit var localNavigationState: MultiNavigationStates
