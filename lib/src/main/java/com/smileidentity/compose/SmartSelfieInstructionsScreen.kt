package com.smileidentity.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.smileidentity.R
import com.smileidentity.compose.components.BottomPinnedColumn
import com.smileidentity.compose.components.CameraPermissionButton
import com.smileidentity.compose.preview.Preview
import com.smileidentity.compose.preview.SmilePreviews

@Composable
fun SmartSelfieInstructionsScreen(
    modifier: Modifier = Modifier,
    showAttribution: Boolean = true,
    onInstructionsAcknowledged: () -> Unit = { },
) {
    BottomPinnedColumn(
        scrollableContent = {
            Image(
                painter = painterResource(id = R.drawable.si_smart_selfie_instructions_hero),
                modifier = Modifier.size(128.dp),
                contentDescription = null,
            )
            Text(
                text = stringResource(R.string.si_smart_selfie_instruction_title),
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(id = R.string.si_verify_identity_instruction_subtitle),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Light,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 16.dp, bottom = 32.dp),
            )
            val instructions = listOf(
                Triple(
                    R.drawable.si_photo_capture_instruction_good_light,
                    R.string.si_photo_capture_instruction_good_light_title,
                    R.string.si_photo_capture_instruction_good_light_subtitle,
                ),
                Triple(
                    R.drawable.si_photo_capture_instruction_clear_image,
                    R.string.si_photo_capture_instruction_clear_image_title,
                    R.string.si_photo_capture_instruction_clear_image_subtitle,
                ),
                Triple(
                    R.drawable.si_photo_capture_instruction_remove_obstructions,
                    R.string.si_photo_capture_instruction_remove_obstructions_title,
                    R.string.si_photo_capture_instruction_remove_obstructions_subtitle,
                ),
            )
            instructions.forEach { (imageId, title, subtitle) ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = imageId),
                        contentDescription = null,
                    )
                    Column(modifier = Modifier.padding(start = 16.dp)) {
                        Text(
                            text = stringResource(title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = stringResource(subtitle),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
                Spacer(modifier = Modifier.size(24.dp))
            }
        },
        pinnedContent = {
            CameraPermissionButton(
                text = stringResource(R.string.si_smart_selfie_instruction_ready_button),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("smart_selfie_instructions_ready_button"),
                onGranted = onInstructionsAcknowledged,
            )
            if (showAttribution) {
                SmileIDAttribution()
            }
        },
        modifier = modifier,
        columnWidth = 320.dp,
    )
}

@SmilePreviews
@Composable
private fun SmartSelfieInstructionsScreenPreview() {
    Preview {
        SmartSelfieInstructionsScreen()
    }
}
