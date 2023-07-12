package com.smileidentity.compose.consent.bvn

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smileidentity.R
import com.smileidentity.compose.components.SmileIDAttribution
import com.smileidentity.compose.preview.Preview
import com.smileidentity.compose.preview.SmilePreviews
import com.smileidentity.viewmodel.BvnConsentViewModel
import com.smileidentity.viewmodel.viewModelFactory

@Composable
fun BvnInputScreen(
    cancelBvnVerification: () -> Unit,
    onBvnVerified: () -> Unit,
    modifier: Modifier = Modifier,
    showAttribution: Boolean = true,
    viewModel: BvnConsentViewModel = viewModel(
        factory = viewModelFactory {
            BvnConsentViewModel()
        },
    ),
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    var bvn by rememberSaveable { mutableStateOf("") }

    val focusRequester = remember { FocusRequester() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(id = R.string.si_bvn_enter_id_title),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.weight(1f),
            )
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Cancel",
                modifier = Modifier
                    .testTag("bvn_input_screen_cancel")
                    .clickable { cancelBvnVerification() },
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(id = R.string.si_bvn_enter_id_bank_verification),
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = bvn,
            onValueChange = { bvn = it },
            isError = uiState.showWrongBvn,
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        )
        AnimatedVisibility(visible = uiState.showWrongBvn) {
            Text(
                text = stringResource(id = R.string.si_bvn_enter_id_wrong_bvn),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = colorResource(id = R.color.si_color_error),
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                viewModel.handleEvent(BvnConsentEvent.SubmitBVNMode(bvn = bvn))
                // TODO - Some notes here
                // I was thinking of using state to navigate instead of callbacks
            },
            modifier = Modifier
                .testTag("bvn_submit_continue_button")
                .fillMaxWidth(),
        ) {
            Text(text = stringResource(id = R.string.si_continue))
        }
        if (showAttribution) {
            SmileIDAttribution()
        }
    }
}

@SmilePreviews
@Composable
private fun BvnInputScreenPreview() {
    Preview {
        BvnInputScreen(
            cancelBvnVerification = {},
            onBvnVerified = {},
        )
    }
}
