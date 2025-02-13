package com.smileidentity.sample.compose.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imeNestedScroll
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smileidentity.SmileID
import com.smileidentity.compose.ConsentScreen
import com.smileidentity.compose.components.BottomPinnedColumn
import com.smileidentity.models.ConsentInformation
import com.smileidentity.models.IdInfo
import com.smileidentity.models.JobType
import com.smileidentity.sample.R
import com.smileidentity.sample.toast
import com.smileidentity.sample.viewmodel.IdTypeSelectorAndFieldInputViewModel
import com.smileidentity.viewmodel.viewModelFactory
import java.net.URL
import timber.log.Timber

@Composable
fun IdTypeSelectorAndFieldInputScreen(
    userId: String,
    jobId: String,
    jobType: JobType,
    onConsentDenied: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: IdTypeSelectorAndFieldInputViewModel = viewModel(
        factory = viewModelFactory { IdTypeSelectorAndFieldInputViewModel(jobType) },
    ),
    onResult: (IdInfo, ConsentInformation) -> Unit,
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    Box(
        modifier = modifier
            .windowInsetsPadding(WindowInsets.statusBars)
            .consumeWindowInsets(WindowInsets.statusBars)
            .fillMaxSize(),
    ) {
        when {
            uiState.errorMessage != null -> {
                val context = LocalContext.current
                LaunchedEffect(uiState.errorMessage) {
                    context.toast("Error loading ID Types: ${uiState.errorMessage}")
                }
            }

            !uiState.hasIdTypeSelectionBeenConfirmed -> IdSelectorScreen(
                modifier = Modifier,
                onNext = {
                    viewModel.onIdTypeConfirmed()
                    viewModel.loadConsent(
                        userId = userId,
                        jobId = jobId,
                        idInfo = viewModel.currentIdInfo,
                    )
                },
            )

            uiState.showLoading ->
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))

            uiState.showConsent -> SmileID.ConsentScreen(
                partnerIcon = painterResource(
                    id = com.smileidentity.R.drawable.si_logo_with_text,
                ),
                partnerName = "SmileID",
                productName = "ID",
                partnerPrivacyPolicy = URL("https://usesmileid.com"),
                showAttribution = true,
                modifier = Modifier,
                onConsentGranted = viewModel::onConsentGranted,
                onConsentDenied = { onConsentDenied("User did not consent") },
            )

            else -> IdInputScreen(
                modifier = Modifier,
                onNext = { onResult(viewModel.currentIdInfo, viewModel.currentConsentInformation) },
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun IdInputScreen(
    modifier: Modifier = Modifier,
    viewModel: IdTypeSelectorAndFieldInputViewModel = viewModel(),
    onNext: () -> Unit,
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    if (uiState.idInputFields.isNullOrEmpty()) {
        Timber.e("ID Input Fields are unexpectedly null or empty")
        return
    }

    BottomPinnedColumn(
        scrollableContent = {
            Text(
                text = stringResource(R.string.biometric_kyc_enter_id_info),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 16.dp),
            )

            val focusManager = LocalFocusManager.current
            val focusRequester = remember { FocusRequester() }
            uiState.idInputFields.forEachIndexed { index, fieldUi ->
                val value = uiState.idInputFieldValues[fieldUi.key] ?: ""
                val keyboardOpts = if (index == uiState.idInputFields.lastIndex) {
                    KeyboardOptions(imeAction = ImeAction.Done)
                } else {
                    KeyboardOptions(imeAction = ImeAction.Next)
                }
                val keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) },
                    onDone = { focusManager.clearFocus() },
                )
                OutlinedTextField(
                    label = { Text(fieldUi.label) },
                    value = value,
                    onValueChange = { viewModel.onInputFieldChange(fieldUi.key, it) },
                    isError = !viewModel.isInputValid(value, fieldUi),
                    singleLine = true,
                    keyboardActions = keyboardActions,
                    keyboardOptions = keyboardOpts,
                    modifier = if (index == 0) {
                        Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                    } else {
                        Modifier.fillMaxWidth()
                    },
                )
                // Focus the first input field when the ID type is selected
                LaunchedEffect(key1 = uiState.selectedIdType) {
                    focusRequester.requestFocus()
                }
            }
        },
        pinnedContent = {
            Button(
                onClick = onNext,
                enabled = uiState.isFinalContinueEnabled,
                modifier = Modifier.fillMaxWidth(),
            ) { Text(text = stringResource(R.string.cont)) }
        },
        columnWidth = 320.dp,
        modifier = modifier
            .imePadding()
            .imeNestedScroll(),
    )
}
