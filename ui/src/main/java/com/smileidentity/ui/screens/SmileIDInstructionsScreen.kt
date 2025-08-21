package com.smileidentity.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.smileidentity.ui.R
import com.smileidentity.ui.components.SmileIDButton
import com.smileidentity.ui.design.SmileIDTheme
import com.smileidentity.ui.design.colors.SmileIDColor
import com.smileidentity.ui.previews.DevicePreviews
import com.smileidentity.ui.previews.PreviewContent

@Composable
fun SmileIDInstructionsScreen(
    modifier: Modifier = Modifier,
    scrollState: ScrollState = rememberScrollState(),
    onContinue: () -> Unit = {},
    onCancel: () -> Unit = {},
    continueButton: @Composable (onContinue: () -> Unit) -> Unit = { onClick ->
        SmileIDButton(
            text = "Continue",
            modifier = Modifier
                .fillMaxWidth()
                .testTag(tag = "instructions:continue_button"),
            onClick = onClick,
        )
    },
    cancelButton: @Composable (onCancel: () -> Unit) -> Unit = { onClick ->
        SmileIDButton(
            text = "Cancel",
            modifier = Modifier
                .fillMaxWidth()
                .testTag(tag = "instructions:cancel_button"),
            onClick = onClick,
        )
    },
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SmileIDTheme.colors[SmileIDColor.background])
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth()
                .verticalScroll(state = scrollState)
                .weight(1f),
        ) {
            Image(
                painter = painterResource(id = R.drawable.si_logo),
                modifier = Modifier
                    .size(178.dp)
                    .padding(top = 8.dp)
                    .testTag(tag = "instructions:logo"),
                contentDescription = null,
            )
            Text(
                text = "Some header here",
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .testTag(tag = "instructions:header"),
            )
            Text(
                text = "Some subtitle here",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Light,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(top = 16.dp, bottom = 32.dp)
                    .testTag(tag = "instructions:subtitle"),
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
        ) {
            continueButton(onContinue)
            Spacer(modifier = Modifier.height(16.dp))
            cancelButton(onCancel)
        }
    }
}

@DevicePreviews
@Composable
private fun SmileIDInstructionsScreenPreview() {
    PreviewContent {
        SmileIDInstructionsScreen()
    }
}

@DevicePreviews
@Composable
private fun SmileIDInstructionsScreenCustomButtonPreview() {
    PreviewContent {
        SmileIDInstructionsScreen(
            continueButton = { onContinue ->
                OutlinedButton(
                    onClick = { onContinue },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(text = "Custom Button")
                }
            },
        )
    }
}
