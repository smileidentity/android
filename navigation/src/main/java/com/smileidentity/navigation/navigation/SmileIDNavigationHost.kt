package com.smileidentity.navigation.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.navigation.rememberBottomSheetNavigator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import androidx.navigation.plusAssign
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.animations.defaults.DefaultFadingTransitions
import com.ramcosta.composedestinations.generated.navigation.destinations.OrchestratedInstructionsScreenDestination
import com.ramcosta.composedestinations.generated.navigation.navgraphs.RootNavGraph
import com.ramcosta.composedestinations.navigation.dependency
import com.ramcosta.composedestinations.navigation.navGraph
import com.ramcosta.composedestinations.spec.Direction
import com.ramcosta.composedestinations.spec.DirectionNavHostGraphSpec
import com.smileidentity.navigation.viewmodel.IdentityViewModel

@Composable
fun SmileIDNavigationHost(
    modifier: Modifier = Modifier,
    startDestination: Direction = OrchestratedInstructionsScreenDestination,
    destinations: DirectionNavHostGraphSpec = RootNavGraph,
) {
    val bottomSheetNavigator = rememberBottomSheetNavigator()
    val navController = rememberNavController()
    navController.navigatorProvider += bottomSheetNavigator

    DestinationsNavHost(
        modifier = modifier.fillMaxSize(),
        navGraph = destinations,
        navController = navController,
        defaultTransitions = DefaultFadingTransitions,
        start = startDestination,
        dependenciesContainerBuilder = {
            navGraph(navGraph = RootNavGraph) {
                dependency(dependency = IdentityViewModel.IdentityViewModelFactory())
            }
        },
    )
}
