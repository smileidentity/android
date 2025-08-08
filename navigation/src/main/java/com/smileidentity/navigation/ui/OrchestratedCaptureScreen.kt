package com.smileidentity.navigation.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ramcosta.composedestinations.annotation.Destination
import com.smileidentity.ml.states.IdentityScanState
import com.smileidentity.navigation.graph.SmileIDGraph
import com.smileidentity.ui.SmileIDCaptureScreen

@Destination<SmileIDGraph>
@Composable
fun OrchestratedCaptureScreen(
    modifier: Modifier = Modifier,
) {
    SmileIDCaptureScreen(
        scanType = IdentityScanState.ScanType.SELFIE,
        modifier = modifier,
        onResult = { file ->
            // use the file here
        },
    )
}
