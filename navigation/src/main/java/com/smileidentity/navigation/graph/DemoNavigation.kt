package com.smileidentity.navigation.graph

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

@Composable
fun DemoNavigation(modifier: Modifier = Modifier) {
    val bottomSheetNavigator = rememberBottomSheetNavigator()
    val navController = rememberNavController()
    navController.navigatorProvider += bottomSheetNavigator

    DestinationsNavHost(
        navGraph = RootNavGraph,
        modifier = modifier.fillMaxSize(),
        navController = navController,
        defaultTransitions = DefaultFadingTransitions,
        start = OrchestratedInstructionsScreenDestination,
    )
}
