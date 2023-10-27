package com.smileidentity.compose.components

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imeNestedScroll
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smileidentity.R
import com.smileidentity.models.IdInfo
import com.smileidentity.models.JobType
import com.smileidentity.util.toast
import com.smileidentity.viewmodel.IdTypeSelectorAndFieldInputViewModel
import com.smileidentity.viewmodel.viewModelFactory
import timber.log.Timber

@Composable
fun IdTypeSelectorAndFieldInputScreen(
    jobType: JobType,
    modifier: Modifier = Modifier,
    viewModel: IdTypeSelectorAndFieldInputViewModel = viewModel(
        factory = viewModelFactory { IdTypeSelectorAndFieldInputViewModel(jobType) },
    ),
    onResult: (IdInfo) -> Unit,
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    when {
        uiState.errorMessage != null -> {
            val context = LocalContext.current
            LaunchedEffect(uiState.errorMessage) {
                context.toast("Error loading ID Types: ${uiState.errorMessage}")
            }
        }

        !uiState.hasIdTypeSelectionBeenConfirmed -> IdSelectorScreen(
            modifier = modifier,
            onNext = { viewModel.onIdTypeConfirmed() },
        )

        else -> IdInputScreen(
            modifier = modifier,
            onNext = { onResult(viewModel.currentIdInfo) },
        )
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
                text = stringResource(R.string.si_biometric_kyc_enter_id_info),
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
            ) { Text(text = stringResource(R.string.si_continue)) }
        },
        columnWidth = 320.dp,
        modifier = modifier
            .imePadding()
            .imeNestedScroll(),
    )
}
