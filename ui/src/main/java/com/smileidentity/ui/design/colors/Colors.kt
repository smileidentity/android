package com.smileidentity.ui.design.colors

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smileidentity.ui.design.SmileIDTheme
import com.smileidentity.ui.previews.PreviewContent

@Suppress("EnumEntryName")
enum class SmileIDColor(private val light: Color, private val dark: Color) {
    background(
        light = Color(0xFF_FFFFFF),
        dark = Color(0xFF_111112),
    ),
    ;

    companion object : ColorPalette {
        /**
         * Get the theme color value for [SmileIDColor]
         */
        @Composable
        override operator fun get(color: SmileIDColor): Color = if (SmileIDTheme.darkMode) {
            color.dark
        } else {
            color.light
        }
    }
}

interface ColorPalette {
    /**
     * Get the theme color value for [SmileIDColor]
     */
    @Composable
    operator fun get(color: SmileIDColor): Color
}

@Composable
private fun PreviewColor(label: String, color: Color) {
    Surface(
        modifier = Modifier.padding(4.dp),
        shape = RoundedCornerShape(4.dp),
        color = color,
    ) {
        Column(
            modifier = Modifier.size(width = 100.dp, height = 50.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = label,
                fontSize = 8.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (color.luminance() < 0.5) Color.White else Color.Black,
            )
            Text(
                text = color.toArgb().toUInt().toString(16).uppercase(),
                fontSize = 8.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (color.luminance() < 0.5) Color.White else Color.Black,
            )
        }
    }
}

@Composable
private fun PreviewColorPalette() {
    Row {
        val groups = SmileIDColor.entries
            .groupBy { it.name }
            .values.toList()

        groups.forEach { group ->
            Column {
                group.forEach {
                    PreviewColor(it.name, SmileIDTheme.colors[it])
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun PreviewColors() {
    PreviewContent {
        PreviewColorPalette()
    }
}
