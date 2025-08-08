package com.smileidentity.navigation.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.smileidentity.navigation.graph.SmileIDGraph
import com.smileidentity.ui.SmileIDInstructionsScreen

@Destination<SmileIDGraph>(start = true)
@Composable
fun OrchestratedInstructionsScreen(
    modifier: Modifier = Modifier,
    destination: DestinationsNavigator,
) {
    SmileIDInstructionsScreen(
        modifier = modifier,
        onContinue = {},
        onCancel = {},
    )
}
