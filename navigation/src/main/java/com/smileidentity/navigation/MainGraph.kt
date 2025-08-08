package com.smileidentity.navigation

import com.ramcosta.composedestinations.annotation.ExternalNavGraph
import com.ramcosta.composedestinations.annotation.NavHostGraph
import com.ramcosta.composedestinations.navigation.DependenciesContainerBuilder
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.utils.toDestinationsNavigator
import com.smileidentity.navigation.graphs.BVNGraph
import com.smileidentity.navigation.graphs.BiometricGraph
import com.smileidentity.navigation.graphs.DocumentVerificationGraph
import com.smileidentity.navigation.graphs.EnhancedDocumentVerificationGraph
import com.smileidentity.navigation.graphs.EnhancedKycGraph
import com.smileidentity.navigation.graphs.SmartSelfieAuthenticationGraph
import com.smileidentity.navigation.graphs.SmartSelfieEnrollmentGraph

@NavHostGraph
annotation class MainGraph {
    @ExternalNavGraph<BiometricGraph>
    @ExternalNavGraph<BVNGraph>
    @ExternalNavGraph<DocumentVerificationGraph>
    @ExternalNavGraph<EnhancedDocumentVerificationGraph>
    @ExternalNavGraph<EnhancedKycGraph>
    @ExternalNavGraph<SmartSelfieAuthenticationGraph>
    @ExternalNavGraph<SmartSelfieEnrollmentGraph>
    companion object Includes
}

class CoreFeatureNavigatorSettings(private val navigator: DestinationsNavigator)

fun DependenciesContainerBuilder<*>.currentNavigator() =
    CoreFeatureNavigatorSettings(navigator = navController.toDestinationsNavigator())
