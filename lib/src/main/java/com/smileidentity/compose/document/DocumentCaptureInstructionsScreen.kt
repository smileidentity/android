package com.smileidentity.compose.document

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.smileidentity.compose.components.SmileIDAttribution
import com.smileidentity.compose.preview.Preview
import com.smileidentity.compose.preview.SmilePreviews

@Composable
fun DocumentCaptureInstructionsScreen(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    showAttribution: Boolean = true,
    allowPhotoFromGallery: Boolean = false,
    showSkipButton: Boolean = true,
    onSkip: () -> Unit = { },
    onInstructionsAcknowledgedSelectFromGallery: () -> Unit = { },
    onInstructionsAcknowledgedTakePhoto: () -> Unit,
) {
    BottomPinnedColumn(
        scrollableContent = {
            Image(
                painter = painterResource(id = R.drawable.si_doc_v_instructions_hero),
                contentDescription = null,
                modifier = Modifier
                    .size(98.dp)
                    .padding(top = 8.dp),
            )
            Text(
                text = title,
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = subtitle,
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
            if (showSkipButton) {
                TextButton(onClick = onSkip) {
                    Text(
                        text = stringResource(id = R.string.si_doc_v_instruction_skip_back_id),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                    )
                }
            }
            CameraPermissionButton(
                text = stringResource(R.string.si_doc_v_instruction_ready_button),
                onGranted = onInstructionsAcknowledgedTakePhoto,
                modifier = Modifier.fillMaxWidth(),
            )
            if (allowPhotoFromGallery) {
                OutlinedButton(
                    onClick = onInstructionsAcknowledgedSelectFromGallery,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.si_doc_v_instruction_upload_button))
                }
            }
            if (showAttribution) {
                SmileIDAttribution()
            }
        },
        columnWidth = 320.dp,
        modifier = modifier.testTag("document_capture_instructions_screen"),
    )
}

@SmilePreviews
@Composable
fun DocumentCaptureInstructionsScreenPreview() {
    Preview {
        Surface {
            DocumentCaptureInstructionsScreen(
                title = stringResource(R.string.si_doc_v_instruction_title),
                subtitle = stringResource(R.string.si_verify_identity_instruction_subtitle),
                showAttribution = true,
                allowPhotoFromGallery = true,
                onInstructionsAcknowledgedSelectFromGallery = {},
                onInstructionsAcknowledgedTakePhoto = {},
                onSkip = {},
            )
        }
    }
}
