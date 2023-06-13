package com.smileidentity.sample.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smileidentity.sample.R
import com.smileidentity.sample.viewmodel.DocumentSelectorViewModel
import com.smileidentity.sample.viewmodel.countryDetails
import com.smileidentity.sample.viewmodel.idTypeFriendlyNames
import timber.log.Timber

/**
 * A composable that allows the user to select a country and ID type for Document Verification.
 *
 * @param onIdTypeSelected A callback that is invoked when the user selects a country and ID type.
 */
@Composable
fun DocumentVerificationIdTypeSelector(
    viewModel: DocumentSelectorViewModel = viewModel(),
    onIdTypeSelected: (String, String) -> Unit,
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    val idTypes = uiState.idTypes

    // If an unsupported country code is passed in, it will display the country code with no emoji
    val countries by remember(idTypes) {
        derivedStateOf {
            idTypes?.keys?.map {
                countryDetails[it] ?: SearchableInputFieldItem(
                    it,
                    it,
                )
            }
        }
    }

    var selectedCountry: String? by rememberSaveable { mutableStateOf(null) }
    var selectedIdType: String? by rememberSaveable { mutableStateOf(null) }
    val isContinueEnabled by remember {
        derivedStateOf { selectedCountry != null && selectedIdType != null }
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.Top),
            modifier = Modifier
                .fillMaxHeight()
                .width(320.dp)
                .verticalScroll(rememberScrollState())
                .weight(1f),
        ) {
            Text(
                text = stringResource(R.string.doc_v_info_title),
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = stringResource(R.string.doc_v_info_subtitle),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Light,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 32.dp),
            )
            Text(text = stringResource(R.string.doc_v_select_country), fontWeight = FontWeight.Bold)
            SearchableInputField(
                fieldLabel = stringResource(R.string.doc_v_country_search_field_hint),
                selectedItem = countryDetails[selectedCountry],
                unfilteredItems = countries,
                onItemSelected = {
                    Timber.v("Selected: ${it.displayName}")
                    selectedCountry = it.key
                    selectedIdType = null
                },
                modifier = Modifier
                    .fillMaxWidth(),
            )

            if (selectedCountry == null) {
                val instructions = listOf(
                    Triple(
                        R.drawable.doc_v_info_government_issued,
                        R.string.doc_v_info_government_issued_title,
                        R.string.doc_v_info_government_issued_subtitle,
                    ),
                    Triple(
                        R.drawable.doc_v_info_encrypted,
                        R.string.doc_v_info_encrypted_title,
                        R.string.doc_v_info_encrypted_subtitle,
                    ),
                )
                Spacer(modifier = Modifier.height(64.dp))
                instructions.forEach { (imageId, title, subtitle) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                    ) {
                        Image(
                            painter = painterResource(id = imageId),
                            contentDescription = null,
                        )
                        Column(modifier = Modifier.padding(start = 16.dp)) {
                            Text(
                                text = stringResource(title),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = stringResource(subtitle),
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                    Spacer(modifier = Modifier.size(24.dp))
                }
            }

            selectedCountry?.let { selectedCountry ->
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.doc_v_select_id_type),
                    fontWeight = FontWeight.Bold,
                )
                val idTypesForCountry = idTypes!![selectedCountry]
                idTypesForCountry?.forEach { idType ->
                    val selected = selectedIdType == idType
                    val onClick = {
                        Timber.v("Selected: $idType")
                        selectedIdType = idType
                    }
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
                        RadioButton(
                            selected = selected,
                            onClick = onClick,
                        )
                        // If ID type ID is not in the friendly name map, display the raw ID type ID
                        Text(text = idTypeFriendlyNames[idType] ?: idType)
                    }
                }
            }
        }
        Button(
            onClick = { onIdTypeSelected(selectedCountry!!, selectedIdType!!) },
            enabled = isContinueEnabled,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = stringResource(R.string.cont))
        }
    }
}
