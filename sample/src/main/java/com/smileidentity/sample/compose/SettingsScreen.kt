package com.smileidentity.sample.compose

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.Icons.AutoMirrored
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.smileidentity.SmileID
import com.smileidentity.compose.components.BottomPinnedColumn
import com.smileidentity.sample.BuildConfig
import com.smileidentity.sample.R
import com.smileidentity.sample.compose.components.SmileConfigModalBottomSheet
import com.smileidentity.sample.viewmodel.SettingsViewModel
import com.smileidentity.viewmodel.viewModelFactory

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = viewModel(factory = viewModelFactory { SettingsViewModel() }),
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val settings = listOf(
        Triple(
            R.string.settings_update_smile_config,
            Icons.Default.Settings,
            viewModel::showSmileConfigInput,
        ),
    )
    val client = remember {
        SmileID.getOkHttpClientBuilder()
            .addInterceptor(ChuckerInterceptor.Builder(context).build())
            .build()
    }

    if (uiState.showSmileConfigBottomSheet) {
        SmileConfigModalBottomSheet(
            onSaveSmileConfig = {
                val config = viewModel.updateSmileConfig(it)
                // reinitialize SmileID if config is updated
                if (config != null) {
                    SmileID.initialize(
                        context = context,
                        config = config,
                        useSandbox = false,
                        enableCrashReporting = !BuildConfig.DEBUG,
                        okHttpClient = client,
                    )
                }
                Toast.makeText(context, R.string.applying_config, Toast.LENGTH_SHORT).show()
            },
            onDismiss = viewModel::hideSmileConfigInput,
            hint = uiState.smileConfigHint,
            errorMessage = uiState.smileConfigError,
            showQrScannerButton = true,
        )
    }

    BottomPinnedColumn(
        modifier = modifier.fillMaxSize(),
        scrollableContent = {
            settings.forEach {
                ListItem(
                    headlineContent = { Text(stringResource(it.first)) },
                    leadingContent = { Icon(it.second, null) },
                    trailingContent = { Icon(AutoMirrored.Filled.ArrowForward, null) },
                    modifier = Modifier.clickable(onClick = it.third),
                )
                HorizontalDivider()
            }
        },
        pinnedContent = {
            SelectionContainer {
                Text(
                    text = stringResource(
                        R.string.version_info,
                        SmileID.config.partnerId,
                        BuildConfig.VERSION_NAME,
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.typography.labelMedium.color.copy(alpha = .5f),
                )
            }
        },
    )
}

@Preview
@Composable
private fun SettingsScreenPreview() {
    SmileIDTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            SettingsScreen()
        }
    }
}
