@file:Suppress("unused")

package com.smileidentity.ui.compose

import android.Manifest
import androidx.compose.runtime.Composable
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.smileidentity.networking.SmileIdentity
import com.smileidentity.ui.core.SmartSelfieResult
import com.smileidentity.ui.core.randomUserId

/**
 * Perform a SmartSelfie™ Registration
 *
 * [Docs](https://docs.smileidentity.com/products/for-individuals-kyc/biometric-authentication)
 *
 * @param allowAgentMode Whether to allow the agent mode or not. If allowed, a switch will be displayed
 * allowing toggling between the back camera and front camera. If not allowed, only the front camera
 * will be used.
 * @param allowManualCapture Whether to allow the manual capture mode or not. If not allowed,
 * captures will be automatically taken when a face is detected. If allowed, a button will be
 * displayed to allow the user to manually initiate liveness and selfie capture. This has a higher
 * likelihood of Job failure, as the user may not take the selfie under the correct conditions. When
 * this mode is enabled, face detection will still be performed.
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
    onResult: SmartSelfieResult.Callback = SmartSelfieResult.Callback {},
) {
    SmartSelfieOrPermissionScreen(
        userId,
        true,
        allowAgentMode,
        allowManualCapture,
        rememberPermissionState(Manifest.permission.CAMERA),
        onResult,
    )
}

/**
 * Perform a SmartSelfie™ Authentication
 */
// Since Experimental APIs are *not* exposed in the outward-facing API, consumers won't need to
// add the @OptIn annotation to use this API.
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SmileIdentity.SmartSelfieAuthenticationScreen(
    userId: String,
    allowAgentMode: Boolean = false,
    allowManualCapture: Boolean = false,
    onResult: SmartSelfieResult.Callback = SmartSelfieResult.Callback {},
) {
    SmartSelfieOrPermissionScreen(
        userId,
        false,
        allowAgentMode,
        allowManualCapture,
        rememberPermissionState(Manifest.permission.CAMERA),
        onResult,
    )
}
