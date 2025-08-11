package com.smileidentity.navigation.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.navigation.destinations.OrchestratedCaptureScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.navigation.EmptyDestinationsNavigator
import com.smileidentity.ui.SmileIDInstructionsScreen
import com.smileidentity.ui.previews.DevicePreviews
import com.smileidentity.ui.previews.ThemePreviews

@Destination<RootGraph>(start = true)
@Composable
fun OrchestratedInstructionsScreen(
    modifier: Modifier = Modifier,
    navigator: DestinationsNavigator,
) {
    SmileIDInstructionsScreen(
        modifier = modifier,
        onContinue = {
            navigator.navigate(direction = OrchestratedCaptureScreenDestination)
        },
        onCancel = {
            navigator.navigate(direction = OrchestratedCaptureScreenDestination)
        },
    )
}

@ThemePreviews
@DevicePreviews
@Composable
private fun OrchestratedInstructionsScreenPreview() {
    OrchestratedInstructionsScreen(navigator = EmptyDestinationsNavigator)
}
