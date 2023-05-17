@file:Suppress("unused", "UnusedReceiverParameter")

package com.smileidentity.compose

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import com.smileidentity.SmileID
import com.smileidentity.compose.theme.colorScheme
import com.smileidentity.compose.theme.typography
import com.smileidentity.models.Document
import com.smileidentity.randomJobId
import com.smileidentity.randomUserId
import com.smileidentity.results.DocumentVerificationResult
import com.smileidentity.results.SmartSelfieResult
import com.smileidentity.results.SmileIDCallback
import java.io.File

/**
 * Perform a SmartSelfie™ Registration
 *
 * [Docs](https://docs.smileidentity.com/products/for-individuals-kyc/biometric-authentication)
 *
 * @param userId The user ID to associate with the SmartSelfie™ Registration. Most often, this
 * will correspond to a unique User ID within your own system. If not provided, a random user ID
 * will be generated.
 * @param jobId The job ID to associate with the SmartSelfie™ Registration. Most often, this
 * will correspond to a unique Job ID within your own system. If not provided, a random job ID
 * will be generated.
 * @param allowAgentMode Whether to allow Agent Mode or not. If allowed, a switch will be
 * displayed allowing toggling between the back camera and front camera. If not allowed, only the
 * front camera will be used.
 * @param showAttribution Whether to show the Smile ID attribution or not on the Instructions screen
 * @param colorScheme The color scheme to use for the UI. This is passed in so that we show a Smile
 * ID branded UI by default, but allow the user to override it if they want.
 * @param typography The typography to use for the UI. This is passed in so that we show a Smile ID
 * branded UI by default, but allow the user to override it if they want.
 * @param onResult Callback to be invoked when the SmartSelfie™ Registration is complete.
 */
@Composable
fun SmileID.SmartSelfieRegistrationScreen(
    userId: String = rememberSaveable { randomUserId() },
    jobId: String = rememberSaveable { randomJobId() },
    allowAgentMode: Boolean = false,
    showAttribution: Boolean = true,
    colorScheme: ColorScheme = SmileID.colorScheme,
    typography: Typography = SmileID.typography,
    onResult: SmileIDCallback<SmartSelfieResult> = {},
) {
    MaterialTheme(colorScheme = colorScheme, typography = typography) {
        OrchestratedSelfieCaptureScreen(
            userId = userId,
            jobId = jobId,
            isEnroll = true,
            allowAgentMode = allowAgentMode,
            showAttribution = showAttribution,
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
 * @param jobId The job ID to associate with the SmartSelfie™ Authentication. Most often, this
 * will correspond to a unique Job ID within your own system. If not provided, a random job ID
 * will be generated.
 * @param allowAgentMode Whether to allow Agent Mode or not. If allowed, a switch will be
 * displayed allowing toggling between the back camera and front camera. If not allowed, only the
 * front camera will be used.
 * @param showAttribution Whether to show the Smile ID attribution or not on the Instructions screen
 * @param colorScheme The color scheme to use for the UI. This is passed in so that we show a Smile
 * ID branded UI by default, but allow the user to override it if they want.
 * @param typography The typography to use for the UI. This is passed in so that we show a Smile ID
 * branded UI by default, but allow the user to override it if they want.
 * @param onResult Callback to be invoked when the SmartSelfie™ Registration is complete.
 */
@Composable
fun SmileID.SmartSelfieAuthenticationScreen(
    userId: String,
    jobId: String = rememberSaveable { randomJobId() },
    allowAgentMode: Boolean = false,
    showAttribution: Boolean = true,
    colorScheme: ColorScheme = SmileID.colorScheme,
    typography: Typography = SmileID.typography,
    onResult: SmileIDCallback<SmartSelfieResult> = {},
) {
    MaterialTheme(colorScheme = colorScheme, typography = typography) {
        OrchestratedSelfieCaptureScreen(
            userId = userId,
            jobId = jobId,
            isEnroll = false,
            allowAgentMode = allowAgentMode,
            showAttribution = showAttribution,
            onResult = onResult,
        )
    }
}

/**
 * Perform a Document Verification.
 *
 * [Docs](https://docs.smileidentity.com/products/for-individuals-kyc/document-verification)
 *
 * @param userId The user ID to associate with the Document Verification. Most often, this will
 * correspond to a unique User ID within your own system. If not provided, a random user ID will be
 * generated
 * @param jobId The job ID to associate with the Document Verification. Most often, this will
 * correspond to a unique Job ID within your own system. If not provided, a random job ID will be
 * generated
 * @param showAttribution Whether to show the Smile ID attribution or not on the Instructions screen
 * @param allowGalleryUpload Whether to allow the user to upload images from their gallery or not
 * @param enforcedIdType The type of ID to be captured. If not specified, it will be automatically
 * classified on the backend. Use this to restrict which ID type can be captured.
 * @param idAspectRatio The aspect ratio of the ID to be captured. If not specified, it will be
 * inferred from the enforced ID type (if provided). If neither are provided, the aspect ratio will
 * attempt to be inferred from the device's camera. If that fails, it will default to 1.6.
 * @param bypassSelfieCaptureWithFile If provided, the user will not be prompted to take a selfie
 * and instead the provided file will be used as the selfie image
 * @param colorScheme The color scheme to use for the UI. This is passed in so that we show a Smile
 * ID branded UI by default, but allow the user to override it if they want.
 * @param typography The typography to use for the UI. This is passed in so that we show a Smile ID
 * branded UI by default, but allow the user to override it if they want.
 * @param onResult Callback to be invoked when the Document Verification is complete.
 */
@Composable
fun SmileID.DocumentVerification(
    userId: String = rememberSaveable { randomUserId() },
    jobId: String = rememberSaveable { randomJobId() },
    showAttribution: Boolean = true,
    allowGalleryUpload: Boolean = false,
    enforcedIdType: Document? = null,
    idAspectRatio: Float? = enforcedIdType?.aspectRatio,
    bypassSelfieCaptureWithFile: File? = null,
    colorScheme: ColorScheme = SmileID.colorScheme,
    typography: Typography = SmileID.typography,
    onResult: SmileIDCallback<DocumentVerificationResult> = {},
) {
    MaterialTheme(colorScheme = colorScheme, typography = typography) {
        // OrchestratedDocumentCaptureScreen(
        //     userId,
        //     showAttribution,
        //     onResult = onResult,
        // )
    }
}
