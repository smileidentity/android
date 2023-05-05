package com.smileidentity.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.smileidentity.R
import com.smileidentity.SmileID
import com.smileidentity.compose.theme.colorScheme
import com.smileidentity.compose.theme.typography

@Composable
fun DocumentCaptureInstructionsScreen(
    showAttribution: Boolean = true,
    allowPhotoFromGallery: Boolean = false,
    onInstructionsAcknowledgedSelectFromGallery: () -> Unit = { },
    onInstructionsAcknowledgedTakePhoto: () -> Unit,
) {
    val columnWidth = 320.dp
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
            modifier = Modifier
                .fillMaxHeight()
                .widthIn(max = columnWidth)
                .verticalScroll(rememberScrollState())
                .weight(1f),
        ) {
            Image(
                painter = painterResource(id = R.drawable.si_doc_v_instructions_hero),
                contentDescription = null,
                modifier = Modifier
                    .size(128.dp)
                    .padding(top = 8.dp),
            )
            Text(
                text = stringResource(R.string.si_doc_v_instruction_title),
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
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .width(columnWidth)
                .padding(8.dp),
        ) {
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
        }
    }
}

@Preview
@Composable
fun DocumentCaptureInstructionsScreenPreview() {
    MaterialTheme(colorScheme = SmileID.colorScheme, typography = SmileID.typography) {
        Surface {
            DocumentCaptureInstructionsScreen(
                showAttribution = true,
                allowPhotoFromGallery = true,
                onInstructionsAcknowledgedSelectFromGallery = {},
                onInstructionsAcknowledgedTakePhoto = {},
            )
        }
    }
}
