package com.smileidentity.sample.compose.components

import android.os.Parcelable
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.smileidentity.sample.R
import com.smileidentity.sample.compose.SmileIDTheme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.parcelize.Parcelize
import timber.log.Timber

@Parcelize
data class SearchableInputFieldItem(
    val key: String,
    val displayName: String,
    val leadingEmoji: String = "",
) : Parcelable

/**
 * A searchable input field that displays a list of items in a dropdown menu.
 *
 * @param fieldLabel The label to display when no item is selected
 * @param selectedItem The currently selected item
 * @param unfilteredItems The list of items to display in the dropdown menu. If null, a loading
 * indicator is displayed.
 * @param onItemSelected The callback to invoke when an item is selected
 * @param modifier The modifier to apply to the [DockedSearchBar]
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun SearchableInputField(
    fieldLabel: String,
    selectedItem: SearchableInputFieldItem?,
    unfilteredItems: ImmutableList<SearchableInputFieldItem>?,
    modifier: Modifier = Modifier,
    onItemSelected: (SearchableInputFieldItem) -> Unit,
) {
    var query by rememberSaveable { mutableStateOf("") }
    var active by rememberSaveable { mutableStateOf(false) }
    var firstLaunch by rememberSaveable { mutableStateOf(true) }
    val filteredItems by remember(unfilteredItems) {
        derivedStateOf {
            unfilteredItems?.filter { it.displayName.contains(query, ignoreCase = true) }
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
            // Dismiss the keyboard when first made active, to prevent covering the list items
            .onFocusChanged {
                if (firstLaunch && it.isFocused) {
                    keyboardController?.hide()
                    firstLaunch = false
                }
            }
            .imePadding()
            .border(1.dp, MaterialTheme.colorScheme.onSurface, shape),
    ) {
        if (filteredItems == null) {
            Text(
                text = stringResource(R.string.loading),
                modifier = Modifier
                    .padding(vertical = 16.dp)
                    .align(alignment = CenterHorizontally),
            )
            CircularProgressIndicator(modifier = Modifier.align(alignment = CenterHorizontally))
            return@DockedSearchBar
        }
        LazyColumn {
            filteredItems?.let { filteredItems ->
                items(filteredItems) {
                    ListItem(
                        leadingContent = { Text(it.leadingEmoji) },
                        headlineContent = { Text(it.displayName) },
                        modifier = Modifier.clickable {
                            // Set query to empty so that the search bar shows the placeholder,
                            // which we use to display the selected value
                            query = ""
                            active = false
                            onItemSelected(it)
                        },
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun SearchableInputFieldPreview() {
    SmileIDTheme {
        SearchableInputField(
            fieldLabel = "Country of Issue",
            selectedItem = null,
            unfilteredItems = persistentListOf(),
            onItemSelected = { },
        )
    }
}
