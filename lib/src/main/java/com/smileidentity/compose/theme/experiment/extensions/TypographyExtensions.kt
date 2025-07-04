package com.smileidentity.compose.theme.experiment.extensions

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import com.smileidentity.compose.theme.experiment.FontProvider
import com.smileidentity.compose.theme.experiment.TypographyType

fun Typography.copyWithFontProvider(fontProvider: FontProvider): Typography {
    with(this) {
        return copy(
            displayLarge = displayLarge.modifyFontIfNeeded(
                typographyType = TypographyType.DISPLAY_LARGE,
                fontProvider = fontProvider,
            ),
            displayMedium = displayMedium.modifyFontIfNeeded(
                typographyType = TypographyType.DISPLAY_MEDIUM,
                fontProvider = fontProvider,
            ),
            displaySmall = displaySmall.modifyFontIfNeeded(
                typographyType = TypographyType.DISPLAY_SMALL,
                fontProvider = fontProvider,
            ),
            headlineLarge = headlineLarge.modifyFontIfNeeded(
                typographyType = TypographyType.HEADLINE_LARGE,
                fontProvider = fontProvider,
            ),
            headlineMedium = headlineMedium.modifyFontIfNeeded(
                typographyType = TypographyType.HEADLINE_MEDIUM,
                fontProvider = fontProvider,
            ),
            headlineSmall = headlineSmall.modifyFontIfNeeded(
                typographyType = TypographyType.HEADLINE_SMALL,
                fontProvider = fontProvider,
            ),
            titleLarge = titleLarge.modifyFontIfNeeded(
                typographyType = TypographyType.TITLE_LARGE,
                fontProvider = fontProvider,
            ),
            titleMedium = titleMedium.modifyFontIfNeeded(
                typographyType = TypographyType.TITLE_MEDIUM,
                fontProvider = fontProvider,
            ),
            titleSmall = titleSmall.modifyFontIfNeeded(
                typographyType = TypographyType.TITLE_SMALL,
                fontProvider = fontProvider,
            ),
            bodyLarge = bodyLarge.modifyFontIfNeeded(
                typographyType = TypographyType.BODY_LARGE,
                fontProvider = fontProvider,
            ),
            bodyMedium = bodyMedium.modifyFontIfNeeded(
                typographyType = TypographyType.BODY_MEDIUM,
                fontProvider = fontProvider,
            ),
            bodySmall = bodySmall.modifyFontIfNeeded(
                typographyType = TypographyType.BODY_SMALL,
                fontProvider = fontProvider,
            ),
            labelLarge = labelLarge.modifyFontIfNeeded(
                typographyType = TypographyType.LABEL_LARGE,
                fontProvider = fontProvider,
            ),
            labelMedium = labelMedium.modifyFontIfNeeded(
                typographyType = TypographyType.LABEL_MEDIUM,
                fontProvider = fontProvider,
            ),
            labelSmall = labelSmall.modifyFontIfNeeded(
                typographyType = TypographyType.LABEL_SMALL,
                fontProvider = fontProvider,
            ),
        )
    }
}

private fun TextStyle.modifyFontIfNeeded(
    typographyType: TypographyType,
    fontProvider: FontProvider,
): TextStyle {
    val font = fontProvider.getFont(typographyType)
    return if (font == null) {
        this
    } else {
        this.copy(fontFamily = font)
    }
}
