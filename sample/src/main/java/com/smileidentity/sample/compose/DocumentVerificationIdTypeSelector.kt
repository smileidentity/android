package com.smileidentity.sample.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smileidentity.sample.R
import com.smileidentity.sample.toast
import com.smileidentity.sample.viewmodel.DocumentSelectorViewModel

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

    var selectedCountry: String? by rememberSaveable { mutableStateOf(null) }
    var selectedIdType: String? by rememberSaveable { mutableStateOf(null) }
    var captureBothSides: Boolean by rememberSaveable { mutableStateOf(true) }
    val isContinueEnabled by remember {
        derivedStateOf { selectedCountry != null && selectedIdType != null }
    }

    uiState.errorMessage?.let {
        LaunchedEffect(it) { context.toast("Error loading ID Types: $it") }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        if (selectedCountry == null) {
            LazyColumn {
                itemsIndexed(uiState.idTypes) { index, item ->
                    Text(
                        text = item.country.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .clickable { selectedCountry = item.country.name },
                    )
                    if (index < uiState.idTypes.lastIndex) {
                        Divider(
                            color = colorResource(id = R.color.si_color_on_light),
                            thickness = 0.5.dp,
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize(),
        ) {
            selectedCountry?.let { selectedCountry ->
                Text(
                    text = stringResource(R.string.doc_v_select_id_type),
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(16.dp))
                uiState.idTypes.filter { it.country.name == selectedCountry }
                    .flatMap { it.idTypes }
                    .forEach { idType ->
                        val selected = selectedIdType == idType.name
                        val onClick = {
                            selectedIdType = idType.name
                            captureBothSides = idType.hasBack
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
                            Text(text = idType.name)
                        }
                    }
                Button(
                    onClick = {
                        onIdTypeSelected(
                            selectedCountry,
                            selectedIdType,
                            captureBothSides,
                        )
                    },
                    enabled = isContinueEnabled,
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                ) {
                    Text(text = stringResource(R.string.cont))
                }
            }
        }

        if (uiState.idTypes.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(R.string.loading),
                    modifier = Modifier
                        .padding(vertical = 16.dp),
                )
                CircularProgressIndicator()
            }
        }
    }
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
