package com.smileidentity.compose.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import com.smileidentity.metadata.LocalMetadataProvider

@Composable
internal fun SmileThemeSurface(
    colorScheme: ColorScheme,
    typography: Typography,
    content: @Composable () -> Unit,
) {
    // Use the network-aware provider to automatically update metadata
    LocalMetadataProvider.MetadataProvider {
        MaterialTheme(colorScheme = colorScheme, typography = typography) {
            Surface(content = content)
        }
    }
}
