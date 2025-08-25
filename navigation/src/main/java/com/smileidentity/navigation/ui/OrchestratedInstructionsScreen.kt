package com.smileidentity.navigation.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.navigation.destinations.OrchestratedCaptureScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.navigation.EmptyDestinationsNavigator
import com.smileidentity.ui.design.SmileIDTheme
import com.smileidentity.ui.previews.DevicePreviews
import com.smileidentity.ui.previews.PreviewContent
import com.smileidentity.ui.screens.SmileIDInstructionsScreen

@Destination<RootGraph>(start = true)
@Composable
fun OrchestratedInstructionsScreen(
    modifier: Modifier = Modifier,
    navigator: DestinationsNavigator,
) {
    SmileIDTheme {
        Scaffold(
            contentWindowInsets = WindowInsets.systemBars,
            content = { innerPadding ->
                SmileIDInstructionsScreen(
                    modifier = modifier.padding(paddingValues = innerPadding),
                    onContinue = {
                        navigator.navigate(direction = OrchestratedCaptureScreenDestination)
                    },
                )
            },
        )
    }
}

@DevicePreviews
@Composable
private fun OrchestratedInstructionsScreenPreview() {
    PreviewContent {
        OrchestratedInstructionsScreen(navigator = EmptyDestinationsNavigator)
    }
}
