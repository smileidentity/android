package com.smileidentity.compose.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import com.smileidentity.R

private val fontProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.si_com_google_android_gms_fonts_certs,
)

private val epilogueGoogleFont = GoogleFont(name = "Epilogue")

private val epilogue = FontFamily(
    Font(epilogueGoogleFont, fontProvider, FontWeight.W100, FontStyle.Normal),
    Font(epilogueGoogleFont, fontProvider, FontWeight.W200, FontStyle.Normal),
    Font(epilogueGoogleFont, fontProvider, FontWeight.W300, FontStyle.Normal),
    Font(epilogueGoogleFont, fontProvider, FontWeight.W400, FontStyle.Normal),
    Font(epilogueGoogleFont, fontProvider, FontWeight.W500, FontStyle.Normal),
    Font(epilogueGoogleFont, fontProvider, FontWeight.W600, FontStyle.Normal),
    Font(epilogueGoogleFont, fontProvider, FontWeight.W700, FontStyle.Normal),
    Font(epilogueGoogleFont, fontProvider, FontWeight.W800, FontStyle.Normal),
    Font(epilogueGoogleFont, fontProvider, FontWeight.W900, FontStyle.Normal),
    Font(epilogueGoogleFont, fontProvider, FontWeight.W100, FontStyle.Italic),
    Font(epilogueGoogleFont, fontProvider, FontWeight.W200, FontStyle.Italic),
    Font(epilogueGoogleFont, fontProvider, FontWeight.W300, FontStyle.Italic),
    Font(epilogueGoogleFont, fontProvider, FontWeight.W400, FontStyle.Italic),
    Font(epilogueGoogleFont, fontProvider, FontWeight.W500, FontStyle.Italic),
    Font(epilogueGoogleFont, fontProvider, FontWeight.W600, FontStyle.Italic),
    Font(epilogueGoogleFont, fontProvider, FontWeight.W700, FontStyle.Italic),
    Font(epilogueGoogleFont, fontProvider, FontWeight.W800, FontStyle.Italic),
    Font(epilogueGoogleFont, fontProvider, FontWeight.W900, FontStyle.Italic),
)

/**
 * Define the typography by taking the default typographies from MaterialTheme and overriding the
 * font family. This means that if a partner has modified some other aspect of the typography (i.e.
 * the default font size), those settings from the partner will be preserved
 */
val SmileIdentityTypography: Typography
    @Composable get() = Typography(
        displayLarge = MaterialTheme.typography.displayLarge.copy(fontFamily = epilogue),
        displayMedium = MaterialTheme.typography.displayMedium.copy(fontFamily = epilogue),
        displaySmall = MaterialTheme.typography.displaySmall.copy(fontFamily = epilogue),
        headlineLarge = MaterialTheme.typography.headlineLarge.copy(fontFamily = epilogue),
        headlineMedium = MaterialTheme.typography.headlineMedium.copy(fontFamily = epilogue),
        headlineSmall = MaterialTheme.typography.headlineSmall.copy(fontFamily = epilogue),
        titleLarge = MaterialTheme.typography.titleLarge.copy(fontFamily = epilogue),
        titleMedium = MaterialTheme.typography.titleMedium.copy(fontFamily = epilogue),
        titleSmall = MaterialTheme.typography.titleSmall.copy(fontFamily = epilogue),
        bodyLarge = MaterialTheme.typography.bodyLarge.copy(fontFamily = epilogue),
        bodyMedium = MaterialTheme.typography.bodyMedium.copy(fontFamily = epilogue),
        bodySmall = MaterialTheme.typography.bodySmall.copy(fontFamily = epilogue),
        labelLarge = MaterialTheme.typography.labelLarge.copy(fontFamily = epilogue),
        labelMedium = MaterialTheme.typography.labelMedium.copy(fontFamily = epilogue),
        labelSmall = MaterialTheme.typography.labelSmall.copy(fontFamily = epilogue),
    )
