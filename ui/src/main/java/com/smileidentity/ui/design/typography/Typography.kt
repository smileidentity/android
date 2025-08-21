package com.smileidentity.ui.design.typography

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smileidentity.ui.R
import com.smileidentity.ui.previews.PreviewContent

interface Typography {
    val dmSansFontFamily: FontFamily
    val pageHeading: TextStyle
    val subHeading: TextStyle
    val sectionHeading: TextStyle
    val body: TextStyle
    val button: TextStyle
}

internal object SmileIDTypography : Typography {
    private val fontProvider = GoogleFont.Provider(
        providerAuthority = "com.google.android.gms.fonts",
        providerPackage = "com.google.android.gms",
        certificates = R.array.si_com_google_android_gms_fonts_certs,
    )

    private val dmSansGoogleFont = GoogleFont(name = "DM Sans")

    override val dmSansFontFamily: FontFamily = FontFamily(
        Font(
            googleFont = dmSansGoogleFont,
            fontProvider = fontProvider,
            weight = FontWeight.Normal,
            style = FontStyle.Normal,
        ),
        Font(
            googleFont = dmSansGoogleFont,
            fontProvider = fontProvider,
            weight = FontWeight.Medium,
            style = FontStyle.Normal,
        ),
        Font(
            googleFont = dmSansGoogleFont,
            fontProvider = fontProvider,
            weight = FontWeight.Bold,
            style = FontStyle.Normal,
        ),
        Font(
            googleFont = dmSansGoogleFont,
            fontProvider = fontProvider,
            weight = FontWeight.Normal,
            style = FontStyle.Italic,
        ),
        Font(
            googleFont = dmSansGoogleFont,
            fontProvider = fontProvider,
            weight = FontWeight.Medium,
            style = FontStyle.Italic,
        ),
        Font(
            googleFont = dmSansGoogleFont,
            fontProvider = fontProvider,
            weight = FontWeight.Bold,
            style = FontStyle.Italic,
        ),
    )

    private val baseStyle = TextStyle(
        fontFamily = dmSansFontFamily,
        fontStyle = FontStyle.Normal,
    )

    override val pageHeading: TextStyle = baseStyle.copy(
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 48.sp,
        fontFamily = dmSansFontFamily,
        letterSpacing = (-1).sp,
    )

    override val subHeading: TextStyle = baseStyle.copy(
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 48.sp,
        fontFamily = dmSansFontFamily,
        letterSpacing = (-1).sp,
    )

    override val sectionHeading: TextStyle = baseStyle.copy(
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 48.sp,
        fontFamily = dmSansFontFamily,
        letterSpacing = (-1).sp,
    )

    override val body: TextStyle = baseStyle.copy(
        fontSize = 12.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 48.sp,
        fontFamily = dmSansFontFamily,
        letterSpacing = (-1).sp,
    )

    override val button: TextStyle = baseStyle.copy(
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 48.sp,
        fontFamily = dmSansFontFamily,
        letterSpacing = (-1).sp,
    )
}

@Preview(device = Devices.NEXUS_10)
@Composable
private fun PreviewTitle() {
    PreviewTypographyGroup {
        PreviewTypographyItem(style = SmileIDTypography.pageHeading, name = "Page Heading")
        PreviewTypographyItem(style = SmileIDTypography.subHeading, name = "Sub Heading")
    }
}

@Preview(device = Devices.NEXUS_10)
@Composable
private fun PreviewBody() {
    PreviewTypographyGroup {
        PreviewTypographyItem(style = SmileIDTypography.sectionHeading, name = "Section Heading")
        PreviewTypographyItem(style = SmileIDTypography.body, name = "Body")
    }
}

@Preview(device = Devices.NEXUS_10)
@Composable
private fun PreviewButton() {
    PreviewContent {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            PreviewTypographyItem(style = SmileIDTypography.button, name = "Button")
        }
    }
}

@Composable
private fun PreviewTypographyGroup(content: @Composable ColumnScope.() -> Unit) {
    PreviewContent {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp),
            content = content,
        )
    }
}

@Composable
private fun PreviewTypographyItem(name: String, style: TextStyle) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(32.dp),
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = name,
            style = style,
        )
        Text(
            modifier = Modifier.weight(3f),
            text = "Unlock digital Africa. Build a trusted user base.",
            style = style,
        )
        Text(
            text = """
                font style = ${style.fontStyle}
                font size = ${style.fontSize}
                font line height = ${style.lineHeight}
                font weight = ${style.fontWeight?.weight}
                font letter spacing = ${style.letterSpacing}
            """.trimIndent(),
            style = SmileIDTypography.body,
        )
    }
}
