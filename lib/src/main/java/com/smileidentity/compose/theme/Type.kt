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

private val epilogueGoogleFont = GoogleFont(name = "Epilogue")
private val dmSansGoogleFont = GoogleFont(name = "DM Sans")

private val epilogue = FontFamily(
    Font(epilogueGoogleFont, fontProvider, FontWeight.Light, FontStyle.Normal),
    Font(epilogueGoogleFont, fontProvider, FontWeight.Normal, FontStyle.Normal),
    Font(epilogueGoogleFont, fontProvider, FontWeight.ExtraBold, FontStyle.Normal),
    Font(epilogueGoogleFont, fontProvider, FontWeight.Light, FontStyle.Italic),
    Font(epilogueGoogleFont, fontProvider, FontWeight.Normal, FontStyle.Italic),
    Font(epilogueGoogleFont, fontProvider, FontWeight.ExtraBold, FontStyle.Italic),
)

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
        displayLarge = MaterialTheme.typography.displayLarge.copy(fontFamily = epilogue),
        displayMedium = MaterialTheme.typography.displayMedium.copy(fontFamily = epilogue),
        displaySmall = MaterialTheme.typography.displaySmall.copy(fontFamily = epilogue),
        headlineLarge = MaterialTheme.typography.headlineLarge.copy(fontFamily = epilogue),
        headlineMedium = MaterialTheme.typography.headlineMedium.copy(fontFamily = epilogue),
        headlineSmall = MaterialTheme.typography.headlineSmall.copy(fontFamily = epilogue),
        titleLarge = MaterialTheme.typography.titleLarge.copy(fontFamily = epilogue),
        titleMedium = MaterialTheme.typography.titleMedium.copy(fontFamily = epilogue),
        titleSmall = MaterialTheme.typography.titleSmall.copy(fontFamily = epilogue),
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
