package com.smileidentity.sample.compose

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.smileidentity.SmileID
import com.smileidentity.sample.BuildConfig
import com.smileidentity.sample.R
import com.smileidentity.sample.SmileIDApplication
import com.smileidentity.sample.isInternetAvailable
import com.smileidentity.sample.toast
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
    viewModel: RootViewModel = viewModel(factory = viewModelFactory { RootViewModel() }),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val runtimeConfig by viewModel.runtimeConfig.collectAsStateWithLifecycle()
    var initialized by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val client = remember {
        SmileID.getOkHttpClientBuilder()
            .addInterceptor(ChuckerInterceptor.Builder(context).build())
            .build()
    }

    SmileIDTheme {
        Surface(modifier = modifier) {
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
            } else {
                WelcomeScreen(
                    partnerId = uiState.partnerId,
                    errorMessage = uiState.smileConfigError,
                    hint = uiState.smileConfigHint,
                    showConfirmation = uiState.showSmileConfigConfirmation,
                    onSaveSmileConfig = {
                        viewModel.updateSmileConfig(it)
                        Toast.makeText(context, "Applying config...", Toast.LENGTH_SHORT).show()
                    },
                    onContinue = viewModel::onConfirmationContinue,
                    modifier = Modifier
                        .fillMaxSize()
                        .navigationBarsPadding(),
                )
            }

            key(runtimeConfig) {
                if (initialized) {
                    MainScreen()
                    LaunchedEffect(Unit) {
                        if (!context.isInternetAvailable()) {
                            context.toast(R.string.warning_no_internet)
                        }
                    }
                }
            }
        }
    }
}

private fun Context.isConfigDefineInAssets(): Boolean {
    return assets.list("")?.contains("smile_config.json") ?: false
}
