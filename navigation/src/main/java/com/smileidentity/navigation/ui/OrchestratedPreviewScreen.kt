package com.smileidentity.navigation.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.navigation.destinations.OrchestratedProcessingScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.smileidentity.ui.SmileIDPreviewScreen

@Destination<RootGraph>
@Composable
fun OrchestratedPreviewScreen(
    modifier: Modifier = Modifier,
    navigator: DestinationsNavigator,
) {
    SmileIDPreviewScreen(
        modifier = modifier,
        onContinue = {
            navigator.navigate(direction = OrchestratedProcessingScreenDestination)
        },
        onRetry = {
            navigator.navigateUp()
        },
    )
}
