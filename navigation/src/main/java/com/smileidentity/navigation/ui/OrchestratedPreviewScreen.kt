package com.smileidentity.navigation.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.navigation.destinations.OrchestratedProcessingScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.navigation.EmptyDestinationsNavigator
import com.smileidentity.ui.previews.DevicePreviews
import com.smileidentity.ui.previews.PreviewContent
import com.smileidentity.ui.screens.SmileIDPreviewScreen

@Destination<RootGraph>
@Composable
fun OrchestratedPreviewScreen(modifier: Modifier = Modifier, navigator: DestinationsNavigator) {
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

@DevicePreviews
@Composable
private fun OrchestratedPreviewScreenPreview() {
    PreviewContent {
        OrchestratedPreviewScreen(navigator = EmptyDestinationsNavigator)
    }
}
