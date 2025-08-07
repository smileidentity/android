package com.smileidentity.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun SmileIDButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier.height(height = 58.dp),
    ) { Text(text) }
}

@Preview
@Composable
private fun SmileIDButtonPreview() {
    SmileIDButton(text = "Continue", modifier = Modifier.fillMaxWidth()) {}
}
