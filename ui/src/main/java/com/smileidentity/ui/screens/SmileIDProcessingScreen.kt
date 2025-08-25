package com.smileidentity.ui.screens

import androidx.compose.foundation.background
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
import com.smileidentity.ui.design.SmileIDTheme
import com.smileidentity.ui.design.colors.SmileIDColor
import com.smileidentity.ui.previews.DevicePreviews
import com.smileidentity.ui.previews.PreviewContent

@Composable
fun SmileIDProcessingScreen(
    modifier: Modifier = Modifier,
    onContinue: () -> Unit = {},
    onRetry: () -> Unit = {},
    continueButton: @Composable (onContinue: () -> Unit) -> Unit = { onClick ->
        SmileIDButton(
            text = "Close",
            modifier = Modifier
                .fillMaxWidth()
                .testTag(tag = "preview:close_button"),
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
            .background(color = SmileIDTheme.colors[SmileIDColor.background])
            .padding(all = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom,
    ) {
        continueButton(onContinue)
        Spacer(modifier = Modifier.height(16.dp))
        cancelButton(onRetry)
    }
}

@DevicePreviews
@Composable
private fun SmileIDProcessingScreenPreview() {
    PreviewContent {
        SmileIDProcessingScreen()
    }
}
