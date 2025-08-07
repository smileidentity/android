package com.smileidentity.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.smileidentity.ui.components.SmileIDButton
import com.smileidentity.ui.previews.DevicePreviews
import com.smileidentity.ui.previews.ThemePreviews

@Composable
fun SmileIDPreviewScreen(
    modifier: Modifier = Modifier,
    onContinue: () -> Unit = {},
    onRetry: () -> Unit = {},
    continueButton: @Composable (onContinue: () -> Unit) -> Unit = { onClick ->
        SmileIDButton(
            text = "Continue",
            modifier = Modifier
                .fillMaxWidth()
                .testTag(tag = "preview:continue_button"),
            onClick = onClick,
        )
    },
    cancelButton: @Composable (onRetry: () -> Unit) -> Unit = { onClick ->
        SmileIDButton(
            text = "Retry",
            modifier = Modifier
                .fillMaxWidth()
                .testTag(tag = "preview:retry_button"),
            onClick = onClick,
        )
    },
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(all = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom,
    ) {
        continueButton(onContinue)
        Spacer(modifier = Modifier.height(16.dp))
        cancelButton(onRetry)
    }
}

@ThemePreviews
@DevicePreviews
@Composable
private fun SmileIDPreviewScreenPreview() {
    SmileIDPreviewScreen()
}

