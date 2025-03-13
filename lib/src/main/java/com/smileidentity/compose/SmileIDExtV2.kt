@file:Suppress("unused", "UnusedReceiverParameter")

package com.smileidentity.compose

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.smileidentity.SmileID
import com.smileidentity.compose.components.SmileThemeSurface
import com.smileidentity.compose.selfie.enhanced.OrchestratedSelfieCaptureScreenEnhanced
import com.smileidentity.compose.theme.colorScheme
import com.smileidentity.compose.theme.typographyV2
import com.smileidentity.ml.SelfieQualityModel
import com.smileidentity.results.SmartSelfieResult
import com.smileidentity.results.SmileIDCallback
import com.smileidentity.util.randomUserId
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf

/**
 * Perform a SmartSelfie™ Enrollment (Enhanced)
 *
 * [Docs](https://docs.usesmileid.com/products/for-individuals-kyc/biometric-authentication)
 *
 * @param userId The user ID to associate with the SmartSelfie™ Enrollment. Most often, this
 * will correspond to a unique User ID within your own system. If not provided, a random user ID
 * will be generated.
 * @param allowNewEnroll Allows a partner to enroll the same user id again
 * @param showAttribution Whether to show the Smile ID attribution or not on the Instructions screen
 * @param showInstructions Whether to deactivate capture screen's instructions for SmartSelfie.
 * @param skipApiSubmission Whether to skip the API submission and return the result of capture only
 * @param extraPartnerParams Custom values specific to partners
 * @param colorScheme The color scheme to use for the UI. This is passed in so that we show a Smile
 * ID branded UI by default, but allow the user to override it if they want.
 * @param typography The typography to use for the UI. This is passed in so that we show a Smile ID
 * branded UI by default, but allow the user to override it if they want.
 * @param onResult Callback to be invoked when the SmartSelfie™ Enrollment is complete.
 */
@Composable
fun SmileID.SmartSelfieEnrollmentEnhanced(
    modifier: Modifier = Modifier,
    userId: String = rememberSaveable { randomUserId() },
    allowNewEnroll: Boolean = false,
    showAttribution: Boolean = true,
    showInstructions: Boolean = true,
    skipApiSubmission: Boolean = false,
    extraPartnerParams: ImmutableMap<String, String> = persistentMapOf(),
    colorScheme: ColorScheme = SmileID.colorScheme,
    typography: Typography = SmileID.typographyV2,
    onResult: SmileIDCallback<SmartSelfieResult> = {},
) {
    SmileThemeSurface(colorScheme = colorScheme, typography = typography) {
        val context = LocalContext.current
        val selfieQualityModel = remember { SelfieQualityModel.newInstance(context) }
        OrchestratedSelfieCaptureScreenEnhanced(
            modifier = modifier,
            userId = userId,
            allowNewEnroll = allowNewEnroll,
            showInstructions = showInstructions,
            isEnroll = true,
            showAttribution = showAttribution,
            skipApiSubmission = skipApiSubmission,
            selfieQualityModel = selfieQualityModel,
            extraPartnerParams = extraPartnerParams,
            onResult = onResult,
        )
    }
}

/**
 * Perform a SmartSelfie™ Authentication (Enhanced)
 *
 * [Docs](https://docs.usesmileid.com/products/for-individuals-kyc/biometric-authentication)
 *
 * @param userId The user ID to authenticate with the SmartSelfie™ Authentication. This should be
 * an ID previously registered via a SmartSelfie™ Enrollment
 * (see: [SmileID.SmartSelfieEnrollment])
 * @param allowNewEnroll Allows a partner to enroll the same user id again
 * @param showAttribution Whether to show the Smile ID attribution or not on the Instructions screen
 * @param showInstructions Whether to deactivate capture screen's instructions for SmartSelfie.
 * @param skipApiSubmission Whether to skip the API submission and return the result of capture only
 * @param extraPartnerParams Custom values specific to partners
 * @param colorScheme The color scheme to use for the UI. This is passed in so that we show a Smile
 * ID branded UI by default, but allow the user to override it if they want.
 * @param typography The typography to use for the UI. This is passed in so that we show a Smile ID
 * branded UI by default, but allow the user to override it if they want.
 * @param onResult Callback to be invoked when the SmartSelfie™ Enrollment is complete.
 */
@Composable
fun SmileID.SmartSelfieAuthenticationEnhanced(
    userId: String,
    modifier: Modifier = Modifier,
    allowNewEnroll: Boolean = false,
    showAttribution: Boolean = true,
    showInstructions: Boolean = true,
    skipApiSubmission: Boolean = false,
    extraPartnerParams: ImmutableMap<String, String> = persistentMapOf(),
    colorScheme: ColorScheme = SmileID.colorScheme,
    typography: Typography = SmileID.typographyV2,
    onResult: SmileIDCallback<SmartSelfieResult> = {},
) {
    SmileThemeSurface(colorScheme = colorScheme, typography = typography) {
        val context = LocalContext.current
        val selfieQualityModel = remember { SelfieQualityModel.newInstance(context) }
        OrchestratedSelfieCaptureScreenEnhanced(
            modifier = modifier,
            userId = userId,
            isEnroll = false,
            allowNewEnroll = allowNewEnroll,
            showAttribution = showAttribution,
            showInstructions = showInstructions,
            skipApiSubmission = skipApiSubmission,
            selfieQualityModel = selfieQualityModel,
            extraPartnerParams = extraPartnerParams,
            onResult = onResult,
        )
    }
}
