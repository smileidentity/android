@file:Suppress("unused", "UnusedReceiverParameter")

package com.smileidentity.compose

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smileidentity.SmileID
import com.smileidentity.compose.biometric.OrchestratedBiometricKYCScreen
import com.smileidentity.compose.components.LocalMetadata
import com.smileidentity.compose.components.SmileThemeSurface
import com.smileidentity.compose.consent.OrchestratedConsentScreen
import com.smileidentity.compose.consent.bvn.OrchestratedBvnConsentScreen
import com.smileidentity.compose.document.OrchestratedDocumentVerificationScreen
import com.smileidentity.compose.selfie.OrchestratedSelfieCaptureScreen
import com.smileidentity.compose.selfie.v2.OrchestratedSelfieCaptureScreenV2
import com.smileidentity.compose.theme.colorScheme
import com.smileidentity.compose.theme.typography
import com.smileidentity.ml.SelfieQualityModel
import com.smileidentity.models.IdInfo
import com.smileidentity.models.JobType
import com.smileidentity.results.BiometricKycResult
import com.smileidentity.results.DocumentVerificationResult
import com.smileidentity.results.EnhancedDocumentVerificationResult
import com.smileidentity.results.SmartSelfieResult
import com.smileidentity.results.SmileIDCallback
import com.smileidentity.util.randomJobId
import com.smileidentity.util.randomUserId
import com.smileidentity.viewmodel.document.DocumentVerificationViewModel
import com.smileidentity.viewmodel.document.EnhancedDocumentVerificationViewModel
import com.smileidentity.viewmodel.viewModelFactory
import java.io.File
import java.net.URL
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf

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
 * @param allowNewEnroll Allows a partner to enroll the same user id again
 * @param allowAgentMode Whether to allow Agent Mode or not. If allowed, a switch will be
 * displayed allowing toggling between the back camera and front camera. If not allowed, only the
 * front camera will be used.
 * @param showAttribution Whether to show the Smile ID attribution or not on the Instructions screen
 * @param showInstructions Whether to deactivate capture screen's instructions for SmartSelfie.
 * @param useStrictMode Whether to use strict mode or not. Strict mode enables an advanced, more
 * secure, and more accurate UX for higher pass rates. [showInstructions] will be ignored
 * @param extraPartnerParams Custom values specific to partners
 * @param colorScheme The color scheme to use for the UI. This is passed in so that we show a Smile
 * ID branded UI by default, but allow the user to override it if they want.
 * @param typography The typography to use for the UI. This is passed in so that we show a Smile ID
 * branded UI by default, but allow the user to override it if they want.
 * @param onResult Callback to be invoked when the SmartSelfie™ Enrollment is complete.
 */
@Composable
fun SmileID.SmartSelfieEnrollment(
    modifier: Modifier = Modifier,
    userId: String = rememberSaveable { randomUserId() },
    jobId: String = rememberSaveable { randomJobId() },
    allowNewEnroll: Boolean = false,
    allowAgentMode: Boolean = false,
    showAttribution: Boolean = true,
    showInstructions: Boolean = true,
    useStrictMode: Boolean = false,
    extraPartnerParams: ImmutableMap<String, String> = persistentMapOf(),
    colorScheme: ColorScheme = SmileID.colorScheme,
    typography: Typography = SmileID.typography,
    onResult: SmileIDCallback<SmartSelfieResult> = {},
) {
    SmileThemeSurface(colorScheme = colorScheme, typography = typography) {
        // TODO: Eventually use the new UI even for nonStrictMode, but with active liveness disabled
        if (useStrictMode) {
            val context = LocalContext.current
            val selfieQualityModel = remember { SelfieQualityModel.newInstance(context) }
            OrchestratedSelfieCaptureScreenV2(
                modifier = modifier,
                userId = userId,
                allowNewEnroll = allowNewEnroll,
                isEnroll = true,
                allowAgentMode = allowAgentMode,
                showAttribution = showAttribution,
                useStrictMode = useStrictMode,
                selfieQualityModel = selfieQualityModel,
                extraPartnerParams = extraPartnerParams,
                onResult = onResult,
            )
        } else {
            OrchestratedSelfieCaptureScreen(
                modifier = modifier,
                userId = userId,
                jobId = jobId,
                allowNewEnroll = allowNewEnroll,
                isEnroll = true,
                allowAgentMode = allowAgentMode,
                showAttribution = showAttribution,
                showInstructions = showInstructions,
                extraPartnerParams = extraPartnerParams,
                onResult = onResult,
            )
        }
    }
}

/**
 * Perform a SmartSelfie™ Authentication
 *
 * [Docs](https://docs.usesmileid.com/products/for-individuals-kyc/biometric-authentication)
 *
 * @param userId The user ID to authenticate with the SmartSelfie™ Authentication. This should be
 * an ID previously registered via a SmartSelfie™ Enrollment
 * (see: [SmileID.SmartSelfieEnrollment])
 * @param jobId The job ID to associate with the SmartSelfie™ Authentication. Most often, this
 * will correspond to a unique Job ID within your own system. If not provided, a random job ID
 * will be generated.
 * @param allowAgentMode Whether to allow Agent Mode or not. If allowed, a switch will be
 * displayed allowing toggling between the back camera and front camera. If not allowed, only the
 * front camera will be used.
 * @param showAttribution Whether to show the Smile ID attribution or not on the Instructions screen
 * @param showInstructions Whether to deactivate capture screen's instructions for SmartSelfie.
 * @param useStrictMode Whether to use strict mode or not. Strict mode enables an advanced, more
 * secure, and more accurate UX for higher pass rates. [showInstructions] will be ignored
 * @param extraPartnerParams Custom values specific to partners
 * @param colorScheme The color scheme to use for the UI. This is passed in so that we show a Smile
 * ID branded UI by default, but allow the user to override it if they want.
 * @param typography The typography to use for the UI. This is passed in so that we show a Smile ID
 * branded UI by default, but allow the user to override it if they want.
 * @param onResult Callback to be invoked when the SmartSelfie™ Enrollment is complete.
 */
@Composable
fun SmileID.SmartSelfieAuthentication(
    userId: String,
    modifier: Modifier = Modifier,
    jobId: String = rememberSaveable { randomJobId() },
    allowNewEnroll: Boolean = false,
    allowAgentMode: Boolean = false,
    showAttribution: Boolean = true,
    showInstructions: Boolean = true,
    useStrictMode: Boolean = false,
    extraPartnerParams: ImmutableMap<String, String> = persistentMapOf(),
    colorScheme: ColorScheme = SmileID.colorScheme,
    typography: Typography = SmileID.typography,
    onResult: SmileIDCallback<SmartSelfieResult> = {},
) {
    SmileThemeSurface(colorScheme = colorScheme, typography = typography) {
        // TODO: Eventually use the new UI even for nonStrictMode, but with active liveness disabled
        if (useStrictMode) {
            val context = LocalContext.current
            val selfieQualityModel = remember { SelfieQualityModel.newInstance(context) }
            OrchestratedSelfieCaptureScreenV2(
                modifier = modifier,
                userId = userId,
                isEnroll = false,
                allowAgentMode = allowAgentMode,
                showAttribution = showAttribution,
                useStrictMode = useStrictMode,
                selfieQualityModel = selfieQualityModel,
                extraPartnerParams = extraPartnerParams,
                onResult = onResult,
            )
        } else {
            OrchestratedSelfieCaptureScreen(
                modifier = modifier,
                userId = userId,
                jobId = jobId,
                allowNewEnroll = allowNewEnroll,
                isEnroll = false,
                allowAgentMode = allowAgentMode,
                showAttribution = showAttribution,
                showInstructions = showInstructions,
                extraPartnerParams = extraPartnerParams,
                onResult = onResult,
            )
        }
    }
}

/**
 * Perform a Document Verification.
 *
 * [Docs](https://docs.usesmileid.com/products/for-individuals-kyc/document-verification)
 *
 * @param countryCode The ISO 3166-1 alpha-3 country code of the document
 * @param documentType An optional document type of the document
 * @param captureBothSides Determines if the document has a back side
 * @param idAspectRatio The aspect ratio of the ID to be captured. If not specified, the aspect
 * ratio will attempt to be inferred from the device's camera.
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
 * @param allowAgentMode Whether to allow Agent Mode or not. If allowed, a switch will be displayed
 * allowing toggling between the back camera and front camera. If not allowed, only the front
 * camera will be used.
 * @param allowGalleryUpload Whether to allow the user to upload images from their gallery or not
 * @param showInstructions Whether to deactivate capture screen's instructions for Document
 * Verification (NB! If instructions are disabled, gallery upload won't be possible)
 * @param extraPartnerParams Custom values specific to partners
 * @param colorScheme The color scheme to use for the UI. This is passed in so that we show a Smile
 * ID branded UI by default, but allow the user to override it if they want.
 * @param typography The typography to use for the UI. This is passed in so that we show a Smile ID
 * branded UI by default, but allow the user to override it if they want.
 * @param onResult Callback to be invoked when the Document Verification is complete.
 */
@Composable
fun SmileID.DocumentVerification(
    countryCode: String,
    modifier: Modifier = Modifier,
    documentType: String? = null,
    captureBothSides: Boolean = true,
    idAspectRatio: Float? = null,
    bypassSelfieCaptureWithFile: File? = null,
    userId: String = rememberSaveable { randomUserId() },
    jobId: String = rememberSaveable { randomJobId() },
    allowNewEnroll: Boolean = false,
    showAttribution: Boolean = true,
    allowAgentMode: Boolean = false,
    allowGalleryUpload: Boolean = false,
    showInstructions: Boolean = true,
    extraPartnerParams: ImmutableMap<String, String> = persistentMapOf(),
    colorScheme: ColorScheme = SmileID.colorScheme,
    typography: Typography = SmileID.typography,
    onResult: SmileIDCallback<DocumentVerificationResult> = {},
) {
    SmileThemeSurface(colorScheme = colorScheme, typography = typography) {
        val metadata = LocalMetadata.current
        OrchestratedDocumentVerificationScreen(
            modifier = modifier,
            userId = userId,
            jobId = jobId,
            showAttribution = showAttribution,
            allowAgentMode = allowAgentMode,
            allowGalleryUpload = allowGalleryUpload,
            showInstructions = showInstructions,
            idAspectRatio = idAspectRatio,
            onResult = onResult,
            viewModel = viewModel(
                factory = viewModelFactory {
                    DocumentVerificationViewModel(
                        jobType = JobType.DocumentVerification,
                        userId = userId,
                        jobId = jobId,
                        allowNewEnroll = allowNewEnroll,
                        countryCode = countryCode,
                        documentType = documentType,
                        captureBothSides = captureBothSides,
                        selfieFile = bypassSelfieCaptureWithFile,
                        extraPartnerParams = extraPartnerParams,
                        metadata = metadata,
                    )
                },
            ),
        )
    }
}

/**
 * Perform Enhanced Document Verification
 *
 * [Docs](https://docs.usesmileid.com/products/for-individuals-kyc/enhanced-document-verification)
 *
 * @param countryCode The ISO 3166-1 alpha-3 country code of the document
 * @param documentType An optional document type of the document
 * @param captureBothSides Determines if the document has a back side
 * @param captureBothSides Whether to capture both sides of the ID or not. Otherwise, only the front
 * side will be captured.
 * @param idAspectRatio The aspect ratio of the ID to be captured. If not specified, the aspect
 * ratio will attempt to be inferred from the device's camera. If that fails, it will default to a
 * standard size of ~1.6
 * @param bypassSelfieCaptureWithFile If provided, the user will not be prompted to take a selfie
 * and instead the provided file will be used as the selfie image
 * @param userId The user ID to associate with the Enhanced Document Verification. Most often, this will
 * correspond to a unique User ID within your own system. If not provided, a random user ID will be
 * generated
 * @param jobId The job ID to associate with the Enhanced Document Verification. Most often, this will
 * correspond to a unique Job ID within your own system. If not provided, a random job ID will be
 * generated
 * @param showAttribution Whether to show the Smile ID attribution or not on the Instructions screen
 * @param allowAgentMode Whether to allow Agent Mode or not. If allowed, a switch will be displayed
 * allowing toggling between the back camera and front camera. If not allowed, only the front
 * camera will be used.
 * @param allowGalleryUpload Whether to allow the user to upload images from their gallery or not
 * @param showInstructions Whether to deactivate capture screen's instructions for Document
 * Verification (NB! If instructions are disabled, gallery upload won't be possible)
 * @param extraPartnerParams Custom values specific to partners
 * @param colorScheme The color scheme to use for the UI. This is passed in so that we show a Smile
 * ID branded UI by default, but allow the user to override it if they want.
 * @param typography The typography to use for the UI. This is passed in so that we show a Smile ID
 * branded UI by default, but allow the user to override it if they want.
 * @param onResult Callback to be invoked when the Enhanced Document Verification is complete.
 */
@Composable
fun SmileID.EnhancedDocumentVerificationScreen(
    countryCode: String,
    modifier: Modifier = Modifier,
    documentType: String? = null,
    captureBothSides: Boolean = true,
    idAspectRatio: Float? = null,
    bypassSelfieCaptureWithFile: File? = null,
    userId: String = rememberSaveable { randomUserId() },
    jobId: String = rememberSaveable { randomJobId() },
    allowNewEnroll: Boolean = false,
    showAttribution: Boolean = true,
    allowAgentMode: Boolean = false,
    allowGalleryUpload: Boolean = false,
    showInstructions: Boolean = true,
    extraPartnerParams: ImmutableMap<String, String> = persistentMapOf(),
    colorScheme: ColorScheme = SmileID.colorScheme,
    typography: Typography = SmileID.typography,
    onResult: SmileIDCallback<EnhancedDocumentVerificationResult> = {},
) {
    SmileThemeSurface(colorScheme = colorScheme, typography = typography) {
        val metadata = LocalMetadata.current
        OrchestratedDocumentVerificationScreen(
            modifier = modifier,
            userId = userId,
            jobId = jobId,
            showAttribution = showAttribution,
            allowAgentMode = allowAgentMode,
            allowGalleryUpload = allowGalleryUpload,
            showInstructions = showInstructions,
            idAspectRatio = idAspectRatio,
            onResult = onResult,
            viewModel = viewModel(
                factory = viewModelFactory {
                    EnhancedDocumentVerificationViewModel(
                        jobType = JobType.EnhancedDocumentVerification,
                        userId = userId,
                        jobId = jobId,
                        allowNewEnroll = allowNewEnroll,
                        countryCode = countryCode,
                        documentType = documentType,
                        captureBothSides = captureBothSides,
                        selfieFile = bypassSelfieCaptureWithFile,
                        extraPartnerParams = extraPartnerParams,
                        metadata = metadata,
                    )
                },
            ),
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
 * @param showInstructions Whether to deactivate capture screen's instructions for SmartSelfie.
 * @param extraPartnerParams Custom values specific to partners
 * @param colorScheme The color scheme to use for the UI. This is passed in so that we show a Smile
 * ID branded UI by default, but allow the user to override it if they want.
 * @param typography The typography to use for the UI. This is passed in so that we show a Smile ID
 * branded UI by default, but allow the user to override it if they want.
 * @param onResult Callback to be invoked when the Biometric KYC is complete.
 */
@Composable
fun SmileID.BiometricKYC(
    idInfo: IdInfo,
    modifier: Modifier = Modifier,
    userId: String = rememberSaveable { randomUserId() },
    jobId: String = rememberSaveable { randomJobId() },
    allowNewEnroll: Boolean = false,
    allowAgentMode: Boolean = false,
    showAttribution: Boolean = true,
    showInstructions: Boolean = true,
    extraPartnerParams: ImmutableMap<String, String> = persistentMapOf(),
    colorScheme: ColorScheme = SmileID.colorScheme,
    typography: Typography = SmileID.typography,
    onResult: SmileIDCallback<BiometricKycResult> = {},
) {
    SmileThemeSurface(colorScheme = colorScheme, typography = typography) {
        OrchestratedBiometricKYCScreen(
            modifier = modifier,
            idInfo = idInfo,
            userId = userId,
            jobId = jobId,
            allowNewEnroll = allowNewEnroll,
            allowAgentMode = allowAgentMode,
            showAttribution = showAttribution,
            showInstructions = showInstructions,
            extraPartnerParams = extraPartnerParams,
            onResult = onResult,
        )
    }
}

/**
 * Perform BVN verification: Verify the BVN information of your user and confirm that the ID
 * actually belongs to the user by requesting an OTP.
 *
 * [Docs](https://docs.usesmileid.com/integration-options/mobile/android/consent-screen#bvn-consent-screen)
 *
 * @param partnerIcon Your own icon to display on the BVN Consent screen (i.e. company logo)
 * @param partnerName Your own name to display on the BVN Consent screen (i.e. company name)
 * @param partnerPrivacyPolicy A link to your own privacy policy to display
 * @param onConsentGranted Callback to be invoked when the BVN verification job is complete.
 * @param onConsentDenied Callback to be invoked when the user denies consent to BVN verification.
 * @param showAttribution Whether to show the Smile ID attribution or not on the Instructions screen
 * @param userId The user ID to associate with the BVN Job. Most often, this will correspond
 * to a unique User ID within your own system. If not provided, a random user ID will be generated
 * @param colorScheme The color scheme to use for the UI. This is passed in so that we show a Smile
 * ID branded UI by default, but allow the user to override it if they want.
 * @param typography The typography to use for the UI. This is passed in so that we show a Smile ID
 * branded UI by default, but allow the user to override it if they want.
 */
@Composable
fun SmileID.BvnConsentScreen(
    partnerIcon: Painter,
    partnerName: String,
    partnerPrivacyPolicy: URL,
    onConsentGranted: () -> Unit,
    onConsentDenied: () -> Unit,
    modifier: Modifier = Modifier,
    showAttribution: Boolean = true,
    userId: String = rememberSaveable { randomUserId() },
    colorScheme: ColorScheme = SmileID.colorScheme,
    typography: Typography = SmileID.typography,
) {
    SmileThemeSurface(colorScheme = colorScheme, typography = typography) {
        OrchestratedBvnConsentScreen(
            modifier = modifier,
            userId = userId,
            partnerIcon = partnerIcon,
            partnerName = partnerName,
            partnerPrivacyPolicy = partnerPrivacyPolicy,
            onConsentGranted = onConsentGranted,
            onConsentDenied = onConsentDenied,
            showAttribution = showAttribution,
        )
    }
}

@Composable
fun SmileID.ConsentScreen(
    partnerIcon: Painter,
    partnerName: String,
    productName: String,
    partnerPrivacyPolicy: URL,
    onConsentGranted: () -> Unit,
    onConsentDenied: () -> Unit,
    modifier: Modifier = Modifier,
    showAttribution: Boolean = true,
) {
    SmileThemeSurface(colorScheme = colorScheme, typography = typography) {
        OrchestratedConsentScreen(
            partnerIcon = partnerIcon,
            partnerName = partnerName,
            productName = productName,
            partnerPrivacyPolicy = partnerPrivacyPolicy,
            onConsentGranted = onConsentGranted,
            onConsentDenied = onConsentDenied,
            modifier = modifier,
            showAttribution = showAttribution,
        )
    }
}
