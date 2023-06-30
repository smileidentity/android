package com.smileidentity.consent.bvn

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smileidentity.R
import com.smileidentity.compose.SmileIDAttribution
import com.smileidentity.compose.preview.Preview
import com.smileidentity.compose.preview.SmilePreviews
import com.smileidentity.viewmodel.BvnConsentViewModel
import com.smileidentity.viewmodel.viewModelFactory

@Composable
fun BvnInputScreen(
    cancelBvnVerification: () -> Unit, // TODO - Do we make this a screen and not a AlertDialog?
    // then we can add the x icon?
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

    AlertDialog(
        title = {
            Text(
                text = stringResource(id = R.string.si_bvn_enter_id_title),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.Start,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = stringResource(id = R.string.si_bvn_enter_id_bank_verification),
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(modifier = Modifier.height(8.dp))
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
        },
        onDismissRequest = { /* Do nothing since we have disabled back press and click outside */ },
        confirmButton = {},
        dismissButton = {},
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
        modifier = modifier.testTag("bvn_id_information_dialog"),
    )
}

@SmilePreviews
@Composable
private fun BvnInputScreenPreview() {
    Preview {
        BvnInputScreen(
            cancelBvnVerification = {},
        )
    }
}
