package com.smileidentity.sample.compose.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import com.smileidentity.sample.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmileConfigModalBottomSheet(
    onSaveSmileConfig: (updatedConfig: String) -> Unit,
    onDismiss: () -> Unit,
    hint: String,
    modifier: Modifier = Modifier,
    showQrScannerButton: Boolean = true,
    @StringRes errorMessage: Int? = null,
    dismissable: Boolean = true,
) {
    var configInput by remember { mutableStateOf("") }
    val context = LocalContext.current
    val options = remember {
        GmsBarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_QR_CODE,
                Barcode.FORMAT_AZTEC,
            )
            .enableAutoZoom()
            .build()
    }
    val scanner = remember(context) { GmsBarcodeScanning.getClient(context, options) }

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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(id = R.string.settings_add_smile_config),
                textAlign = TextAlign.Start,
                modifier = Modifier
                    .fillMaxWidth(),
            )
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
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onSaveSmileConfig(configInput) },
            ) {
                Text(stringResource(id = R.string.settings_update_smile_config))
            }
            if (showQrScannerButton) {
                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        scanner.startScan()
                            .addOnSuccessListener { barcode ->
                                barcode.rawValue?.let { onSaveSmileConfig(it) }
                            }
                    },
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_qr_code),
                        contentDescription = null,
                        modifier = Modifier.size(ButtonDefaults.IconSize),
                    )
                    Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                    Text(text = stringResource(id = R.string.scan_qr_code))
                }
            }
        }
    }
}
