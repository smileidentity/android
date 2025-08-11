package com.smileidentity.navigation.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.smile.destinations.OrchestratedCaptureScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.smileidentity.ui.SmileIDInstructionsScreen

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
