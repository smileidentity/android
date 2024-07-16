package com.smileidentity.compose.components

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.toMutableStateList
import com.smileidentity.models.v2.Metadata

@Composable
internal fun SmileThemeSurface(
    colorScheme: ColorScheme,
    typography: Typography,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalMetadata provides remember { Metadata.default().items.toMutableStateList() },
    ) {
        MaterialTheme(colorScheme = colorScheme, typography = typography) {
            Surface(content = content)
        }
    }
}
