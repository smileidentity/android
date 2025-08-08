package com.smileidentity.navigation.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.smileidentity.navigation.graph.SmileIDGraph
import com.smileidentity.ui.SmileIDProcessingScreen

@Destination<SmileIDGraph>
@Composable
fun OrchestratedProcessingScreen(
    modifier: Modifier = Modifier,
    destination: DestinationsNavigator,
) {
    SmileIDProcessingScreen(
        modifier = modifier,
        onContinue = {},
        onRetry = {},
    )
}
