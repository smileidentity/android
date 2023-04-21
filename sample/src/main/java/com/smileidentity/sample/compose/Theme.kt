package com.smileidentity.sample.compose

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.smileidentity.SmileID
import com.smileidentity.compose.theme.colorScheme
import com.smileidentity.compose.theme.typography

@Composable
fun SmileIDTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        }
    }

    MaterialTheme(
        colorScheme = SmileID.colorScheme,
        typography = SmileID.typography,
        content = content,
    )
}
