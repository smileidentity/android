package com.smileidentity.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

class ThemeBuilder {
    private var brandColor: Color? = null
    private var accentColor: Color? = null
    private var surfaceColor: Color? = null
    private var backgroundColor: Color? = null
    private var errorColor: Color? = null
    private var successColor: Color? = null

    fun brand(light: Color, dark: Color? = null) = apply {
        this.brandColor = light
        // Store dark variant
    }

    fun accent(light: Color, dark: Color? = null) = apply {
        this.accentColor = light
    }

    fun surface(light: Color, dark: Color? = null) = apply {
        this.surfaceColor = light
    }

    fun build(): SDKTheme {
        // Generate Material 3 color schemes
        val lightScheme = generateMaterial3Scheme(
            brand = brandColor ?: defaultBrand,
            accent = accentColor ?: deriveAccent(brandColor),
            surface = surfaceColor ?: defaultSurface,
            isLight = true
        )

        val darkScheme = generateMaterial3Scheme(
            // Use dark variants or auto-generate
            isLight = false
        )

        return SDKTheme(lightScheme, darkScheme)
    }

    private fun generateMaterial3Scheme(
        brand: Color,
        accent: Color,
        surface: Color,
        isLight: Boolean
    ): ColorScheme {
        // Map to Material 3 tokens
        return if (isLight) {
            lightColorScheme(
                primary = brand,
                secondary = accent,
                tertiary = brand.harmonize(60), // Generate tertiary
                surface = surface,
                // ... other tokens derived or defaulted
            )
        } else {
            // Dark scheme generation
        }
    }
}

/**
 * The main theme class that wraps Material 3 color schemes
 * and provides the theming interface for your SDK
 */
class SDKTheme(
    val lightColorScheme: ColorScheme,
    val darkColorScheme: ColorScheme,
    val typography: androidx.compose.material3.Typography,
    val shapes: androidx.compose.material3.Shapes
) {
    /**
     * Composable function to apply this theme to SDK components
     */
    @Composable
    fun ApplyTheme(
        darkTheme: Boolean = isSystemInDarkTheme(),
        dynamicColor: Boolean = false,
        content: @Composable () -> Unit
    ) {
        val colorScheme = when {
            dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                val context = LocalContext.current
                if (darkTheme) dynamicDarkColorScheme(context)
                else dynamicLightColorScheme(context)
            }
            darkTheme -> darkColorScheme
            else -> lightColorScheme
        }

        // Provide the theme to the composition
        CompositionLocalProvider(
            LocalSDKTheme provides this
        ) {
            MaterialTheme(
                colorScheme = colorScheme,
                typography = typography,
                shapes = shapes,
                content = content
            )
        }
    }

    /**
     * Get the appropriate color scheme based on dark mode
     */
    fun getColorScheme(isDark: Boolean): ColorScheme {
        return if (isDark) darkColorScheme else lightColorScheme
    }

    /**
     * Builder function to create a modified version of this theme
     */
    fun copy(
        lightColorScheme: ColorScheme = this.lightColorScheme,
        darkColorScheme: ColorScheme = this.darkColorScheme,
        typography: androidx.compose.material3.Typography = this.typography,
        shapes: androidx.compose.material3.Shapes = this.shapes
    ): SDKTheme {
        return SDKTheme(
            lightColorScheme = lightColorScheme,
            darkColorScheme = darkColorScheme,
            typography = typography,
            shapes = shapes
        )
    }

    companion object {
        /**
         * Default theme instance
         */
        val Default = SDKTheme(
            lightColorScheme = lightColorScheme(),
            darkColorScheme = darkColorScheme()
        )

        /**
         * Create theme from simplified colors
         */
        fun fromSimplifiedColors(
            primary: Color,
            secondary: Color,
            tertiary: Color? = null,
            surface: Color? = null,
            error: Color? = null
        ): SDKTheme {
            val builder = ThemeBuilder()
                .brand(primary)
                .accent(secondary)

            tertiary?.let { builder.brand(it) }
            surface?.let { builder.surface(it) }
            error?.let { builder.brand(it) }

            return builder.build()
        }
    }
}

/**
 * CompositionLocal to access the current SDK theme
 */
val LocalSDKTheme = staticCompositionLocalOf { SDKTheme.Default }

/**
 * Extension property to access current theme in Composables
 */
val sdkTheme: SDKTheme
    @Composable
    get() = LocalSDKTheme.current

/**
 * Extension functions for easier access to theme properties
 */
@Composable
fun sdkColors(): ColorScheme {
    return MaterialTheme.colorScheme
}

@Composable
fun sdkTypography(): androidx.compose.material3.Typography {
    return MaterialTheme.typography
}

@Composable
fun sdkShapes(): androidx.compose.material3.Shapes {
    return MaterialTheme.shapes
}
// Usage
val theme = ThemeBuilder()
    .brand(light = Color(0xFF1976D2), dark = Color(0xFF90CAF9))
    .accent(light = Color(0xFFFF6B6B), dark = Color(0xFFFF8787))
    .surface(light = Color.White, dark = Color(0xFF121212))
    .build()
