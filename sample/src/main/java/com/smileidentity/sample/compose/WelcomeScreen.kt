package com.smileidentity.sample.compose

import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import com.smileidentity.compose.components.BottomPinnedColumn
import com.smileidentity.compose.components.annotatedStringResource
import com.smileidentity.sample.R
import com.smileidentity.sample.compose.components.SmileConfigConfirmationScreen
import com.smileidentity.sample.compose.components.SmileConfigModalBottomSheet

@Composable
fun WelcomeScreen(
    partnerId: String,
    @StringRes errorMessage: Int?,
    hint: String,
    showConfirmation: Boolean,
    onSaveSmileConfig: (updatedConfig: String) -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
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
    var showBottomSheet by remember { mutableStateOf(false) }

    if (showBottomSheet) {
        SmileConfigModalBottomSheet(
            onSaveSmileConfig = onSaveSmileConfig,
            onDismiss = { showBottomSheet = false },
            errorMessage = errorMessage,
            showQrScannerButton = false,
            hint = hint,
            dismissable = true,
        )
    }

    if (showConfirmation) {
        SmileConfigConfirmationScreen(
            partnerId = partnerId,
            onConfirm = onContinue,
        )
    }

    BottomPinnedColumn(
        modifier = modifier,
        scrollableContent = {
            Image(
                painter = painterResource(id = R.drawable.ic_smile_logo),
                contentDescription = "",
            )
            Text(
                text = stringResource(id = R.string.root_welcome),
                modifier = Modifier.padding(horizontal = 16.dp),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 32.sp,
                    lineHeight = 40.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = colorResource(R.color.si_color_accent),
                ),
            )
            Spacer(modifier = Modifier.height(46.dp))
            val annotatedText = annotatedStringResource(
                id = R.string.root_description,
                spanStyles = { annotation ->
                    when (annotation.key) {
                        "is_url" -> SpanStyle(color = Color.Blue)
                        else -> null
                    }
                },
            )
            ClickableText(
                text = annotatedText,
                modifier = Modifier.padding(horizontal = 16.dp),
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 16.sp),
                onClick = { offset ->
                    annotatedText.getStringAnnotations(
                        tag = "is_url",
                        start = offset,
                        end = offset,
                    ).firstOrNull()?.let {
                        uriHandler.openUri("https://portal.usesmileid.com/sdk")
                    }
                },
            )
        },
        pinnedContent = {
            Button(
                onClick = {
                    scanner.startScan().addOnSuccessListener { barcode ->
                        barcode.rawValue?.let { config -> onSaveSmileConfig(config) }
                    }
                },
                modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_qr_code),
                    contentDescription = null,
                    modifier = Modifier.size(ButtonDefaults.IconSize),
                )
                Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                Text(
                    text = stringResource(id = R.string.root_scan_qr),
                    modifier = Modifier.padding(8.dp),
                )
            }
            OutlinedButton(
                onClick = { showBottomSheet = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            ) {
                Text(
                    text = stringResource(R.string.root_add_config),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    textAlign = TextAlign.Center,
                )
            }
        },
    )
}
