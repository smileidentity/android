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

    val titleHuge: TextStyle
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

    override val titleHuge = baseStyle.copy(
        fontSize = 40.sp,
        fontWeight = FontWeight.ExtraBold,
        lineHeight = 48.sp,
        fontFamily = dmSansFontFamily,
        letterSpacing = (-1).sp,
    )
}

@Preview(device = Devices.NEXUS_10)
@Composable
private fun PreviewTitle() {
    PreviewTypographyGroup {
        PreviewTypographyItem(style = SmileIDTypography.titleHuge, name = "Title Huge")
    }
}

@Preview(device = Devices.NEXUS_10)
@Composable
private fun PreviewBody() {
    PreviewTypographyGroup {
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
        }
    }
}

@Preview(device = Devices.NEXUS_10)
@Composable
private fun PreviewCaption() {
    PreviewTypographyGroup {
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
            style = SmileIDTypography.titleHuge,
        )
    }
}
