package com.smileidentity.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.smileidentity.ui.components.SmileIDButton

@Composable
fun SmileIDInstructionsScreen(
    modifier: Modifier = Modifier,
    onContinueClick: () -> Unit = {},
    button: @Composable (onContinueClick: () -> Unit) -> Unit = { onClick ->
        SmileIDButton(
            text = "Continue",
            modifier = Modifier
                .fillMaxWidth()
                .testTag(tag = "continue_button"),
            onContinueClick = onClick,
        )
    },
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .weight(1f),
        ) {
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
        ) {
            button(onContinueClick)
        }
    }
}

@Preview
@Composable
private fun SmileIDInstructionsScreenPreview() {
    SmileIDInstructionsScreen()
}

@Preview
@Composable
private fun SmileIDInstructionsScreenCustomButtonPreview() {
    SmileIDInstructionsScreen(
        button = { onContinueClick ->
            OutlinedButton(
                onClick = { onContinueClick },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(text = "Custom Button")
            }
        },
    )
}
