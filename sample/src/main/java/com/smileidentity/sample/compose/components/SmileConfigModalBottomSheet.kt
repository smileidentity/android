package com.smileidentity.sample.compose.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.jakewharton.processphoenix.ProcessPhoenix
import com.smileidentity.SmileID
import com.smileidentity.models.Config
import com.smileidentity.sample.R
import com.smileidentity.sample.SmileIDApplication
import kotlinx.coroutines.launch
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmileConfigModalBottomSheet(
    shouldShowSmileConfigBottomSheet: (shouldDismiss: Boolean) -> Unit,
    updateSmileConfig: (updatedConfig: String) -> Boolean,
    modifier: Modifier = Modifier,
    setSmileConfig: String = "",
    smileConfig: Config? = null,
) {
    val coroutineScope = rememberCoroutineScope()
    var showConfigError by rememberSaveable { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var stringConfig by remember { mutableStateOf(setSmileConfig) }
    var config by remember { mutableStateOf(smileConfig) }

    ModalBottomSheet(
        modifier = modifier,
        onDismissRequest = {
            shouldShowSmileConfigBottomSheet(false)
        },
        sheetState = sheetState,
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
                shape = RoundedCornerShape(4.dp),
                value = stringConfig,
                onValueChange = { stringConfig = it },
                isError = showConfigError,
                supportingText = {
                    if (showConfigError) {
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = stringResource(id = R.string.settings_smile_config_error),
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                minLines = 10,
                maxLines = 15,
                textStyle = MaterialTheme.typography.bodyMedium,
            )
            Spacer(modifier = Modifier.size(16.dp))
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    coroutineScope.launch {
                        val updatedConfig = updateSmileConfig(stringConfig)
                        if (updatedConfig) {
                            sheetState.hide()
                            shouldShowSmileConfigBottomSheet(false)
                            setConfigAndRestart(smileConfig = config)
                        } else {
                            showConfigError = true
                        }
                    }
                },
            ) {
                Text(stringResource(id = R.string.settings_show_smile_config))
            }
        }
    }
}

fun setConfigAndRestart(smileConfig: Config?) {
    smileConfig?.let { config ->
        SmileID.config = config
        SmileID.isInitialized = true
        // Restart Process
        ProcessPhoenix.triggerRebirth(SmileIDApplication.appContext)
    }
}