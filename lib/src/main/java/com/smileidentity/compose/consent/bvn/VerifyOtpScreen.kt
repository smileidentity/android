package com.smileidentity.compose.consent.bvn

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smileidentity.R
import com.smileidentity.compose.components.BottomPinnedColumn
import com.smileidentity.compose.components.LoadingButton
import com.smileidentity.compose.preview.Preview
import com.smileidentity.compose.preview.SmilePreviews
import com.smileidentity.viewmodel.BvnConsentViewModel
import com.smileidentity.viewmodel.viewModelFactory

@Composable
internal fun VerifyOtpScreen(
    userId: String,
    modifier: Modifier = Modifier,
    viewModel: BvnConsentViewModel = viewModel(
        factory = viewModelFactory {
            BvnConsentViewModel(userId = userId)
        },
    ),
    successfulBvnVerification: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    if (uiState.showSuccess) successfulBvnVerification()

    val focusRequester = remember { FocusRequester() }

    BottomPinnedColumn(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .padding(24.dp),
        scrollableContent = {
            Text(
                textAlign = TextAlign.Center,
                text = stringResource(id = R.string.si_bvn_verification),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
            )
            Spacer(modifier = Modifier.height(32.dp))
            if (uiState.showError) {
                Text(
                    text = stringResource(R.string.si_bvn_verification_invalid_otp),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = colorResource(R.color.si_color_error),
                )
            } else {
                Text(
                    text = stringResource(
                        id = R.string.si_bvn_verification_enter_otp,
                        uiState.selectedBvnOtpVerificationMode?.mode ?: "",
                    ),
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = viewModel.otp,
                onValueChange = viewModel::updateOtp,
                singleLine = true,
                isError = uiState.showError,
                label = {
                    Text(text = stringResource(id = R.string.si_bvn_verification_otp_acronym))
                },
                keyboardOptions = KeyboardOptions(
                    imeAction = if (uiState.isBvnOtpValid) ImeAction.Send else ImeAction.None,
                    // Use NumberPassword instead of Number to prevent entering '.' or ','
                    keyboardType = KeyboardType.NumberPassword,
                ),
                keyboardActions = KeyboardActions(
                    onSend = { viewModel.submitBvnOtp() },
                ),
                modifier = Modifier.focusRequester(focusRequester),
            )
            Spacer(modifier = Modifier.height(32.dp))
            if (uiState.showError) {
                Text(
                    text = stringResource(
                        id = R.string.si_bvn_verification_didnt_receive_otp_at,
                        uiState.selectedBvnOtpVerificationMode?.mode ?: "",
                    ),
                    textAlign = TextAlign.Center,
                )
            } else {
                Text(text = stringResource(id = R.string.si_bvn_verification_didnt_receive_otp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(id = R.string.si_bvn_verification_different_contact_method),
                color = Color.Blue,
                modifier = Modifier.clickable(onClick = viewModel::selectContactMethod),
            )
        },
        pinnedContent = {
            LoadingButton(
                buttonText = stringResource(id = R.string.si_continue),
                loading = uiState.showLoading,
                enabled = uiState.isBvnOtpValid,
                onClick = viewModel::submitBvnOtp,
                modifier = Modifier
                    .testTag("show_verify_otp_continue_button")
                    .fillMaxWidth(),
            )
        },
    )

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}

@SmilePreviews
@Composable
private fun ShowVerifyOtpScreenPreview() {
    Preview {
        VerifyOtpScreen(userId = "") { }
    }
}
