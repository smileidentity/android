package com.smileidentity.sample.compose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smileidentity.compose.components.BottomPinnedColumn
import com.smileidentity.sample.R
import com.smileidentity.sample.viewmodel.IdTypeSelectorAndFieldInputViewModel

@Composable
internal fun IdSelectorScreen(
    modifier: Modifier = Modifier,
    viewModel: IdTypeSelectorAndFieldInputViewModel = viewModel(),
    onNext: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
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
        },
        pinnedContent = {
            Button(
                onClick = onNext,
                enabled = uiState.isIdTypeContinueEnabled,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(text = stringResource(R.string.cont))
            }
        },
        columnWidth = 320.dp,
        modifier = modifier,
    )
}
