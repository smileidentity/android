package com.smileidentity.compose.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

// TODO: Use Color Resources
val SmileIdentityDarkerBlue = Color(0xFF001096)
val SmileIdentitySemiTransparentBackground = Color(0xCCE0E0E0)
val SmileIdentityAffirmationColor = Color(0xFF5DC998)

// TODO: Set the affirmation/success color to the tertiary color
val SmileIdentityColorScheme = lightColorScheme(
    primary = Color(0xFF001096),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF008096),
    onPrimaryContainer = Color(0xFFFFFFFF),
    inversePrimary = Color(0xFF008096),
    secondary = Color(0xFF008096),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFF001096),
    onSecondaryContainer = Color(0xFFFFFFFF),
    tertiary = Color(0xFF008096),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFF001096),
    onTertiaryContainer = Color(0xFFFFFFFF),
    background = Color(0xFFFFFFFF),
    onBackground = Color(0xFF001096),
    surface = Color(0xFF001096),
    onSurface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFF001096),
    onSurfaceVariant = Color(0xFFFFFFFF),
    surfaceTint = Color(0xFF001096),
    inverseSurface = Color(0xFF001096),
    inverseOnSurface = Color(0xFFFFFFFF),
    error = Color(0xCCF15A5A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xCCF15A5A),
    onErrorContainer = Color(0xFFFFFFFF),
    outline = Color(0xFF001096),
    outlineVariant = Color(0xFF001096),
    scrim = Color(0xFF000000),
)

val SmileIdentityTypography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp,
    ),
    /* Other default text styles to override
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
    */
)

@Composable
fun SmileIdentityTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = SmileIdentityColorScheme,
        typography = SmileIdentityTypography,
        content = content,
    )
}
