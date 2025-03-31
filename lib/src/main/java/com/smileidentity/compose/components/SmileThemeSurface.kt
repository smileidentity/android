package com.smileidentity.compose.components

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

@Composable
internal fun SmileThemeSurface(
    colorScheme: ColorScheme,
    typography: Typography,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
    ) {
        MaterialTheme(colorScheme = colorScheme, typography = typography) {
            Surface(content = content)
        }
    }
}
