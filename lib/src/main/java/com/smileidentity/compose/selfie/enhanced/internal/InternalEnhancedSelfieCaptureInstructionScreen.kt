package com.smileidentity.compose.selfie.enhanced.internal

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ramcosta.composedestinations.annotation.Destination
import com.smileidentity.compose.selfie.enhanced.composables.EnhancedSelfieCaptureInstructionScreen
import com.smileidentity.compose.selfie.enhanced.graph.EnhancedSelfieGraph

/**
 * The Internal Selfie Capture Instruction Screen. This screen is responsible for displaying the
 * instructions to the user based on the current selfie state.
 * @param modifier The modifier to apply to this composable
 * @param showAttribution Whether to show the SmileID attribution
 * @param onInstructionsAcknowledged Callback Invoked when instructions are acknowledged
 */
@Destination<EnhancedSelfieGraph>
@Composable
internal fun InternalEnhancedSelfieCaptureInstructionScreen(
    modifier: Modifier = Modifier,
    showAttribution: Boolean = true,
    onInstructionsAcknowledged: () -> Unit = { },
) {
    EnhancedSelfieCaptureInstructionScreen(
        modifier = modifier,
        showAttribution = showAttribution,
        onInstructionsAcknowledged = onInstructionsAcknowledged,
    )
}
