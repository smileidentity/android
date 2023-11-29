package com.smileidentity.sample.compose

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import com.smileidentity.SmileID
import com.smileidentity.compose.components.BottomPinnedColumn
import com.smileidentity.compose.components.annotatedStringResource
import com.smileidentity.sample.BuildConfig
import com.smileidentity.sample.R
import com.smileidentity.sample.SmileIDApplication
import com.smileidentity.sample.compose.components.SmileConfigConfirmationScreen
import com.smileidentity.sample.compose.components.SmileConfigModalBottomSheet
import com.smileidentity.sample.viewmodel.RootViewModel
import com.smileidentity.viewmodel.viewModelFactory

/**
 * *****Note to Partners*****
 *
 * To enable runtime switching of the Smile Config, it is essential to have the RootScreen.
 * For instructions on initializing the SDK, please refer to [SmileIDApplication].
 */
@Composable
fun RootScreen(
    modifier: Modifier = Modifier,
    viewModel: RootViewModel = viewModel(
        factory = viewModelFactory { RootViewModel() },
    ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val runtimeConfig by viewModel.runtimeConfig.collectAsStateWithLifecycle()
    var initialized by remember { mutableStateOf(false) }
    var showBottomSheet by remember { mutableStateOf(false) }
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
    val uriHandler = LocalUriHandler.current
    val scanner = remember(context) { GmsBarcodeScanning.getClient(context, options) }
    val client = remember {
        SmileID.getOkHttpClientBuilder()
            .addInterceptor(ChuckerInterceptor.Builder(context).build())
            .build()
    }
    SmileIDTheme {
        Scaffold(
            modifier = modifier
                .fillMaxSize(),
        ) { paddingValues ->
            BottomPinnedColumn(
                modifier = Modifier.padding(paddingValues),
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
                            color = colorResource(R.color.color_digital_blue),
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
                                uriHandler.openUri("https://portal.smileidentity.com/sdk")
                            }
                        },
                    )
                },
                pinnedContent = {
                    Button(
                        onClick = {
                            scanner.startScan()
                                .addOnSuccessListener { barcode ->
                                    barcode.rawValue?.let { viewModel.updateSmileConfig(it) }
                                }
                        },
                        modifier = Modifier.padding(horizontal = 16.dp),
                    ) {
                        Text(
                            text = stringResource(id = R.string.root_scan_qr),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            textAlign = TextAlign.Center,
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

            if (runtimeConfig != null) {
                // If a config has been set at runtime, it takes first priority
                LaunchedEffect(runtimeConfig) {
                    initialized = false
                    SmileID.initialize(
                        context = context,
                        config = runtimeConfig!!,
                        useSandbox = true,
                        enableCrashReporting = !BuildConfig.DEBUG,
                        okHttpClient = client,
                    )
                    initialized = true
                }
            } else if (context.isConfigDefineInAssets()) {
                // Otherwise, fallback to the config defined in assets
                LaunchedEffect(Unit) {
                    initialized = false
                    SmileID.initialize(
                        context = context,
                        useSandbox = true,
                        enableCrashReporting = !BuildConfig.DEBUG,
                        okHttpClient = client,
                    )
                    initialized = true
                }
            }

            if (showBottomSheet) {
                SmileConfigModalBottomSheet(
                    onSaveSmileConfig = viewModel::updateSmileConfig,
                    onDismiss = { showBottomSheet = false },
                    errorMessage = uiState.smileConfigError,
                    hint = uiState.smileConfigHint,
                    dismissable = false,
                )
            }

            if (uiState.showSmileWelcomeAlert) {
                SmileConfigConfirmationScreen(
                    partnerId = uiState.partnerId,
                    goToMainScreen = { initialized = true },
                )
            }

            key(runtimeConfig) {
                if (initialized) {
                    MainScreen()
                }
            }
        }
    }
}

private fun Context.isConfigDefineInAssets(): Boolean {
    return assets.list("")?.contains("smile_config.json") ?: false
}
