package com.smileidentity.compose.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.toMutableStateList
import com.smileidentity.compose.metadata.LocalMetadataProvider
import com.smileidentity.compose.metadata.Metadata

@Composable
internal fun SmileThemeSurface(
    colorScheme: ColorScheme,
    typography: Typography,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalMetadataProvider provides Metadata.default().items.toMutableStateList(),
    ) {
        MaterialTheme(colorScheme = colorScheme, typography = typography) {
            Surface(content = content)
        }
    }
}
