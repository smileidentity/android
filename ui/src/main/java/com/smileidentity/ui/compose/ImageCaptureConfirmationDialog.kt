package com.smileidentity.ui.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush.Companion.linearGradient
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.BrushPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.smileidentity.ui.R

/**
 * A dialog that shows a preview of the image captured by the camera and asks the user to confirm
 * that the image is of acceptable quality.
 *
 * @param titleText The title of the dialog
 * @param subtitleText The subtitle of the dialog
 * @param painter The image that the user captured and needs to confirm
 * @param confirmButtonText The text of the button that confirms the image is of acceptable quality
 * @param onConfirm The callback to invoke when the user confirms the image is of acceptable quality
 * @param retakeButtonText The text of the button that allows the user to retake the image
 * @param onRetake The callback to invoke when the user wants to retake the image
 */
@Composable
fun ImageCaptureConfirmationDialog(
    titleText: String,
    subtitleText: String,
    painter: Painter,
    confirmButtonText: String,
    onConfirm: () -> Unit,
    retakeButtonText: String,
    onRetake: () -> Unit,
) {
    AlertDialog(
        title = { Text(titleText, textAlign = TextAlign.Center) },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = subtitleText,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 32.dp),
                )
                Image(
                    painter = painter,
                    contentDescription = null,
                    contentScale = ContentScale.FillHeight,
                    modifier = Modifier.height(256.dp).clip(RoundedCornerShape(16.dp)).scale(1.25f),
                )
            }
        },
        onDismissRequest = { /* Do nothing since we have disabled back press and click outside */ },
        confirmButton = { TextButton(onClick = onConfirm) { Text(confirmButtonText) } },
        dismissButton = { TextButton(onClick = onRetake) { Text(retakeButtonText) } },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
    )
}

@Preview
@Composable
private fun PreviewImageCaptureConfirmationDialog() {
    ImageCaptureConfirmationDialog(
        titleText = stringResource(R.string.si_smart_selfie_confirmation_dialog_title),
        subtitleText = stringResource(R.string.si_smart_selfie_confirmation_dialog_subtitle),
        painter = BrushPainter(
            brush = linearGradient(listOf(Color(0xFF11B33E), Color(0xFF1B73AD))),
        ),
        confirmButtonText = stringResource(R.string.si_smart_selfie_confirmation_dialog_confirm_button),
        onConfirm = {},
        retakeButtonText = stringResource(R.string.si_smart_selfie_confirmation_dialog_retake_button),
        onRetake = {},
    )
}
