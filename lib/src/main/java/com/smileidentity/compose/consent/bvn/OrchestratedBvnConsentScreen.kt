package com.smileidentity.compose.consent.bvn

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smileidentity.util.randomUserId
import com.smileidentity.viewmodel.BvnConsentScreens
import com.smileidentity.viewmodel.BvnConsentViewModel
import com.smileidentity.viewmodel.viewModelFactory

@Composable
internal fun OrchestratedBvnConsentScreen(
    successfulBvnVerification: () -> Unit,
    userId: String = rememberSaveable { randomUserId() },
    viewModel: BvnConsentViewModel = viewModel(
        factory = viewModelFactory {
            BvnConsentViewModel(userId = userId)
        },
    ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    when (uiState.bvnConsentScreens) {
        BvnConsentScreens.BvnInputScreen -> BvnInputScreen()
        BvnConsentScreens.ChooseOtpDeliveryScreen -> ChooseOtpDeliveryScreen()
        BvnConsentScreens.ShowVerifyOtpScreen -> ShowVerifyOtpScreen(successfulBvnVerification)
        BvnConsentScreens.ShowWrongOtpScreen -> ShowWrongOtpScreen()
    }
}
