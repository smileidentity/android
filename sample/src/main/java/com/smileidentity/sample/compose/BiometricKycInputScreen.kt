package com.smileidentity.sample.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imeNestedScroll
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smileidentity.compose.components.BottomPinnedColumn
import com.smileidentity.models.IdInfo
import com.smileidentity.sample.R
import com.smileidentity.sample.toast
import com.smileidentity.sample.viewmodel.BiometricKycInputViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BiometricKycInputScreen(
    modifier: Modifier = Modifier,
    viewModel: BiometricKycInputViewModel = viewModel(),
    onResult: (IdInfo) -> Unit,
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value

    uiState.errorMessage?.let {
        val context = LocalContext.current
        LaunchedEffect(it) { context.toast("Error loading ID Types: $it") }
    }

    BottomPinnedColumn(
        scrollableContent = {
            Text(
                text = stringResource(R.string.biometric_kyc_instructions),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(vertical = 16.dp),
            )
            SearchableInputField(
                fieldLabel = stringResource(R.string.doc_v_country_search_field_hint),
                selectedItem = uiState.selectedCountry,
                unfilteredItems = uiState.countries,
            ) { viewModel.onCountrySelected(it) }

            uiState.idTypesForCountry?.let { idTypesForCountry ->
                Text(
                    text = stringResource(R.string.doc_v_select_id_type),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 16.dp),
                )

                idTypesForCountry.forEach {
                    val selected = uiState.selectedIdType == it
                    val onClick = { viewModel.onIdTypeSelected(it) }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .selectable(
                                selected = selected,
                                onClick = onClick,
                                role = Role.RadioButton,
                            )
                            .padding(vertical = 2.dp),
                    ) {
                        RadioButton(selected = selected, onClick = onClick)
                        Text(text = it.label)
                    }
                }
            }

            uiState.idInputFields?.let {
                Text(
                    text = stringResource(R.string.biometric_kyc_enter_id_info),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 16.dp),
                )
            }

            val focusManager = LocalFocusManager.current
            val focusRequester = remember { FocusRequester() }
            uiState.idInputFields?.forEachIndexed { index, fieldUi ->
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
                    isError = value.isBlank(),
                    singleLine = true,
                    keyboardActions = keyboardActions,
                    keyboardOptions = keyboardOpts,
                    modifier = if (index == 0) {
                        Modifier.fillMaxWidth().focusRequester(focusRequester)
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
                onClick = { onResult(viewModel.currentIdInfo) },
                enabled = uiState.isContinueEnabled,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(text = stringResource(R.string.cont))
            }
        },
        columnWidth = 320.dp,
        modifier = modifier
            .imePadding()
            .imeNestedScroll(),
    )
}
