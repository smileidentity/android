package com.smileidentity.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun SmileIDInstructionsScreen(
    modifier: Modifier = Modifier,
    onContinueClick: () -> Unit = {},
    button: @Composable (onContinueClick: () -> Unit) -> Unit = { onClick ->
        Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("continue_button"),
        ) {
            Text("Continue")
        }
    },
) {
    Column(modifier = modifier) {
        button(onContinueClick)
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
                Text("Custom Button")
            }
        },
    )
}
