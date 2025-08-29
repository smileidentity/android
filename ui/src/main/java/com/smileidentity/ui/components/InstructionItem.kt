package com.smileidentity.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.smileidentity.ui.design.SmileIDTheme
import com.smileidentity.ui.design.colors.SmileIDColor
import com.smileidentity.ui.previews.DevicePreviews
import com.smileidentity.ui.previews.PreviewContent
import com.smileidentity.ui.utils.SelfieInstruction

@Composable
fun InstructionItem(
    instruction: SelfieInstruction,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = SmileIDTheme.colors[SmileIDColor.cardBackground],
                shape = RoundedCornerShape(12.dp),
            )
            .border(
                width = 1.dp,
                color = SmileIDTheme.colors[SmileIDColor.stroke],
                shape = RoundedCornerShape(12.dp),
            )
            .padding(10.dp),
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = SmileIDTheme.colors[SmileIDColor.stroke],
                    shape = CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = "Placeholder icon",
                tint = Color.DarkGray,
            )
        }

        Column(
            modifier = Modifier
                .padding(start = 10.dp)
                .weight(1f),
        ) {
            Text(
                text = instruction.title,
                style = SmileIDTheme.typography.cardTitle,
                color = SmileIDTheme.colors[SmileIDColor.titleText],
            )
            Text(
                text = instruction.description,
                style = SmileIDTheme.typography.cardSubTitle,
                color = SmileIDTheme.colors[SmileIDColor.cardText],
                modifier = Modifier.padding(top = 6.dp),
            )
        }
    }
}

@DevicePreviews
@Composable
private fun InstructionItemPreview() {
    PreviewContent {
        InstructionItem(
            instruction = SelfieInstruction.GOOD_LIGHT,
        )
    }
}
