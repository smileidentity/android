@file:Suppress("unused", "UnusedReceiverParameter")

package com.smileidentity.compose

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import com.smileidentity.SmileID
import com.smileidentity.compose.theme.ColorScheme
import com.smileidentity.compose.theme.Typography
import com.smileidentity.randomUserId
import com.smileidentity.results.SmartSelfieResult

/**
 * Perform a SmartSelfie™ Registration
 *
 * [Docs](https://docs.smileidentity.com/products/for-individuals-kyc/biometric-authentication)
 *
 * @param userId The user ID to associate with the SmartSelfie™ Registration. Most often, this
 * will correspond to a unique User ID within your own system. If not provided, a random user ID
 * will be generated.
 * @param allowAgentMode Whether to allow Agent Mode or not. If allowed, a switch will be
 * displayed allowing toggling between the back camera and front camera. If not allowed, only the
 * front camera will be used.
 * @param showAttribution Whether to show the Smile ID attribution or not.
 * @param colorScheme The color scheme to use for the UI. This is passed in so that we show a Smile
 * ID branded UI by default, but allow the user to override it if they want.
 * @param typography The typography to use for the UI. This is passed in so that we show a Smile ID
 * branded UI by default, but allow the user to override it if they want.
 * @param onResult Callback to be invoked when the SmartSelfie™ Registration is complete.
 */
// TODO: Rename to SmileID
@Composable
fun SmileID.SmartSelfieRegistrationScreen(
    userId: String = randomUserId(),
    allowAgentMode: Boolean = false,
    showAttribution: Boolean = true,
    colorScheme: ColorScheme = SmileID.ColorScheme,
    typography: Typography = SmileID.Typography,
    onResult: SmartSelfieResult.Callback = SmartSelfieResult.Callback {},
) {
    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
    ) {
        OrchestratedSelfieCaptureScreen(
            userId,
            true,
            allowAgentMode,
            showAttribution,
            onResult = onResult,
        )
    }
}

/**
 * Perform a SmartSelfie™ Authentication
 *
 * [Docs](https://docs.smileidentity.com/products/for-individuals-kyc/biometric-authentication)
 *
 * @param userId The user ID to authenticate with the SmartSelfie™ Authentication. This should be
 * an ID that was previously registered via a SmartSelfie™ Registration
 * (see: [SmileID.SmartSelfieRegistrationScreen])
 * @param allowAgentMode Whether to allow Agent Mode or not. If allowed, a switch will be
 * displayed allowing toggling between the back camera and front camera. If not allowed, only the
 * front camera will be used.
 * @param showAttribution Whether to show the Smile ID attribution or not.
 * @param colorScheme The color scheme to use for the UI. This is passed in so that we show a Smile
 * ID branded UI by default, but allow the user to override it if they want.
 * @param typography The typography to use for the UI. This is passed in so that we show a Smile ID
 * branded UI by default, but allow the user to override it if they want.
 * @param onResult Callback to be invoked when the SmartSelfie™ Registration is complete.
 */
// TODO: Rename to SmileID
@Composable
fun SmileID.SmartSelfieAuthenticationScreen(
    userId: String,
    allowAgentMode: Boolean = false,
    showAttribution: Boolean = true,
    colorScheme: ColorScheme = SmileID.ColorScheme,
    typography: Typography = SmileID.Typography,
    onResult: SmartSelfieResult.Callback = SmartSelfieResult.Callback {},
) {
    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
    ) {
        OrchestratedSelfieCaptureScreen(
            userId,
            false,
            allowAgentMode,
            showAttribution,
            onResult = onResult,
        )
    }
}
