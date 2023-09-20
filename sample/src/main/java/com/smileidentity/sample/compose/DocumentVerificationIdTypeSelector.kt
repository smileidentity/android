package com.smileidentity.sample.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smileidentity.models.IdType
import com.smileidentity.models.ValidDocument
import com.smileidentity.sample.R
import com.smileidentity.sample.toast
import com.smileidentity.sample.viewmodel.DocumentSelectorViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import timber.log.Timber

private val OTHERS_ID_TYPE = IdType(code = "", name = "Others", hasBack = true, example = listOf())

/**
 * A composable that allows the user to select a country and ID type for Document Verification.
 *
 * @param onIdTypeSelected A callback that is invoked when the user selects a country and ID type.
 */
@Composable
fun DocumentVerificationIdTypeSelector(
    modifier: Modifier = Modifier,
    viewModel: DocumentSelectorViewModel = viewModel(),
    onIdTypeSelected: (countryCode: String, idType: String?, captureBothSides: Boolean) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var selectedCountry: ValidDocument? by rememberSaveable { mutableStateOf(null) }
    val idTypesForCountry by remember {
        derivedStateOf { selectedCountry?.idTypes?.toImmutableList() }
    }

    uiState.errorMessage?.let {
        LaunchedEffect(it) { context.toast("Error loading ID Types: $it") }
    }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (uiState.idTypes.isEmpty()) {
            CircularProgressIndicator()
            Text(text = stringResource(id = R.string.loading))
            return
        }

        CountrySelector(
            validDocuments = uiState.idTypes,
            selectedCountry = selectedCountry,
            onCountrySelected = { selectedCountry = it },
        )

        idTypesForCountry?.let { idTypesForCountry ->
            IdTypeSelector(idTypesForCountry = idTypesForCountry) {
                onIdTypeSelected(selectedCountry!!.country.name, it.name, it.hasBack)
            }
        }
    }
}

@Suppress("UnusedReceiverParameter")
@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun ColumnScope.CountrySelector(
    validDocuments: ImmutableList<ValidDocument>,
    selectedCountry: ValidDocument?,
    modifier: Modifier = Modifier,
    onCountrySelected: (ValidDocument) -> Unit,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val shape = RoundedCornerShape(8.dp)
    var query by rememberSaveable { mutableStateOf("") }
    var active by rememberSaveable { mutableStateOf(selectedCountry == null) }
    var firstLaunch by rememberSaveable { mutableStateOf(true) }
    val filteredItems by remember(validDocuments) {
        derivedStateOf {
            validDocuments.filter { it.country.name.contains(query, ignoreCase = true) }
        }
    }
    Text(
        text = stringResource(id = R.string.doc_v_select_country),
        fontWeight = FontWeight.Bold,
    )
    DockedSearchBar(
        query = query,
        onQueryChange = {
            query = it.trim()
            Timber.v("Query: $query")
        },
        onSearch = {
            query = it
            keyboardController?.hide()
        },
        active = active,
        onActiveChange = { active = it },
        placeholder = {
            val text = if (active) {
                stringResource(id = R.string.search)
            } else if (selectedCountry?.country?.name != null) {
                selectedCountry.country.name
            } else {
                stringResource(id = R.string.doc_v_country_search_field_hint)
            }
            Text(text)
        },
        leadingIcon = {
            if (selectedCountry == null) {
                Icon(Icons.Default.Search, contentDescription = null)
            } else {
                Icon(Icons.Default.Place, contentDescription = null)
            }
        },
        trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
        shape = shape,
        modifier = modifier
            // Dismiss the keyboard when first made active, to prevent covering the list items
            .onFocusChanged {
                if (firstLaunch && it.isFocused) {
                    keyboardController?.hide()
                    firstLaunch = false
                }
            }
            .imePadding()
            .padding(8.dp),
    ) {
        LazyColumn {
            items(filteredItems) {
                ListItem(
                    headlineContent = { Text(text = it.country.name) },
                    modifier = Modifier.clickable {
                        // Set query to empty so that the search bar shows the placeholder,
                        // which we use to display the selected value
                        query = ""
                        active = false
                        onCountrySelected(it)
                    },
                )
            }
        }
    }
}

@Composable
private fun ColumnScope.IdTypeSelector(
    idTypesForCountry: ImmutableList<IdType>,
    modifier: Modifier = Modifier,
    onIdTypeSelected: (IdType) -> Unit,
) {
    var selectedIdType: IdType? by rememberSaveable { mutableStateOf(null) }
    Text(
        text = stringResource(id = R.string.doc_v_select_id_type),
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
    )
    LazyColumn(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .weight(1f),
    ) {
        items(idTypesForCountry) {
            val selected = selectedIdType == it
            val onClick = { selectedIdType = it }
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
                Text(text = it.name)
            }
        }
    }
    Button(
        // Button is always enabled, because we can default to Others
        onClick = { onIdTypeSelected(selectedIdType ?: OTHERS_ID_TYPE) },
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 0.dp)
            .fillMaxWidth(),
    ) { Text(text = stringResource(id = R.string.cont)) }
}

@Preview
@Composable
fun DocumentVerificationIdTypeSelectorPreview() {
    SmileIDTheme {
        Surface {
            DocumentVerificationIdTypeSelector(
                onIdTypeSelected = { _, _, _ -> },
            )
        }
    }
}
