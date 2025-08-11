package com.smileidentity.navigation.graph

import com.ramcosta.composedestinations.annotation.NavHostGraph

@NavHostGraph
annotation class MainGraph {
    companion object Includes
}

// class CoreFeatureNavigatorSettings(val navigator: DestinationsNavigator) : SmileIDGraphNavigation {
//    override fun navigateToInstructionsScreen() =
//        navigator.navigate(direction = OrchestratedInstructionsScreenDestination)
//
//    override fun navigateToCaptureScreen() =
//        navigator.navigate(direction = OrchestratedCaptureScreenDestination)
//
//    override fun navigateToProcessingScreen() =
//        navigator.navigate(direction = OrchestratedProcessingScreenDestination)
// }

// fun DependenciesContainerBuilder<*>.currentNavigator() =
//    CoreFeatureNavigatorSettings(navigator = navController.toDestinationsNavigator())
