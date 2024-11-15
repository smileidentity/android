package com.smileidentity.compose.document

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly
import androidx.annotation.DrawableRes
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.smileidentity.R
import com.smileidentity.SmileIDCrashReporting
import com.smileidentity.compose.components.BottomPinnedColumn
import com.smileidentity.compose.components.CameraPermissionButton
import com.smileidentity.compose.components.SmileIDAttribution
import com.smileidentity.compose.nav.encodeUrl
import com.smileidentity.compose.preview.Preview
import com.smileidentity.compose.preview.SmilePreviews
import com.smileidentity.util.isValidDocumentImage
import com.smileidentity.util.toast
import timber.log.Timber

/**
 * Instructions for taking a good quality document photo. Optionally, allows user to select a
 * photo from their gallery.
 */
@Composable
fun DocumentCaptureInstructionsScreen(
    modifier: Modifier = Modifier,
    @DrawableRes heroImage: Int = R.drawable.si_doc_v_front_hero,
    title: String = stringResource(R.string.si_doc_v_instruction_title),
    subtitle: String = stringResource(R.string.si_verify_identity_instruction_subtitle),
    showAttribution: Boolean = true,
    allowPhotoFromGallery: Boolean = false,
    showSkipButton: Boolean = true,
    onSkip: () -> Unit = { },
    onInstructionsAcknowledgedSelectFromGallery: (String?) -> Unit = { },
    onInstructionsAcknowledgedTakePhoto: () -> Unit,
) {
    val context = LocalContext.current
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            Timber.v("selectedUri: $uri")
            if (uri == null) {
                Timber.e("selectedUri is null")
                context.toast(R.string.si_doc_v_capture_error_subtitle)
                return@rememberLauncherForActivityResult
            }
            if (isValidDocumentImage(context, uri)) {
                onInstructionsAcknowledgedSelectFromGallery(encodeUrl(uri.toString()))
            } else {
                SmileIDCrashReporting.hub.addBreadcrumb("Gallery upload document image too small")
                context.toast(R.string.si_doc_v_capture_error_subtitle)
            }
        },
    )

    BottomPinnedColumn(
        scrollableContent = {
            Image(
                painter = painterResource(id = heroImage),
                contentDescription = null,
                modifier = Modifier
                    .size(192.dp)
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
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("document_instructions_ready_button"),
            )
            if (allowPhotoFromGallery) {
                OutlinedButton(
                    onClick = {
                        SmileIDCrashReporting.hub.addBreadcrumb(
                            "Selecting document photo from gallery",
                        )
                        photoPickerLauncher.launch(PickVisualMediaRequest(ImageOnly))
                    },
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
private fun DocumentCaptureInstructionsScreenPreview() {
    Preview {
        Surface {
            DocumentCaptureInstructionsScreen(
                heroImage = R.drawable.si_doc_v_front_hero,
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
