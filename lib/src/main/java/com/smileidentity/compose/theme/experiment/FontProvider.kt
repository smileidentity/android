package com.smileidentity.compose.theme.experiment

import androidx.compose.ui.text.font.FontFamily

/**
 * Implement this interface to provide custom fonts
 * If you don't, the default material3 theme fonts will be used.
 */
interface FontProvider {
    fun getFont(type: TypographyType): FontFamily?
}
