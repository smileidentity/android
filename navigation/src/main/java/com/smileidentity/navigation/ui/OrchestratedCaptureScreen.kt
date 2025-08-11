package com.smileidentity.navigation.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.navigation.destinations.OrchestratedPreviewScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.smileidentity.ml.states.IdentityScanState
import com.smileidentity.ui.SmileIDCaptureScreen

@Destination<RootGraph>
@Composable
fun OrchestratedCaptureScreen(
    modifier: Modifier = Modifier,
    navigator: DestinationsNavigator,
) {
    SmileIDCaptureScreen(
        scanType = IdentityScanState.ScanType.SELFIE,
        modifier = modifier,
        onResult = { file ->
            // use the file here
            navigator.navigate(direction = OrchestratedPreviewScreenDestination)
        },
    )
}
