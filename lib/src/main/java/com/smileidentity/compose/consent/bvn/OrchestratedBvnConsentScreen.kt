package com.smileidentity.compose.consent.bvn

import android.content.pm.ActivityInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smileidentity.compose.components.ForceOrientation
import com.smileidentity.viewmodel.BvnConsentScreens
import com.smileidentity.viewmodel.BvnConsentViewModel
import com.smileidentity.viewmodel.viewModelFactory

@Composable
internal fun OrchestratedBvnConsentScreen(
    userId: String,
    viewModel: BvnConsentViewModel = viewModel(
        factory = viewModelFactory {
            BvnConsentViewModel(userId = userId)
        },
    ),
    successfulBvnVerification: () -> Unit,
) {
    ForceOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    when (uiState.bvnConsentScreens) {
        BvnConsentScreens.BvnInputScreen -> BvnInputScreen(userId = userId)
        BvnConsentScreens.ChooseOtpDeliveryScreen -> ChooseOtpDeliveryScreen(userId = userId)
        BvnConsentScreens.VerifyOtpScreen -> VerifyOtpScreen(
            userId = userId,
            successfulBvnVerification = successfulBvnVerification,
        )
    }
}
