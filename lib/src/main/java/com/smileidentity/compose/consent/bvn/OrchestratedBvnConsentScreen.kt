package com.smileidentity.compose.consent.bvn

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smileidentity.compose.consent.OrchestratedConsentScreen
import com.smileidentity.viewmodel.BvnConsentScreens
import com.smileidentity.viewmodel.BvnConsentViewModel
import com.smileidentity.viewmodel.viewModelFactory
import java.net.URL

@Composable
internal fun OrchestratedBvnConsentScreen(
    userId: String,
    partnerIcon: Painter,
    partnerName: String,
    partnerPrivacyPolicy: URL,
    onConsentGranted: () -> Unit,
    onConsentDenied: () -> Unit,
    modifier: Modifier = Modifier,
    showAttribution: Boolean = true,
    viewModel: BvnConsentViewModel = viewModel(
        factory = viewModelFactory { BvnConsentViewModel(userId = userId) },
    ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    Box(
        modifier = modifier
            .background(color = MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.statusBars)
            .consumeWindowInsets(WindowInsets.statusBars)
            .fillMaxSize(),
    ) {
        when (uiState.currentScreen) {
            BvnConsentScreens.ConsentScreen -> OrchestratedConsentScreen(
                partnerIcon = partnerIcon,
                partnerName = partnerName,
                productName = "BVN",
                partnerPrivacyPolicy = partnerPrivacyPolicy,
                showAttribution = showAttribution,
                onConsentGranted = viewModel::onConsentGranted,
                onConsentDenied = onConsentDenied,
            )

            BvnConsentScreens.BvnInputScreen -> BvnInputScreen(userId = userId)
            BvnConsentScreens.ChooseOtpDeliveryScreen -> ChooseOtpDeliveryScreen(userId = userId)
            BvnConsentScreens.VerifyOtpScreen -> VerifyOtpScreen(
                userId = userId,
                onSuccessfulBvnVerification = onConsentGranted,
            )
        }
    }
}
