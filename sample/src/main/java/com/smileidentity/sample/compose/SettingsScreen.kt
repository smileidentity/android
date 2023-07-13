package com.smileidentity.sample.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.smileidentity.sample.R
import com.smileidentity.sample.viewmodel.MainScreenViewModel

@Composable
fun SettingsScreen(
    viewModel: MainScreenViewModel,
    modifier: Modifier = Modifier,
) {
    val settings = listOf(
        Triple(R.string.settings_show_smile_config, Icons.Default.Settings) {
            viewModel.showSmileConfigBottomSheet(shouldShowSmileConfigBottomSheet = true)
        },
    )
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        settings.forEach {
            ListItem(
                headlineContent = { Text(stringResource(it.first)) },
                leadingContent = { Icon(it.second, null) },
                trailingContent = { Icon(Icons.Default.ArrowForward, null) },
                modifier = Modifier.clickable { it.third() },
            )
            Divider()
        }
    }
}

@Preview
@Composable
private fun PreviewAboutUsScreen() {
    SmileIDTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            // SettingsScreen()
        }
    }
}


