package com.smileidentity.compose.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import com.smileidentity.R
import com.smileidentity.SmileID

private val fontProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.si_com_google_android_gms_fonts_certs,
)

private val dmSansGoogleFont = GoogleFont(name = "DM Sans")

private val dmSans = FontFamily(
    Font(dmSansGoogleFont, fontProvider, FontWeight.Normal, FontStyle.Normal),
    Font(dmSansGoogleFont, fontProvider, FontWeight.Medium, FontStyle.Normal),
    Font(dmSansGoogleFont, fontProvider, FontWeight.Bold, FontStyle.Normal),
    Font(dmSansGoogleFont, fontProvider, FontWeight.Normal, FontStyle.Italic),
    Font(dmSansGoogleFont, fontProvider, FontWeight.Medium, FontStyle.Italic),
    Font(dmSansGoogleFont, fontProvider, FontWeight.Bold, FontStyle.Italic),
)

/**
 * Define the typography by taking the default defined typographies and overriding the font family
 */
@Suppress("UnusedReceiverParameter")
val SmileID.typography: Typography
    @Composable
    @ReadOnlyComposable
    get() = Typography(
        displayLarge = MaterialTheme.typography.displayLarge.copy(fontFamily = dmSans),
        displayMedium = MaterialTheme.typography.displayMedium.copy(fontFamily = dmSans),
        displaySmall = MaterialTheme.typography.displaySmall.copy(fontFamily = dmSans),
        headlineLarge = MaterialTheme.typography.headlineLarge.copy(fontFamily = dmSans),
        headlineMedium = MaterialTheme.typography.headlineMedium.copy(fontFamily = dmSans),
        headlineSmall = MaterialTheme.typography.headlineSmall.copy(fontFamily = dmSans),
        titleLarge = MaterialTheme.typography.titleLarge.copy(fontFamily = dmSans),
        titleMedium = MaterialTheme.typography.titleMedium.copy(fontFamily = dmSans),
        titleSmall = MaterialTheme.typography.titleSmall.copy(fontFamily = dmSans),
        bodyLarge = MaterialTheme.typography.bodyLarge.copy(fontFamily = dmSans),
        bodyMedium = MaterialTheme.typography.bodyMedium.copy(fontFamily = dmSans),
        bodySmall = MaterialTheme.typography.bodySmall.copy(fontFamily = dmSans),
        labelLarge = MaterialTheme.typography.labelLarge.copy(
            fontFamily = dmSans,
            fontWeight = FontWeight.Bold,
        ),
        labelMedium = MaterialTheme.typography.labelMedium.copy(fontFamily = dmSans),
        labelSmall = MaterialTheme.typography.labelSmall.copy(fontFamily = dmSans),
    )

/**
 * Define the typography by taking the default defined typographies and overriding the font family
 */
@Suppress("UnusedReceiverParameter")
val SmileID.typographyV2: Typography
    @Composable
    @ReadOnlyComposable
    get() = Typography(
        titleMedium = MaterialTheme.typography.titleMedium.copy(fontFamily = dmSans),

        // reworking this
        displayLarge = MaterialTheme.typography.displayLarge.copy(fontFamily = dmSans),
        displayMedium = MaterialTheme.typography.displayMedium.copy(fontFamily = dmSans),
        displaySmall = MaterialTheme.typography.displaySmall.copy(fontFamily = dmSans),
        headlineLarge = MaterialTheme.typography.headlineLarge.copy(fontFamily = dmSans),
        headlineMedium = MaterialTheme.typography.headlineMedium.copy(fontFamily = dmSans),
        headlineSmall = MaterialTheme.typography.headlineSmall.copy(fontFamily = dmSans),
        titleLarge = MaterialTheme.typography.titleLarge.copy(fontFamily = dmSans),
        titleSmall = MaterialTheme.typography.titleSmall.copy(fontFamily = dmSans),
        bodyLarge = MaterialTheme.typography.bodyLarge.copy(fontFamily = dmSans),
        bodyMedium = MaterialTheme.typography.bodyMedium.copy(fontFamily = dmSans),
        bodySmall = MaterialTheme.typography.bodySmall.copy(fontFamily = dmSans),
        labelLarge = MaterialTheme.typography.labelLarge.copy(
            fontFamily = dmSans,
            fontWeight = FontWeight.Bold,
        ),
        labelMedium = MaterialTheme.typography.labelMedium.copy(fontFamily = dmSans),
        labelSmall = MaterialTheme.typography.labelSmall.copy(fontFamily = dmSans),
    )
