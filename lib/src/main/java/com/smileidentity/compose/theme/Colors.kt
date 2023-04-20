package com.smileidentity.compose.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.res.colorResource
import com.smileidentity.R
import com.smileidentity.SmileID

@Suppress("UnusedReceiverParameter")
val SmileID.ColorScheme: ColorScheme
    @Composable
    @ReadOnlyComposable
    get() = lightColorScheme(
        primary = colorResource(R.color.si_color_material_primary),
        onPrimary = colorResource(R.color.si_color_material_on_primary),
        primaryContainer = colorResource(R.color.si_color_material_primary_container),
        onPrimaryContainer = colorResource(R.color.si_color_material_on_primary_container),
        inversePrimary = colorResource(R.color.si_color_material_inverse_primary),
        secondary = colorResource(R.color.si_color_material_secondary),
        onSecondary = colorResource(R.color.si_color_material_on_secondary),
        secondaryContainer = colorResource(R.color.si_color_material_secondary_container),
        onSecondaryContainer = colorResource(R.color.si_color_material_on_secondary_container),
        tertiary = colorResource(R.color.si_color_material_tertiary),
        onTertiary = colorResource(R.color.si_color_material_on_tertiary),
        tertiaryContainer = colorResource(R.color.si_color_material_tertiary_container),
        onTertiaryContainer = colorResource(R.color.si_color_material_on_tertiary_container),
        background = colorResource(R.color.si_color_material_background),
        onBackground = colorResource(R.color.si_color_material_on_background),
        surface = colorResource(R.color.si_color_material_surface),
        onSurface = colorResource(R.color.si_color_material_on_surface),
        surfaceVariant = colorResource(R.color.si_color_material_surface_variant),
        onSurfaceVariant = colorResource(R.color.si_color_material_on_surface_variant),
        surfaceTint = colorResource(R.color.si_color_material_surface_tint),
        inverseSurface = colorResource(R.color.si_color_material_inverse_surface),
        inverseOnSurface = colorResource(R.color.si_color_material_inverse_on_surface),
        error = colorResource(R.color.si_color_material_error),
        onError = colorResource(R.color.si_color_material_on_error),
        errorContainer = colorResource(R.color.si_color_material_error_container),
        onErrorContainer = colorResource(R.color.si_color_material_on_error_container),
        outline = colorResource(R.color.si_color_material_outline),
        outlineVariant = colorResource(R.color.si_color_material_outline_variant),
        scrim = colorResource(R.color.si_color_material_scrim),
    )
