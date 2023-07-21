@file:Suppress("unused", "UnusedReceiverParameter")

package com.smileidentity.compose

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.painter.Painter
import com.smileidentity.SmileID
import com.smileidentity.compose.biometric.OrchestratedBiometricKYCScreen
import com.smileidentity.compose.consent.bvn.OrchestratedBvnConsentScreen
import com.smileidentity.compose.document.OrchestratedDocumentVerificationScreen
import com.smileidentity.compose.selfie.OrchestratedSelfieCaptureScreen
import com.smileidentity.compose.theme.colorScheme
import com.smileidentity.compose.theme.typography
import com.smileidentity.models.Document
import com.smileidentity.models.IdInfo
import com.smileidentity.results.BiometricKycResult
import com.smileidentity.results.DocumentVerificationResult
import com.smileidentity.results.SmartSelfieResult
import com.smileidentity.results.SmileIDCallback
import com.smileidentity.util.randomJobId
import com.smileidentity.util.randomUserId
import java.io.File
import java.net.URL

/**
 * Perform a SmartSelfie™ Enrollment
 *
 * [Docs](https://docs.usesmileid.com/products/for-individuals-kyc/biometric-authentication)
 *
 * @param userId The user ID to associate with the SmartSelfie™ Enrollment. Most often, this
 * will correspond to a unique User ID within your own system. If not provided, a random user ID
 * will be generated.
 * @param jobId The job ID to associate with the SmartSelfie™ Enrollment. Most often, this
 * will correspond to a unique Job ID within your own system. If not provided, a random job ID
 * will be generated.
 * @param allowAgentMode Whether to allow Agent Mode or not. If allowed, a switch will be
 * displayed allowing toggling between the back camera and front camera. If not allowed, only the
 * front camera will be used.
 * @param showAttribution Whether to show the Smile ID attribution or not on the Instructions screen
 * @param showInstructions Whether to deactivate capture screen's instructions for
 * SmartSelfie.
 * @param colorScheme The color scheme to use for the UI. This is passed in so that we show a Smile
 * ID branded UI by default, but allow the user to override it if they want.
 * @param typography The typography to use for the UI. This is passed in so that we show a Smile ID
 * branded UI by default, but allow the user to override it if they want.
 * @param onResult Callback to be invoked when the SmartSelfie™ Enrollment is complete.
 */
@Composable
fun SmileID.SmartSelfieEnrollment(
    userId: String = rememberSaveable { randomUserId() },
    jobId: String = rememberSaveable { randomJobId() },
    allowAgentMode: Boolean = false,
    showAttribution: Boolean = true,
    showInstructions: Boolean = true,
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
            showInstructions = showInstructions,
            onResult = onResult,
        )
    }
}

/**
 * Perform a SmartSelfie™ Authentication
 *
 * [Docs](https://docs.usesmileid.com/products/for-individuals-kyc/biometric-authentication)
 *
 * @param userId The user ID to authenticate with the SmartSelfie™ Authentication. This should be
 * an ID that was previously registered via a SmartSelfie™ Enrollment
 * (see: [SmileID.SmartSelfieEnrollment])
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
 * @param onResult Callback to be invoked when the SmartSelfie™ Enrollment is complete.
 */
@Composable
fun SmileID.SmartSelfieAuthentication(
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
 * [Docs](https://docs.usesmileid.com/products/for-individuals-kyc/document-verification)
 *
 * @param idType The type of ID to be captured
 * @param idAspectRatio The aspect ratio of the ID to be captured. If not specified, it will be
 * inferred from the enforced ID type (if provided). If neither are provided, the aspect ratio will
 * attempt to be inferred from the device's camera. If that fails, it will default to 1.6.
 * @param captureBothSides Whether to capture both sides of the ID or not. Otherwise, only the front
 * side will be captured.
 * @param bypassSelfieCaptureWithFile If provided, the user will not be prompted to take a selfie
 * and instead the provided file will be used as the selfie image
 * @param userId The user ID to associate with the Document Verification. Most often, this will
 * correspond to a unique User ID within your own system. If not provided, a random user ID will be
 * generated
 * @param jobId The job ID to associate with the Document Verification. Most often, this will
 * correspond to a unique Job ID within your own system. If not provided, a random job ID will be
 * generated
 * @param showAttribution Whether to show the Smile ID attribution or not on the Instructions screen
 * @param allowGalleryUpload Whether to allow the user to upload images from their gallery or not
 * @param showInstructions Whether to deactivate capture screen's instructions for Document
 * Verification (NB! If instructions are disabled, gallery upload won't be possible)
 * @param colorScheme The color scheme to use for the UI. This is passed in so that we show a Smile
 * ID branded UI by default, but allow the user to override it if they want.
 * @param typography The typography to use for the UI. This is passed in so that we show a Smile ID
 * branded UI by default, but allow the user to override it if they want.
 * @param onResult Callback to be invoked when the Document Verification is complete.
 */
@Composable
fun SmileID.DocumentVerification(
    idType: Document,
    idAspectRatio: Float? = idType.aspectRatio,
    captureBothSides: Boolean = false,
    bypassSelfieCaptureWithFile: File? = null,
    userId: String = rememberSaveable { randomUserId() },
    jobId: String = rememberSaveable { randomJobId() },
    showAttribution: Boolean = true,
    allowGalleryUpload: Boolean = false,
    showInstructions: Boolean = false,
    colorScheme: ColorScheme = SmileID.colorScheme,
    typography: Typography = SmileID.typography,
    onResult: SmileIDCallback<DocumentVerificationResult> = {},
) {
    MaterialTheme(colorScheme = colorScheme, typography = typography) {
        OrchestratedDocumentVerificationScreen(
            idType = idType,
            userId = userId,
            jobId = jobId,
            showAttribution = showAttribution,
            allowGalleryUpload = allowGalleryUpload,
            showInstructions = showInstructions,
            idAspectRatio = idAspectRatio,
            captureBothSides = captureBothSides,
            bypassSelfieCaptureWithFile = bypassSelfieCaptureWithFile,
            onResult = onResult,
        )
    }
}

/**
 * Perform a Biometric KYC: Verify the ID information of your user and confirm that the ID actually
 * belongs to the user. This is achieved by comparing the user's SmartSelfie™ to the user's photo in
 * an ID authority database
 *
 * [Docs](https://docs.usesmileid.com/products/for-individuals-kyc/biometric-kyc)
 *
 * @param idInfo The ID information to look up in the ID Authority
 * @param userId The user ID to associate with the Biometric KYC. Most often, this will correspond
 * to a unique User ID within your own system. If not provided, a random user ID will be generated
 * @param jobId The job ID to associate with the Biometric KYC. Most often, this will correspond
 * to a unique Job ID within your own system. If not provided, a random job ID will be generated
 * @param allowAgentMode Whether to allow Agent Mode or not. If allowed, a switch will be displayed
 * allowing toggling between the back camera and front camera. If not allowed, only the front
 * camera will be used.
 * @param showAttribution Whether to show the Smile ID attribution or not on the Instructions screen
 * @param colorScheme The color scheme to use for the UI. This is passed in so that we show a Smile
 * ID branded UI by default, but allow the user to override it if they want.
 * @param typography The typography to use for the UI. This is passed in so that we show a Smile ID
 * branded UI by default, but allow the user to override it if they want.
 * @param onResult Callback to be invoked when the Biometric KYC is complete.
 */
@Composable
fun SmileID.BiometricKYC(
    idInfo: IdInfo,
    partnerIcon: Painter,
    partnerName: String,
    productName: String,
    partnerPrivacyPolicy: URL,
    userId: String = rememberSaveable { randomUserId() },
    jobId: String = rememberSaveable { randomJobId() },
    allowAgentMode: Boolean = false,
    showAttribution: Boolean = true,
    colorScheme: ColorScheme = SmileID.colorScheme,
    typography: Typography = SmileID.typography,
    onResult: SmileIDCallback<BiometricKycResult> = {},
) {
    MaterialTheme(colorScheme = colorScheme, typography = typography) {
        OrchestratedBiometricKYCScreen(
            idInfo = idInfo,
            partnerIcon = partnerIcon,
            partnerName = partnerName,
            productName = productName,
            partnerPrivacyPolicy = partnerPrivacyPolicy,
            userId = userId,
            jobId = jobId,
            allowAgentMode = allowAgentMode,
            showAttribution = showAttribution,
            onResult = onResult,
        )
    }
}

@Composable
fun SmileID.BvnConsentScreen(
) {
    MaterialTheme(colorScheme = colorScheme, typography = typography) {
        OrchestratedBvnConsentScreen()
    }
}