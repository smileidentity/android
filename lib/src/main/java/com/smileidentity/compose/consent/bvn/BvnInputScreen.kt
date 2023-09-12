package com.smileidentity.compose.consent.bvn

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
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.testTag
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

@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun BvnInputScreen(
    userId: String,
    modifier: Modifier = Modifier,
    viewModel: BvnConsentViewModel = viewModel(
        factory = viewModelFactory {
            BvnConsentViewModel(userId = userId)
        },
    ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusRequester = remember { FocusRequester() }

    BottomPinnedColumn(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .padding(16.dp),
        scrollableContent = {
            Text(
                text = stringResource(id = R.string.si_bvn_enter_bvn_number),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(36.dp))
            Text(text = stringResource(id = R.string.si_bvn_enter_id_bank_verification))
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = viewModel.bvnNumber,
                onValueChange = viewModel::updateBvnNumber,
                isError = uiState.showError,
                singleLine = true,
                label = { Text(text = stringResource(id = R.string.si_bvn_acronym)) },
                supportingText = {
                    val id = if (uiState.showError) {
                        R.string.si_bvn_enter_id_wrong_bvn
                    } else {
                        R.string.si_bvn_enter_bvn_number_limit
                    }
                    Text(text = stringResource(id = id))
                },
                keyboardOptions = KeyboardOptions(
                    imeAction = if (uiState.isBvnValid) ImeAction.Send else ImeAction.None,
                    // Use NumberPassword instead of Number to prevent entering '.' or ','
                    keyboardType = KeyboardType.NumberPassword,
                ),
                keyboardActions = KeyboardActions(
                    onSend = { viewModel.submitUserBvn() },
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
            )
        },
        pinnedContent = {
            LoadingButton(
                buttonText = stringResource(id = R.string.si_continue),
                loading = uiState.showLoading,
                enabled = uiState.isBvnValid,
                onClick = viewModel::submitUserBvn,
                modifier = Modifier
                    .testTag("bvn_submit_continue_button")
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
private fun BvnInputScreenPreview() {
    Preview {
        BvnInputScreen(userId = "")
    }
}
