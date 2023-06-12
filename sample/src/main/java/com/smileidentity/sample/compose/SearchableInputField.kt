package com.smileidentity.sample.compose

import android.os.Parcelable
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.smileidentity.SmileID
import com.smileidentity.compose.theme.colorScheme
import com.smileidentity.compose.theme.typography
import kotlinx.parcelize.Parcelize
import timber.log.Timber

@Parcelize
data class SearchableInputFieldItem(
    val key: String,
    val displayName: String,
    val leadingEmoji: String = "",
) : Parcelable

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun SearchableInputField(
    fieldLabel: String,
    selectedItem: SearchableInputFieldItem?,
    unfilteredItems: List<SearchableInputFieldItem>,
    onItemSelected: (SearchableInputFieldItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    var query by rememberSaveable { mutableStateOf("") }
    var active by rememberSaveable { mutableStateOf(false) }
    val filteredItems by remember(unfilteredItems) {
        derivedStateOf {
            unfilteredItems.filter { it.displayName.contains(query, ignoreCase = true) }
        }
    }
    val keyboardController = LocalSoftwareKeyboardController.current
    val shape = RoundedCornerShape(8.dp)
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
        // The field label when nothing is selected, otherwise the selected value
        placeholder = { Text(selectedItem?.displayName ?: fieldLabel) },
        leadingIcon = {
            if (selectedItem == null) {
                Icon(Icons.Default.Search, contentDescription = null)
            } else {
                Text(selectedItem.leadingEmoji)
            }
        },
        trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
        shape = shape,
        modifier = modifier
            .imePadding()
            .border(1.dp, MaterialTheme.colorScheme.onSurface, shape),
    ) {
        LazyColumn {
            items(filteredItems) {
                ListItem(
                    leadingContent = { Text(it.leadingEmoji) },
                    headlineContent = { Text(it.displayName) },
                    trailingContent = {
                        Icon(
                            Icons.Outlined.CheckCircle,
                            contentDescription = null,
                        )
                    },
                    modifier = Modifier.clickable {
                        // Set query to empty so that the search bar shows the placeholder, which we
                        // use to display the selected value
                        query = ""
                        active = false
                        onItemSelected(it)
                    },
                )
            }
        }
    }
}

@Preview
@Composable
private fun SearchableInputFieldPreview() {
    MaterialTheme(colorScheme = SmileID.colorScheme, typography = SmileID.typography) {
        SearchableInputField(
            fieldLabel = "Country of Issue",
            selectedItem = null,
            unfilteredItems = countryDetails.values.toList(),
            onItemSelected = { },
        )
    }
}
