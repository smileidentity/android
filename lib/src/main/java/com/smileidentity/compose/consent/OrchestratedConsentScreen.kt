package com.smileidentity.compose.consent

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import java.net.URL

/**
 * Responsible for showing the consent screen and the consent denied (try again) screens.
 */
@Composable
fun OrchestratedConsentScreen(
    partnerIcon: Painter,
    partnerName: String,
    productName: String,
    partnerPrivacyPolicy: URL,
    onConsentGranted: () -> Unit,
    onConsentDenied: () -> Unit,
    modifier: Modifier = Modifier,
    showAttribution: Boolean = true,
) {
    var showTryAgain by remember { mutableStateOf(false) }
    if (showTryAgain) {
        ConsentDeniedScreen(
            onGoBack = { showTryAgain = false },
            onCancel = onConsentDenied,
            modifier = modifier,
            showAttribution = showAttribution,
        )
    } else {
        ConsentScreen(
            partnerIcon = partnerIcon,
            partnerName = partnerName,
            productName = productName,
            partnerPrivacyPolicy = partnerPrivacyPolicy,
            showAttribution = showAttribution,
            modifier = modifier,
            onContinue = onConsentGranted,
            onCancel = { showTryAgain = true },
        )
    }
}
