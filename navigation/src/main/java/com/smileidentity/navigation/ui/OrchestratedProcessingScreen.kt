package com.smileidentity.navigation.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.navigation.destinations.OrchestratedCaptureScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.navigation.EmptyDestinationsNavigator
import com.smileidentity.ui.previews.DevicePreviews
import com.smileidentity.ui.previews.ThemePreviews
import com.smileidentity.ui.screens.SmileIDProcessingScreen

@Destination<RootGraph>
@Composable
fun OrchestratedProcessingScreen(modifier: Modifier = Modifier, navigator: DestinationsNavigator) {
    SmileIDProcessingScreen(
        modifier = modifier,
        onContinue = {
            navigator.navigate(direction = OrchestratedCaptureScreenDestination)
        },
        onRetry = {
            navigator.navigateUp()
        },
    )
}

@ThemePreviews
@DevicePreviews
@Composable
private fun OrchestratedProcessingScreenPreview() {
    OrchestratedProcessingScreen(navigator = EmptyDestinationsNavigator)
}
