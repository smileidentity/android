package com.smileidentity.sample.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.smileidentity.sample.R

@Composable
fun ResourcesScreen(
    modifier: Modifier = Modifier,
) {
    val uriHandler = LocalUriHandler.current
    val resources = listOf(
        Triple(
            stringResource(R.string.resources_explore_docs_title),
            stringResource(R.string.resources_explore_docs_subtitle),
        ) { uriHandler.openUri("https://docs.smileidentity.com") },
        Triple(
            stringResource(R.string.resources_privacy_policy_title),
            stringResource(R.string.resources_privacy_policy_subtitle),
        ) { uriHandler.openUri("https://smileidentity.com/privacy-policy") },
        Triple(
            stringResource(R.string.resources_faqs_title),
            stringResource(R.string.resources_faqs_subtitle),
        ) { uriHandler.openUri("https://docs.smileidentity.com/further-reading/faqs") },
        Triple(
            stringResource(R.string.resources_supported_types_title),
            stringResource(R.string.resources_supported_types_subtitle),
        ) { uriHandler.openUri("https://docs.smileidentity.com/supported-id-types") },
    )
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        resources.forEach {
            ListItem(
                headlineContent = { Text(it.first) },
                supportingContent = { Text(it.second) },
                trailingContent = { Icon(Icons.Default.ArrowForward, null) },
                modifier = Modifier.clickable { it.third() },
            )
            Divider()
        }
    }
}

@Preview
@Composable
private fun ResourcesScreenPreview() {
    SmileIDTheme {
        Surface {
            ResourcesScreen()
        }
    }
}
