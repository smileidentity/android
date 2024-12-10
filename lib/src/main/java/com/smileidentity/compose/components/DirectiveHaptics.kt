package com.smileidentity.compose.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.smileidentity.viewmodel.SelfieHint
import com.smileidentity.viewmodel.SelfieState
import kotlinx.coroutines.delay

/**
 * Provide custom haptic feedback based on the selfie hint.
 */
@Composable
internal fun DirectiveHaptics(selfieState: SelfieState) {
    val haptic = LocalHapticFeedback.current
    if (selfieState is SelfieState.Analyzing) {
        if (selfieState.hint == SelfieHint.LookUp ||
            selfieState.hint == SelfieHint.LookRight ||
            selfieState.hint == SelfieHint.LookLeft
        ) {
            LaunchedEffect(selfieState.hint) {
                // Custom vibration pattern
                for (i in 0..2) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    delay(100)
                }
            }
        }
    } else if (selfieState is SelfieState.Processing) {
        LaunchedEffect(selfieState) {
            // Custom vibration pattern
            for (i in 0..2) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                delay(100)
            }
        }
    }
}
