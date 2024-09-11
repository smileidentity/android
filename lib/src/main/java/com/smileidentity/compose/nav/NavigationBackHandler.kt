package com.smileidentity.compose.nav

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun NavigationBackHandler(
    navController: NavController,
    enabled: Boolean = true,
    onBack: (currentDestination: NavDestination?) -> Unit,
) {
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = currentBackStackEntry?.destination
    BackHandler(enabled = enabled) {
        onBack(currentDestination)
    }
}
