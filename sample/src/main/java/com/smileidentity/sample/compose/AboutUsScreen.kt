package com.smileidentity.sample.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.smileidentity.sample.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutUsScreen() {
    var shouldShowWhoWeAreDialog by remember { mutableStateOf(false) }
    val uriHandler = LocalUriHandler.current
    val abouts = listOf(
        Triple(R.string.about_us_who_we_are, Icons.Default.Info) {
            shouldShowWhoWeAreDialog = true
        },
        Triple(R.string.about_us_visit_our_website, Icons.Default.Star) {
            uriHandler.openUri("https://smileidentity.com")
        },
        Triple(R.string.about_us_contact_support, Icons.Default.Email) {
            uriHandler.openUri("https://smileidentity.com/contact-us")
        },
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            // .padding(16.dp)
            // .clip(MaterialTheme.shapes.small)
            .verticalScroll(rememberScrollState()),
    ) {
        abouts.forEach {
            ListItem(
                headlineText = { Text(stringResource(it.first)) },
                leadingContent = { Icon(it.second, null) },
                trailingContent = { Icon(Icons.Default.ArrowForward, null) },
                modifier = Modifier.clickable { it.third() },
            )
            Divider()
        }
    }

    if (shouldShowWhoWeAreDialog) {
        WhoWeAreDialog { shouldShowWhoWeAreDialog = false }
    }
}

@Composable
fun WhoWeAreDialog(onDialogClose: () -> Unit = {}) {
    AlertDialog(
        onDismissRequest = onDialogClose,
        title = { Text(text = stringResource(R.string.about_us_who_we_are)) },
        confirmButton = {
            Button(onClick = onDialogClose) { Text(stringResource(R.string.okay)) }
        },
        text = {
            Text(
                text = stringResource(R.string.about_us_who_we_are_content),
                style = MaterialTheme.typography.bodyMedium,
            )
        },
    )
}

@Preview
@Composable
private fun PreviewAboutUsScreen() {
    SmileIdentityTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            AboutUsScreen()
        }
    }
}

@Preview
@Composable
private fun PreviewWhoWeAreDialog() {
    SmileIdentityTheme {
        WhoWeAreDialog()
    }
}
