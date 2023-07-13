package com.smileidentity.sample.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Info
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smileidentity.sample.R
import com.smileidentity.sample.viewmodel.MainScreenViewModel
import com.smileidentity.viewmodel.viewModelFactory

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: MainScreenViewModel = viewModel(
        factory = viewModelFactory { MainScreenViewModel() },
    ),
) {
    val settings = listOf(
        Triple(R.string.about_us_who_we_are, Icons.Default.Info) {
            viewModel.showSmileConfigBottomSheet()
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
            SettingsScreen()
        }
    }
}


