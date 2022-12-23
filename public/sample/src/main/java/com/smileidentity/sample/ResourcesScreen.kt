package com.smileidentity.sample

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun ResourcesScreen() {
    val resources = listOf(
        Pair(
            stringResource(R.string.resources_explore_docs_title),
            stringResource(R.string.resources_explore_docs_subtitle),
        ),
        Pair(
            stringResource(R.string.resources_privacy_policy_title),
            stringResource(R.string.resources_privacy_policy_subtitle),
        ),
        Pair(
            stringResource(R.string.resources_faqs_title),
            stringResource(R.string.resources_faqs_subtitle),
        ),
        Pair(
            stringResource(R.string.resources_supported_types_title),
            stringResource(R.string.resources_supported_types_subtitle),
        ),
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        val context = LocalContext.current
        resources.forEach {
            ListItem(
                headlineText = { Text(it.first) },
                supportingText = { Text(it.second) },
                trailingContent = { Icon(Icons.Default.ArrowForward, null) },
                modifier = Modifier.clickable { context.toast(it.first) },
            )
            Divider()
        }
    }
}
