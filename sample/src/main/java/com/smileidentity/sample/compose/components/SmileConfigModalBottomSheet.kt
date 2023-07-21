package com.smileidentity.sample.compose.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.smileidentity.sample.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmileConfigModalBottomSheet(
    onSaveSmileConfig: (updatedConfig: String) -> Unit,
    onDismiss: () -> Unit,
    hint: String,
    modifier: Modifier = Modifier,
    @StringRes errorMessage: Int? = null,
    dismissable: Boolean = true,
) {
    var configInput by remember { mutableStateOf("") }

    ModalBottomSheet(
        sheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = true,
            confirmValueChange = { dismissable },
        ),
        onDismissRequest = onDismiss,
        modifier = modifier,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth(),
                value = configInput,
                onValueChange = { configInput = it },
                placeholder = { Text(hint, style = MaterialTheme.typography.bodySmall) },
                isError = errorMessage != null,
                supportingText = {
                    if (errorMessage != null) {
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = stringResource(id = errorMessage),
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                minLines = 8,
                maxLines = 12,
                textStyle = MaterialTheme.typography.bodySmall,
            )
            Spacer(modifier = Modifier.size(16.dp))
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onSaveSmileConfig(configInput) },
            ) {
                Text(stringResource(id = R.string.settings_update_smile_config))
            }
        }
    }
}
