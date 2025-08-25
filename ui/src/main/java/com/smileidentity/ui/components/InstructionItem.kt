package com.smileidentity.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.List
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smileidentity.ui.design.SmileIDTheme
import com.smileidentity.ui.previews.DevicePreviews
import com.smileidentity.ui.previews.PreviewContent
import com.smileidentity.ui.utils.SelfieInstruction

@Composable
fun InstructionItem(
    instruction: SelfieInstruction,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = Color(0xFFF8F8F8),
                shape = RoundedCornerShape(12.dp),
            )
            .border(
                width = 1.dp,
                color = Color(0xFFE6E6E6),
                shape = RoundedCornerShape(12.dp),
            )
            .padding(16.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = Color.LightGray.copy(alpha = 0.3f),
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
                .padding(start = 16.dp)
                .weight(1f),
        ) {
            Text(
                text = instruction.title,
                style = SmileIDTheme.typography.subHeading,
                color = Color.Black,
            )
            Text(
                text = instruction.description,
                style = SmileIDTheme.typography.body,
                color = Color(0xFF666666),
                modifier = Modifier.padding(top = 4.dp),
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
