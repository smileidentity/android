package com.smileidentity.compose.nav

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
    private val startDestination: Routes? = null,
) {

    // fun observeNavController() {
    //     val navBackStackEntry = getNavController.currentBackStackEntryAsState()
    //     navBackStackEntry?.let {
    //         val currentDestination = navBackStackEntry?.destination
    //     }
    // }

    fun setNavController(_navController: NavHostController) {
        navController = _navController
        // observeNavController()
    }

    var getStartDestination: Routes = startDestination!!
        private set

    var getNavController: NavHostController = navController!!
        get() {
            return navController!!
        }
        private set

    fun setStartDestination(route: Routes) {
        getStartDestination = route
    }

    @Composable
    fun isRouteActive(route: String): Boolean {
        var navHostController = navController

        return if (navHostController != null) {
            val destination = navHostController.getDestination()
            return destination?.any {
                (it.route.equals(route))
            } ?: false
        } else {
            false
        }
    }

    fun navigateTo(route: Routes, popUpTo: Boolean, popUpToInclusive: Boolean) {
        getNavController.navigate(route) {
            launchSingleTop = true
            restoreState = true
            // if (popUpTo) {
            //     popUpTo(route) {
            //         inclusive = popUpToInclusive
            //     }
            // }
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
