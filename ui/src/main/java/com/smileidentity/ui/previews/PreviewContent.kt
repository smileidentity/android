package com.smileidentity.ui.previews

import android.annotation.SuppressLint
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.smileidentity.ui.design.SmileIDTheme

@SuppressLint("ComposePreviewPublic")
@DevicePreviews
@Composable
fun PreviewContent(
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable BoxScope.() -> Unit = {
        Text(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
            textAlign = TextAlign.Center,
            text = "Missing preview content",
        )
    },
) {
    val activity = LocalActivity.current
    LaunchedEffect(activity) {
        activity?.window?.let { window -> WindowCompat.setDecorFitsSystemWindows(window, false) }
    }
    SmileIDTheme {
        Surface {
            Box(
                modifier = Modifier.padding(contentPadding),
                content = content,
            )
        }
    }
}
