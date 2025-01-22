package com.smileidentity.compose.selfie.navigation

import com.ramcosta.composedestinations.annotation.NavHostGraph
import com.ramcosta.composedestinations.annotation.parameters.CodeGenVisibility

@NavHostGraph(
    route = "selfie_route",
    visibility = CodeGenVisibility.INTERNAL,
)
internal annotation class SelfieGraph

internal interface SelfieGraphNavigation {
    fun navigateToSmileSmartSelfieInstructionsScreen(showAttribution: Boolean)
    fun navigateToSmileSelfieCaptureScreen()
    fun navigateToSmileProcessingScreen()
}
