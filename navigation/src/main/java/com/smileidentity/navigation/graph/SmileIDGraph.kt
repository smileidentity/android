package com.smileidentity.navigation.graph

import com.ramcosta.composedestinations.annotation.ExternalModuleGraph
import com.ramcosta.composedestinations.annotation.NavGraph

@NavGraph<ExternalModuleGraph>
annotation class SmileIDGraph

interface SmileIDGraphNavigation {

    fun navigateToInstructionsScreen()

    fun navigateToCaptureScreen()

    fun navigateToProcessingScreen()
}
