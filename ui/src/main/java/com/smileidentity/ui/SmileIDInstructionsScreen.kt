package com.smileidentity.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
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
                Text(text = "Custom Button")
            }
        },
    )
}
