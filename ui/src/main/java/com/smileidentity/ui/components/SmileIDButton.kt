package com.smileidentity.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun SmileIDButton(text: String, modifier: Modifier = Modifier, onContinueClick: () -> Unit) {
    Button(
        onClick = onContinueClick,
        modifier = modifier,
    ) { Text(text) }
}

@Preview
@Composable
private fun SmileIDButtonPreview() {
    SmileIDButton(text = "Continue", modifier = Modifier.fillMaxWidth()) {}
}
