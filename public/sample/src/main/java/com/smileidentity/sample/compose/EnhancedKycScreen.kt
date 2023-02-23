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
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults.TrailingIcon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection.Companion.Down
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType.Companion.NumberPassword
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smileidentity.networking.models.IdType
import com.smileidentity.sample.viewmodel.EnhancedKycViewModel
import com.smileidentity.sample.viewmodel.SupportedCountry
import com.smileidentity.ui.R
import com.smileidentity.ui.compose.ProcessingScreen
import com.smileidentity.ui.core.EnhancedKycResult

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EnhancedKycScreen(
    viewModel: EnhancedKycViewModel = viewModel(),
    onResult: EnhancedKycResult.Callback = EnhancedKycResult.Callback {},
) {
    val uiState = viewModel.uiState.collectAsState().value
    if (uiState.isWaitingForResult) {
        ProcessingScreen(textRes = R.string.si_enhanced_kyc_processing)
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
                stringResource(R.string.si_enhanced_kyc_instructions),
                style = MaterialTheme.typography.titleLarge,
            )
            var isCountriesExpanded by remember { mutableStateOf(false) }
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
                    label = { Text(stringResource(R.string.si_enhanced_kyc_country_picker_label)) },
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
                var isIdTypesExpanded by remember { mutableStateOf(false) }
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
                            Text(stringResource(R.string.si_enhanced_kyc_id_type_picker_label))
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
                        isError = value.isNotBlank() && !uiState.selectedIdType.isValidIdNumber(
                            value,
                        ),
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
            onClick = { viewModel.doEnhancedKyc(callback = onResult) },
        ) { Text(stringResource(R.string.si_enhanced_kyc_submit_button)) }
    }
}
