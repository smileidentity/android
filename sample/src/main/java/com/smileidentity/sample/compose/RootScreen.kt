package com.smileidentity.sample.compose

import android.content.Context
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
import com.smileidentity.sample.compose.components.SmileConfigModalBottomSheet
import com.smileidentity.sample.viewmodel.RootViewModel
import com.smileidentity.viewmodel.viewModelFactory
import timber.log.Timber

@Composable
fun RootScreen(
    modifier: Modifier = Modifier,
    viewModel: RootViewModel = viewModel(
        factory = viewModelFactory { RootViewModel() },
    ),
) {
    SmileIDTheme {
        val runtimeConfig by viewModel.runtimeConfig.collectAsStateWithLifecycle()
        var initialized by remember { mutableStateOf(false) }
        val context = LocalContext.current
        val client = remember {
            SmileID.getOkHttpClientBuilder()
                .addInterceptor(ChuckerInterceptor.Builder(context).build())
                .build()
        }

        if (runtimeConfig != null) {
            // If a config has been set at runtime, it takes first priority
            LaunchedEffect(viewModel.runtimeConfig) {
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
            // Otherwise, ask the user to input a config. Once input, the runtimeConfig will
            // be updated and the LaunchedEffect above will be triggered.
            var errorMessage: Int? by remember { mutableStateOf(null) }
            SmileConfigModalBottomSheet(
                onSaveSmileConfig = viewModel::updateSmileConfig,
                onDismiss = { Timber.v("onDismiss") },
                errorMessage = errorMessage,
                hint = "Paste your config from the Portal here",
                dismissable = false,
            )
        }

        key(runtimeConfig) {
            if (initialized) {
                MainScreen()
            }
        }
    }
}

private fun Context.isConfigDefineInAssets(): Boolean {
    return assets.list("")?.contains("smile_config.json") ?: false
}
