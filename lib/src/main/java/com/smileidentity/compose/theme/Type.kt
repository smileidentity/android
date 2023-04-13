package com.smileidentity.compose.theme

import androidx.compose.material3.Typography
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
private val dmSansGoogleFont = GoogleFont(name = "DM Sans")

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

private val dmSans = FontFamily(
    Font(dmSansGoogleFont, fontProvider, FontWeight.W100, FontStyle.Normal),
    Font(dmSansGoogleFont, fontProvider, FontWeight.W200, FontStyle.Normal),
    Font(dmSansGoogleFont, fontProvider, FontWeight.W300, FontStyle.Normal),
    Font(dmSansGoogleFont, fontProvider, FontWeight.W400, FontStyle.Normal),
    Font(dmSansGoogleFont, fontProvider, FontWeight.W500, FontStyle.Normal),
    Font(dmSansGoogleFont, fontProvider, FontWeight.W600, FontStyle.Normal),
    Font(dmSansGoogleFont, fontProvider, FontWeight.W700, FontStyle.Normal),
    Font(dmSansGoogleFont, fontProvider, FontWeight.W800, FontStyle.Normal),
    Font(dmSansGoogleFont, fontProvider, FontWeight.W900, FontStyle.Normal),
    Font(dmSansGoogleFont, fontProvider, FontWeight.W100, FontStyle.Italic),
    Font(dmSansGoogleFont, fontProvider, FontWeight.W200, FontStyle.Italic),
    Font(dmSansGoogleFont, fontProvider, FontWeight.W300, FontStyle.Italic),
    Font(dmSansGoogleFont, fontProvider, FontWeight.W400, FontStyle.Italic),
    Font(dmSansGoogleFont, fontProvider, FontWeight.W500, FontStyle.Italic),
    Font(dmSansGoogleFont, fontProvider, FontWeight.W600, FontStyle.Italic),
    Font(dmSansGoogleFont, fontProvider, FontWeight.W700, FontStyle.Italic),
    Font(dmSansGoogleFont, fontProvider, FontWeight.W800, FontStyle.Italic),
    Font(dmSansGoogleFont, fontProvider, FontWeight.W900, FontStyle.Italic),
)

/**
 * Define the typography by taking the default typographies and overriding the font family. This has
 * been defined using .run {} to avoid this needing to be a @Composable getter (i.e. since it would
 * need to use MaterialTheme.typography otherwise as the base to copy from).
 */
val SmileIdentityTypography = Typography().run {
    Typography(
        displayLarge = displayLarge.copy(fontFamily = epilogue),
        displayMedium = displayMedium.copy(fontFamily = epilogue),
        displaySmall = displaySmall.copy(fontFamily = epilogue),
        headlineLarge = headlineLarge.copy(fontFamily = epilogue),
        headlineMedium = headlineMedium.copy(fontFamily = epilogue),
        headlineSmall = headlineSmall.copy(fontFamily = epilogue),
        titleLarge = titleLarge.copy(fontFamily = epilogue),
        titleMedium = titleMedium.copy(fontFamily = epilogue),
        titleSmall = titleSmall.copy(fontFamily = epilogue),
        bodyLarge = bodyLarge.copy(fontFamily = dmSans),
        bodyMedium = bodyMedium.copy(fontFamily = dmSans),
        bodySmall = bodySmall.copy(fontFamily = dmSans),
        labelLarge = labelLarge.copy(fontFamily = dmSans),
        labelMedium = labelMedium.copy(fontFamily = dmSans),
        labelSmall = labelSmall.copy(fontFamily = dmSans),
    )
}
