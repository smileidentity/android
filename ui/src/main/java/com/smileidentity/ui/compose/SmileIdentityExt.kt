@file:Suppress("unused")

package com.smileidentity.ui.compose

import android.Manifest
import androidx.compose.runtime.Composable
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.smileidentity.networking.SmileIdentity
import com.smileidentity.ui.core.SmartSelfieResult

/**
 * Perform a SmartSelfie™ Registration
 *
 * [Docs](https://docs.smileidentity.com/products/for-individuals-kyc/biometric-authentication)
 *
 * @param agentMode Whether to allow the agent mode or not. If allowed, a switch will be displayed
 * allowing toggling between the back camera and front camera. If not allowed, only the front camera
 * will be used.
 * @param manualCaptureMode Whether to allow the manual capture mode or not. If not allowed,
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
    agentMode: Boolean = false,
    manualCaptureMode: Boolean = false,
    onResult: SmartSelfieResult.Callback = SmartSelfieResult.Callback {},
) {
    SmartSelfieOrPermissionScreen(
        agentMode,
        manualCaptureMode,
        rememberPermissionState(Manifest.permission.CAMERA),
        onResult,
    )
}

@Composable
fun SmileIdentity.SmartSelfieAuthenticationScreen() {
    TODO("https://app.shortcut.com/smileid/story/8948")
}
