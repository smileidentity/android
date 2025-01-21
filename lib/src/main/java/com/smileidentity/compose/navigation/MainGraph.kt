package com.smileidentity.compose.navigation

import com.ramcosta.composedestinations.generated.destinations.SmileProcessingScreenDestination
import com.ramcosta.composedestinations.generated.destinations.SmileSelfieCaptureScreenDestination
import com.ramcosta.composedestinations.generated.destinations.SmileSmartSelfieInstructionsScreenDestination
import com.ramcosta.composedestinations.navigation.DependenciesContainerBuilder
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.utils.toDestinationsNavigator
import com.smileidentity.compose.selfie.navigation.SelfieGraphNavigation

class CoreFeatureNavigatorSettings(
    private val navigator: DestinationsNavigator,
) : SelfieGraphNavigation {
    override fun navigateToSmileSmartSelfieInstructionsScreen(showAttribution: Boolean) =
        navigator.navigate(
            direction = SmileSmartSelfieInstructionsScreenDestination(
                showAttribution = showAttribution,
            ),
        )

    override fun navigateToSmileSelfieCaptureScreen() =
        navigator.navigate(direction = SmileSelfieCaptureScreenDestination)

    override fun navigateToSmileProcessingScreen() =
        navigator.navigate(direction = SmileProcessingScreenDestination)
}

fun DependenciesContainerBuilder<*>.currentNavigator() =
    CoreFeatureNavigatorSettings(navigator = navController.toDestinationsNavigator())
