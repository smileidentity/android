package com.smileidentity.navigation

import com.ramcosta.composedestinations.annotation.ExternalNavGraph
import com.ramcosta.composedestinations.annotation.NavHostGraph
import com.ramcosta.composedestinations.navigation.DependenciesContainerBuilder
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.utils.toDestinationsNavigator
import com.smileidentity.navigation.graph.SmileIDGraph

@NavHostGraph
annotation class MainGraph {
    @ExternalNavGraph<SmileIDGraph>
    companion object Includes
}

class CoreFeatureNavigatorSettings(val navigator: DestinationsNavigator)

fun DependenciesContainerBuilder<*>.currentNavigator() =
    CoreFeatureNavigatorSettings(navigator = navController.toDestinationsNavigator())
