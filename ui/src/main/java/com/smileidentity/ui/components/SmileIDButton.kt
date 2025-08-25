package com.smileidentity.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.smileidentity.ui.design.SmileIDTheme
import com.smileidentity.ui.design.colors.SmileIDColor
import com.smileidentity.ui.previews.DevicePreviews
import com.smileidentity.ui.previews.PreviewContent

@Composable
fun SmileIDButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier.height(height = 52.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = SmileIDTheme.colors[SmileIDColor.primary],
            contentColor = SmileIDTheme.colors[SmileIDColor.primaryForeground],
        ),
        shape = RoundedCornerShape(12.dp),
    ) { Text(text = text, style = SmileIDTheme.typography.button) }
}

@DevicePreviews
@Composable
private fun SmileIDButtonPreview() {
    PreviewContent {
        SmileIDButton(text = "Continue", modifier = Modifier.fillMaxWidth()) {}
    }
}
