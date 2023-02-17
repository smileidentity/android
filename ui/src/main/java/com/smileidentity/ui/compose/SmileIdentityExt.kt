@file:Suppress("unused")

package com.smileidentity.ui.compose

import android.Manifest
import androidx.compose.runtime.Composable
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.smileidentity.networking.SmileIdentity
import com.smileidentity.ui.core.EnhancedKycResult
import com.smileidentity.ui.core.SmartSelfieResult
import com.smileidentity.ui.core.randomSessionId
import com.smileidentity.ui.core.randomUserId

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
 * @param allowManualCapture Whether to allow the manual capture mode or not. If not allowed,
 * captures will be automatically taken when a face is detected. If allowed, a button will be
 * displayed to allow the user to manually initiate liveness and selfie capture. This has a higher
 * likelihood of Job failure, as the user may not take the selfie under the correct conditions. When
 * this mode is enabled, face detection will still be performed.
 * @param sessionId The session token to associate with this job. If not provided, a random one will
 * be generated. Useful for tracking multiple jobs, and to retrieve image capture files directly at
 * a later time via [com.smileidentity.ui.core.retrieveCapturedImages].
 * @param onResult Callback to be invoked when the SmartSelfie™ Registration is complete.
 */
// Since Experimental APIs are *not* exposed in the outward-facing API, consumers won't need to
// add the @OptIn annotation to use this API.
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SmileIdentity.SmartSelfieRegistrationScreen(
    userId: String = randomUserId(),
    allowAgentMode: Boolean = false,
    allowManualCapture: Boolean = false,
    sessionId: String = randomSessionId(),
    onResult: SmartSelfieResult.Callback = SmartSelfieResult.Callback {},
) {
    SmartSelfieOrPermissionScreen(
        userId,
        true,
        allowAgentMode,
        allowManualCapture,
        sessionId,
        rememberPermissionState(Manifest.permission.CAMERA),
        onResult,
    )
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
 * @param allowManualCapture Whether to allow the manual capture mode or not. If not allowed,
 * captures will be automatically taken when a face is detected. If allowed, a button will be
 * displayed to allow the user to manually initiate liveness and selfie capture. This has a higher
 * likelihood of Job failure, as the user may not take the selfie under the correct conditions. When
 * this mode is enabled, face detection will still be performed.
 * @param sessionId The session token to associate with this job. If not provided, a random one will
 * be generated. Useful for tracking multiple jobs, and to retrieve image capture files directly at
 * a later time via [com.smileidentity.ui.core.retrieveCapturedImages].
 * @param onResult Callback to be invoked when the SmartSelfie™ Registration is complete.
 */
// Since Experimental APIs are *not* exposed in the outward-facing API, consumers won't need to
// add the @OptIn annotation to use this API.
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SmileIdentity.SmartSelfieAuthenticationScreen(
    userId: String,
    allowAgentMode: Boolean = false,
    allowManualCapture: Boolean = false,
    sessionId: String = randomSessionId(),
    onResult: SmartSelfieResult.Callback = SmartSelfieResult.Callback {},
) {
    SmartSelfieOrPermissionScreen(
        userId,
        false,
        allowAgentMode,
        allowManualCapture,
        sessionId,
        rememberPermissionState(Manifest.permission.CAMERA),
        onResult,
    )
}

/**
 * Perform an Enhanced KYC (using the synchronous API)
 *
 * [Docs](https://docs.smileidentity.com/products/for-individuals-kyc/identity-lookup)
 *
 * @param sessionId The session token to associate with this job. If not provided, a random one will
 * be generated. Useful for tracking multiple jobs, and to retrieve image capture files directly at
 * a later time via [com.smileidentity.ui.core.retrieveCapturedImages].
 */
@Composable
fun SmileIdentity.EnhancedKycScreen(
    sessionId: String = randomSessionId(),
    onResult: EnhancedKycResult.Callback = EnhancedKycResult.Callback {},
) {
    com.smileidentity.ui.compose.EnhancedKycScreen(
        sessionId = sessionId,
        onResult = onResult,
    )
}
