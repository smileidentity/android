package com.smileidentity.compose.consent.bvn

import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smileidentity.viewmodel.BvnConsentScreens
import com.smileidentity.viewmodel.BvnConsentViewModel
import com.smileidentity.viewmodel.viewModelFactory

@Composable
internal fun OrchestratedBvnConsentScreen(
    cancelBvnVerification: () -> Unit,
    successfulBvnVerification: () -> Unit,
    viewModel: BvnConsentViewModel = viewModel(
        factory = viewModelFactory {
            BvnConsentViewModel()
        },
    ),
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    when (uiState.bvnConsentScreens) {
        BvnConsentScreens.BvnInputScreen -> BvnInputScreen(cancelBvnVerification)
        BvnConsentScreens.ChooseOtpDeliveryScreen -> ChooseOtpDeliveryScreen()
        BvnConsentScreens.ShowVerifyOtpScreen -> ShowVerifyOtpScreen(successfulBvnVerification)
        BvnConsentScreens.ShowWrongOtpScreen -> ShowWrongOtpScreen()
    }
}
