@file:Suppress("unused")

package com.smileidentity.compose

import androidx.compose.runtime.Composable
import com.smileidentity.SmileIdentity
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
 * @param allowAgentMode Whether to allow the agent mode or not. If allowed, a switch will be
 * displayed allowing toggling between the back camera and front camera. If not allowed, only the
 * front camera will be used.
 * @param onResult Callback to be invoked when the SmartSelfie™ Registration is complete.
 */
@Composable
fun SmileIdentity.SmartSelfieRegistrationScreen(
    userId: String = randomUserId(),
    allowAgentMode: Boolean = false,
    onResult: SmartSelfieResult.Callback = SmartSelfieResult.Callback {},
) {
    OrchestratedSelfieCaptureScreen(userId, true, allowAgentMode, onResult = onResult)
}

/**
 * Perform a SmartSelfie™ Authentication
 *
 * [Docs](https://docs.smileidentity.com/products/for-individuals-kyc/biometric-authentication)
 *
 * @param userId The user ID to authenticate with the SmartSelfie™ Authentication. This should be
 * an ID that was previously passed to a SmartSelfie™ Registration
 * (see: [SmileIdentity.SmartSelfieRegistrationScreen])
 * @param allowAgentMode Whether to allow the agent mode or not. If allowed, a switch will be
 * displayed allowing toggling between the back camera and front camera. If not allowed, only the
 * front camera will be used.
 * @param onResult Callback to be invoked when the SmartSelfie™ Registration is complete.
 */
@Composable
fun SmileIdentity.SmartSelfieAuthenticationScreen(
    userId: String,
    allowAgentMode: Boolean = false,
    onResult: SmartSelfieResult.Callback = SmartSelfieResult.Callback {},
) {
    OrchestratedSelfieCaptureScreen(userId, false, allowAgentMode, onResult = onResult)
}
