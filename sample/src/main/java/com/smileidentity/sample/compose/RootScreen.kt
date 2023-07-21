package com.smileidentity.sample.compose

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.smileidentity.SmileID
import com.smileidentity.models.Config
import com.smileidentity.sample.BuildConfig
import com.smileidentity.sample.R
import com.smileidentity.sample.compose.components.SmileConfigModalBottomSheet
import com.smileidentity.sample.repo.DataStoreRepository
import kotlinx.coroutines.launch
import timber.log.Timber

@Composable
fun RootScreen(
    modifier: Modifier = Modifier,
) {
    var initialized by remember { mutableStateOf(false) }
    val runtimeConfig by DataStoreRepository.getConfig().collectAsStateWithLifecycle(null)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val client = remember {
        SmileID.getOkHttpClientBuilder()
            .addInterceptor(ChuckerInterceptor.Builder(context).build())
            .build()
    }

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
        // Otherwise, ask the user to input a config. Once input, the runtimeConfig will
        // be updated and the LaunchedEffect above will be triggered.

        // TODO: Fix behavior when the user dismisses the modal without inputting a config.
        //  Maybe AlertDialog better for this?? (even for settings screen)
        var errorMessage: Int? by remember { mutableStateOf(null) }
        SmileConfigModalBottomSheet(
            onSaveSmileConfig = {
                try {
                    val config = SmileID.moshi.adapter(Config::class.java).fromJson(it)
                    if (config != null) {
                        scope.launch { DataStoreRepository.setConfig(config) }
                    } else {
                        errorMessage = R.string.settings_smile_config_error
                    }
                } catch (e: Exception) {
                    errorMessage = R.string.settings_smile_config_error
                }
            },
            onDismiss = { Timber.v("onDismiss") },
            errorMessage = errorMessage,
            hint = "Paste your config from the Portal here",
            canDismissConfigSheet = false,
        )
    }

    key(runtimeConfig) {
        if (initialized) {
            MainScreen()
        }
    }
}

private fun Context.isConfigDefineInAssets(): Boolean {
    return assets.list("")?.contains("smile_config.json") ?: false
}
