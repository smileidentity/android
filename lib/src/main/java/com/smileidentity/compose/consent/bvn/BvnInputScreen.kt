package com.smileidentity.compose.consent.bvn

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
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
import com.smileidentity.compose.components.LoadingButton
import com.smileidentity.compose.preview.Preview
import com.smileidentity.compose.preview.SmilePreviews
import com.smileidentity.util.randomUserId
import com.smileidentity.viewmodel.BvnConsentViewModel
import com.smileidentity.viewmodel.bvnNumberLength
import com.smileidentity.viewmodel.viewModelFactory

@Composable
internal fun BvnInputScreen(
    modifier: Modifier = Modifier,
    userId: String = rememberSaveable { randomUserId() },
    viewModel: BvnConsentViewModel = viewModel(
        factory = viewModelFactory {
            BvnConsentViewModel(userId = userId)
        },
    ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusRequester = remember { FocusRequester() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .padding(24.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(id = R.string.si_bvn_enter_bvn_number),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(modifier = Modifier.height(36.dp))
        Text(
            text = stringResource(id = R.string.si_bvn_enter_id_bank_verification),
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = viewModel.bvnNumber,
            onValueChange = {
                if (it.length <= bvnNumberLength) viewModel.updateBvnNumber(it)
            },
            isError = uiState.showError,
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done,
                keyboardType = KeyboardType.NumberPassword,
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    if (uiState.isBvnValid) viewModel.submitUserBvn()
                },
            ),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(
                id = if (uiState.showError) {
                    R.string.si_bvn_enter_id_wrong_bvn
                } else {
                    R.string.si_bvn_enter_bvn_number_limit
                },
            ),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = colorResource(
                id = if (uiState.showError) {
                    R.color.si_color_error
                } else {
                    R.color.si_color_material_on_primary_container
                },
            ),
        )
        Spacer(modifier = Modifier.height(56.dp))
        LoadingButton(
            buttonText = stringResource(id = R.string.si_continue),
            loading = uiState.showLoading,
            enabled = uiState.isBvnValid,
            onClick = viewModel::submitUserBvn,
            modifier = Modifier
                .testTag("bvn_submit_continue_button")
                .fillMaxWidth(),
        )
    }
}

@SmilePreviews
@Composable
private fun BvnInputScreenPreview() {
    Preview {
        BvnInputScreen()
    }
}
