package com.smileidentity.sample.compose

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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import timber.log.Timber

val countries = listOf(
    SearchableInputFieldItem("Côte d'Ivoire", "🇨🇮"),
    SearchableInputFieldItem("Egypt", "🇪🇬"),
    SearchableInputFieldItem("Ghana", "🇬🇭"),
    SearchableInputFieldItem("Kenya", "🇰🇪"),
    SearchableInputFieldItem("Nigeria", "🇳🇬"),
    SearchableInputFieldItem("Senegal", "🇸🇳"),
    SearchableInputFieldItem("Tanzania", "🇹🇿"),
    SearchableInputFieldItem("Togo", "🇹🇬"),
    SearchableInputFieldItem("Uganda", "🇺🇬"),
)

val idTypes = listOf(
    "National ID",
    "Passport",
    "Voter ID",
)

@Composable
fun DocumentVerificationIdTypeSelector(
    onIdTypeSelected: (String, String) -> Unit,
) {
    var selectedCountry: SearchableInputFieldItem? by rememberSaveable { mutableStateOf(null) }
    var selectedIdType by rememberSaveable { mutableStateOf<String?>(null) }
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
                .verticalScroll(rememberScrollState())
                .weight(1f),
        ) {
            Text("Select Country of Issue")
            SearchableInputField(
                fieldLabel = "Country of Issue",
                selectedItem = selectedCountry,
                unfilteredItems = countries,
                onItemSelected = {
                    Timber.v("Selected: ${it.item}")
                    selectedCountry = it
                    selectedIdType = null
                },
                modifier = Modifier
                    .fillMaxWidth(),
            )
            if (selectedCountry != null) {
                Spacer(Modifier.height(8.dp))
                Text("Select ID Type")
                // radio button for each id type
                idTypes.forEach { idType ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .selectable(
                                selected = selectedIdType == idType,
                                onClick = { selectedIdType = idType },
                                role = Role.RadioButton,
                            )
                            .padding(vertical = 2.dp),
                    ) {
                        RadioButton(
                            selected = selectedIdType == idType,
                            onClick = { selectedIdType = idType },
                        )
                        Text(text = idType)
                    }
                }
            }
        }
        Button(
            onClick = { onIdTypeSelected(selectedCountry!!.item, selectedIdType!!) },
            enabled = isContinueEnabled,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = "Continue")
        }
    }
}
