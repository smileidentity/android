package com.smileidentity.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf

private val LocalIsDarkMode = compositionLocalOf<Boolean> {
    error("LocalIsDarkMode CompositionLocal not configured")
}

@Composable
fun SmileIDTheme(
    darkMode: Boolean = isSystemInDarkTheme(),
    colors: ColorScheme = SmileIDColors.materialThemeColors(darkMode),
    typography: Typography = SmileIDTypography.material,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalIsDarkMode provides darkMode,
    ) {
        MaterialTheme(
            colorScheme = colors,
            typography = SmileIDTypography.material,
            content = content,
        )
    }
}

@Composable
fun Test() {
    SmileIDTheme(
        colors = SmileIDColors.defaultDarkColors().copy()
    ) {

    }
}

//object SmileIDTheme {
//    val colors: ColorPalette get() = SmileIDColor.Companion
//    val darkMode: Boolean
//        @Composable
//        get() = LocalIsDarkMode.current
//}
