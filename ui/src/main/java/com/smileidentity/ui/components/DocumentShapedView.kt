package com.smileidentity.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.smileidentity.ui.previews.DevicePreviews
import com.smileidentity.ui.previews.PreviewContent

@Composable
fun DocumentShapedView(modifier: Modifier = Modifier) {
}

@DevicePreviews
@Composable
private fun SmileIDButtonPreview() {
    PreviewContent {
        DocumentShapedView()
    }
}
