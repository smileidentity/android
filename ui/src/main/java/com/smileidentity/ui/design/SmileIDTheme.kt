package com.smileidentity.ui.design

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import com.smileidentity.ui.design.colors.ColorPalette
import com.smileidentity.ui.design.colors.SmileIDColor
import com.smileidentity.ui.design.typography.SmileIDTypography
import com.smileidentity.ui.design.typography.Typography

private val LocalIsDarkMode = compositionLocalOf<Boolean> {
    error("LocalIsDarkMode CompositionLocal not configured")
}

@Composable
fun SmileIDTheme(darkMode: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalIsDarkMode provides darkMode,
    ) {
        MaterialTheme(
            colorScheme = materialThemeColors(darkMode = darkMode),
            typography = SmileIDTypography.material,
            content = content,
        )
    }
}

object SmileIDTheme {
    val colors: ColorPalette get() = SmileIDColor.Companion
    val typography: Typography get() = SmileIDTypography
    val darkMode: Boolean
        @Composable
        get() = LocalIsDarkMode.current
}

@Composable
internal fun materialThemeColors(darkMode: Boolean) = if (darkMode) {
    darkColorScheme(
        background = SmileIDTheme.colors[SmileIDColor.background],
    )
} else {
    lightColorScheme(
        background = SmileIDTheme.colors[SmileIDColor.background],
    )
}

internal val SmileIDTypography.material: androidx.compose.material3.Typography
    get() = androidx.compose.material3.Typography()
