package com.smileidentity.sample.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imeNestedScroll
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults.TrailingIcon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection.Companion.Down
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType.Companion.NumberPassword
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smileidentity.compose.ProcessingScreen
import com.smileidentity.models.IdType
import com.smileidentity.results.EnhancedKycResult
import com.smileidentity.results.SmileIDCallback
import com.smileidentity.sample.R
import com.smileidentity.sample.viewmodel.EnhancedKycViewModel
import com.smileidentity.sample.viewmodel.SupportedCountry

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EnhancedKycScreen(
    viewModel: EnhancedKycViewModel = viewModel(),
    onResult: SmileIDCallback<EnhancedKycResult> = {},
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    if (uiState.processingState != null) {
        ProcessingScreen(
            processingState = uiState.processingState,
            inProgressTitle = stringResource(R.string.enhanced_kyc_processing_title),
            inProgressSubtitle = stringResource(R.string.enhanced_kyc_processing_subtitle),
            inProgressIcon = rememberVectorPainter(Icons.Default.MailOutline),
            successTitle = stringResource(R.string.enhanced_kyc_processing_success_title),
            successSubtitle = stringResource(R.string.enhanced_kyc_processing_success_subtitle),
            successIcon = rememberVectorPainter(Icons.Default.Done),
            errorTitle = stringResource(R.string.enhanced_kyc_processing_error_title),
            errorSubtitle = uiState.errorMessage
                ?: stringResource(R.string.enhanced_kyc_processing_error_subtitle),
            errorIcon = rememberVectorPainter(Icons.Default.Warning),
            continueButtonText = stringResource(R.string.enhanced_kyc_processing_continue_button),
            onContinue = { viewModel.onFinished(onResult) },
            retryButtonText = stringResource(R.string.enhanced_kyc_processing_retry_button),
            onRetry = { viewModel.doEnhancedKyc() },
            closeButtonText = stringResource(R.string.enhanced_kyc_processing_close_button),
            onClose = { viewModel.onFinished(onResult) },
        )
        return
    }
    Column(
        modifier = Modifier
            .imePadding()
            .imeNestedScroll()
            .fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .weight(1f),
            horizontalAlignment = CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                stringResource(R.string.enhanced_kyc_instructions),
                style = MaterialTheme.typography.titleLarge,
            )
            var isCountriesExpanded by rememberSaveable { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                modifier = Modifier.fillMaxWidth(),
                expanded = isCountriesExpanded,
                onExpandedChange = { isCountriesExpanded = !isCountriesExpanded },
            ) {
                OutlinedTextField(
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    value = uiState.selectedCountry?.displayName?.let { stringResource(it) } ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.enhanced_kyc_country_picker_label)) },
                    leadingIcon = { Text(uiState.selectedCountry?.flagEmoji ?: "🌍") },
                    trailingIcon = { TrailingIcon(isCountriesExpanded) },
                )
                ExposedDropdownMenu(
                    expanded = isCountriesExpanded,
                    onDismissRequest = { isCountriesExpanded = false },
                ) {
                    SupportedCountry.values().forEach {
                        DropdownMenuItem(
                            text = { Text(stringResource(it.displayName)) },
                            leadingIcon = { Text(it.flagEmoji) },
                            onClick = {
                                viewModel.onCountrySelected(it)
                                isCountriesExpanded = false
                            },
                        )
                    }
                }
            }

            uiState.selectedCountry?.let {
                var isIdTypesExpanded by rememberSaveable { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    modifier = Modifier.fillMaxWidth(),
                    expanded = isIdTypesExpanded,
                    onExpandedChange = { isIdTypesExpanded = !isIdTypesExpanded },
                ) {
                    val idTypeDisplayName = uiState.selectedIdType?.let {
                        stringResource(viewModel.getIdTypeDisplayName(it))
                    } ?: ""
                    OutlinedTextField(
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        value = idTypeDisplayName,
                        onValueChange = {},
                        readOnly = true,
                        label = {
                            Text(stringResource(R.string.enhanced_kyc_id_type_picker_label))
                        },
                        trailingIcon = { TrailingIcon(isIdTypesExpanded) },
                    )
                    ExposedDropdownMenu(
                        expanded = isIdTypesExpanded,
                        onDismissRequest = { isIdTypesExpanded = false },
                    ) {
                        it.supportedIdTypes.forEach {
                            DropdownMenuItem(
                                text = { Text(stringResource(viewModel.getIdTypeDisplayName(it))) },
                                onClick = {
                                    viewModel.onIdTypeSelected(it)
                                    isIdTypesExpanded = false
                                },
                            )
                        }
                    }
                }
            }

            uiState.selectedIdType?.requiredFields?.forEachIndexed { index, field ->
                val focusManager = LocalFocusManager.current
                val label = stringResource(viewModel.getFieldDisplayName(field))
                val value = uiState.idInputFieldValues[field] ?: ""
                val onValueChange = { it: String -> viewModel.onIdInputFieldChanged(field, it) }
                val keyboardOpts = if (index == uiState.selectedIdType.requiredFields.lastIndex) {
                    KeyboardOptions(imeAction = ImeAction.Done)
                } else {
                    KeyboardOptions(imeAction = ImeAction.Next)
                }
                val keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(Down) },
                    onDone = { focusManager.clearFocus() },
                )
                val modifier = Modifier.fillMaxWidth()
                when (field) {
                    IdType.InputField.IdNumber -> OutlinedTextField(
                        modifier = modifier,
                        label = { Text(label) },
                        value = value,
                        onValueChange = onValueChange,
                        isError = value.isBlank(),
                        singleLine = true,
                        keyboardActions = keyboardActions,
                        keyboardOptions = keyboardOpts,
                    )
                    IdType.InputField.FirstName -> OutlinedTextField(
                        modifier = modifier,
                        label = { Text(label) },
                        value = value,
                        onValueChange = onValueChange,
                        singleLine = true,
                        keyboardActions = keyboardActions,
                        keyboardOptions = keyboardOpts,
                    )
                    IdType.InputField.LastName -> OutlinedTextField(
                        modifier = modifier,
                        label = { Text(label) },
                        value = value,
                        onValueChange = onValueChange,
                        singleLine = true,
                        keyboardActions = keyboardActions,
                        keyboardOptions = keyboardOpts,
                    )
                    IdType.InputField.Dob -> OutlinedTextField(
                        modifier = modifier,
                        label = { Text(label) },
                        value = value,
                        onValueChange = onValueChange,
                        singleLine = true,
                        keyboardActions = keyboardActions,
                        keyboardOptions = keyboardOpts,
                    )
                    IdType.InputField.BankCode -> OutlinedTextField(
                        modifier = modifier,
                        label = { Text(label) },
                        value = value,
                        onValueChange = onValueChange,
                        singleLine = true,
                        keyboardActions = keyboardActions,
                        keyboardOptions = keyboardOpts.copy(keyboardType = NumberPassword),
                    )
                }
            }
        }
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            enabled = viewModel.allInputsSatisfied(),
            onClick = { viewModel.doEnhancedKyc() },
        ) { Text(stringResource(R.string.enhanced_kyc_submit_button)) }
    }
}

@Preview
@Composable
private fun PreviewEnhancedKycScreen() {
    SmileIDTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            EnhancedKycScreen()
        }
    }
}
