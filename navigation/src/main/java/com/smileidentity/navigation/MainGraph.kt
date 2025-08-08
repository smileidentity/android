package com.smileidentity.navigation

import com.ramcosta.composedestinations.annotation.ExternalNavGraph
import com.ramcosta.composedestinations.annotation.NavHostGraph
import com.ramcosta.composedestinations.navigation.DependenciesContainerBuilder
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.utils.toDestinationsNavigator
import com.smileidentity.navigation.graph.SmileIDGraph
import com.smileidentity.navigation.graph.SmileIDGraphNavigation
import com.smileidentity.navigation.ui.OrchestratedInstructionsScreen

@NavHostGraph
annotation class MainGraph {
    @ExternalNavGraph<SmileIDGraph>
    companion object Includes
}

class CoreFeatureNavigatorSettings(val navigator: DestinationsNavigator) : SmileIDGraphNavigation {
    override fun navigateToInstructionsScreen() =
        navigator.navigate(direction = OrchestratedInstructionsScreenDestination)

    override fun navigateToCaptureScreen() {
        TODO("Not yet implemented")
    }

    override fun navigateToProcessingScreen() {
        TODO("Not yet implemented")
    }
}

fun DependenciesContainerBuilder<*>.currentNavigator() =
    CoreFeatureNavigatorSettings(navigator = navController.toDestinationsNavigator())
