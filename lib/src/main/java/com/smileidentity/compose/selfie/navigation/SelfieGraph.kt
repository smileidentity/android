package com.smileidentity.compose.selfie.navigation

import com.ramcosta.composedestinations.annotation.NavGraph
import com.ramcosta.composedestinations.annotation.RootGraph

@NavGraph<RootGraph>(start = true)
internal annotation class SelfieGraph

interface SelfieGraphNavigation {
    fun navigateToSmileSmartSelfieInstructionsScreen(showAttribution: Boolean)
    fun navigateToSmileSelfieCaptureScreen()
    fun navigateToSmileProcessingScreen()
}
