package com.smileidentity.sample.compose

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
import kotlinx.coroutines.delay

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
    val context = LocalContext.current
    val client = remember {
        SmileID.getOkHttpClientBuilder()
            .addInterceptor(ChuckerInterceptor.Builder(context).build())
            .build()
    }

    val initializationState by produceState<InitializationState>(
        InitializationState.NotInitialized,
    ) {
        delay(1000)
        when {
            runtimeConfig != null -> {
                SmileID.initialize(
                    context = context,
                    config = runtimeConfig!!,
                    useSandbox = false,
                    enableCrashReporting = !BuildConfig.DEBUG,
                    okHttpClient = client,
                )
            }
            context.isConfigDefineInAssets() -> {
                SmileID.initialize(
                    context = context,
                    useSandbox = false,
                    enableCrashReporting = !BuildConfig.DEBUG,
                    okHttpClient = client,
                )
            }
            else -> {
                value = InitializationState.NoConfig
                return@produceState
            }
        }
        value = InitializationState.Initialized
    }

    SmileIDTheme {
        Surface(modifier = modifier) {
            when (initializationState) {
                InitializationState.Initialized -> {
                    MainScreen()
                    LaunchedEffect(Unit) {
                        if (!context.isInternetAvailable()) {
                            context.toast(R.string.warning_no_internet)
                        }
                    }
                }
                InitializationState.NoConfig -> {
                    WelcomeScreen(
                        partnerId = uiState.partnerId,
                        errorMessage = uiState.smileConfigError,
                        hint = uiState.smileConfigHint,
                        showConfirmation = uiState.showSmileConfigConfirmation,
                        onSaveSmileConfig = {
                            viewModel.updateSmileConfig(it)
                            Toast.makeText(
                                context,
                                R.string.applying_config,
                                Toast.LENGTH_SHORT,
                            ).show()
                        },
                        onContinue = viewModel::onConfirmationContinue,
                        modifier = Modifier
                            .fillMaxSize()
                            .navigationBarsPadding(),
                    )
                }
                InitializationState.NotInitialized -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .navigationBarsPadding(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}

private fun Context.isConfigDefineInAssets(): Boolean {
    return assets.list("")?.contains("smile_config.json") ?: false
}

private sealed class InitializationState {
    data object NotInitialized : InitializationState()
    data object Initialized : InitializationState()
    data object NoConfig : InitializationState()
}
