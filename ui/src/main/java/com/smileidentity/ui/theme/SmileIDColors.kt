package com.smileidentity.ui.theme

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
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
import com.smileidentity.ui.previews.PreviewContent


data class SmileIDColors(
    val background: Color,
    val foreground: Color,
    val cardForeground: Color,
    val primary: Color,
    val primaryForeground: Color,
) {
    companion object {
        @Composable
        internal fun defaultColors(smileIDColors: SmileIDColors) =
            lightColorScheme(
                smileIDColors.background,
            )

        @Composable
        internal fun defaultDarkColors(smileIDColors: SmileIDColors) =
            darkColorScheme(
                smileIDColors.background,
            )

        @Composable
        internal fun materialThemeColors(darkMode: Boolean) = if (darkMode) {
            darkColorScheme(
                background = defaultDarkColors().background,
            )
        } else {
            lightColorScheme(
                background = defaultColors().background,
            )
        }
    }
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
//    Row {
//        val groups = SmileIDColor.entries
//            .groupBy { it.name }
//            .values.toList()
//
//        groups.forEach { group ->
//            Column {
//                group.forEach {
//                    PreviewColor(it.name, SmileIDTheme.colors[it])
//                }
//            }
//        }
//    }
}

@PreviewLightDark
@Composable
private fun PreviewColors() {
    PreviewContent {
        PreviewColorPalette()
    }
}
