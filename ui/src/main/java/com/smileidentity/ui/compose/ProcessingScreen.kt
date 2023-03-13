package com.smileidentity.ui.compose

import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.smileidentity.ui.R
import com.smileidentity.ui.theme.SmileIdentityAffirmationColor
import com.smileidentity.ui.theme.SmileIdentityLightBlue

@Composable
fun ProcessingScreen(
    processingState: Boolean?,
    successTitle: String,
    successSubtitle: String,
    successIcon: Painter,
    errorTitle: String,
    errorSubtitle: String,
    errorIcon: Painter,
    continueButtonText: String,
    onContinue: () -> Unit,
    retryButtonText: String,
    onRetry: () -> Unit,
    closeButtonText: String,
    onClose: () -> Unit,
) {
    when (processingState) {
        null -> ProcessingInProgressScreen(
            icon = painterResource(R.drawable.si_smart_selfie_processing_hero),
            title = stringResource(R.string.si_smart_selfie_processing_title),
            subtitle = stringResource(R.string.si_smart_selfie_processing_subtitle),
        )
        true -> ProcessingSuccessScreen(
            icon = successIcon,
            title = successTitle,
            subtitle = successSubtitle,
            continueButtonText = continueButtonText,
            onContinue = onContinue,
        )
        false -> ProcessingErrorScreen(
            icon = errorIcon,
            title = errorTitle,
            subtitle = errorSubtitle,
            retryButtonText = retryButtonText,
            onRetry = onRetry,
            closeButtonText = closeButtonText,
            onClose = onClose,
        )
    }
}

@VisibleForTesting
@Composable
internal fun ProcessingInProgressScreen(
    icon: Painter,
    title: String,
    subtitle: String,
    progressIndicatorColor: Color = SmileIdentityAffirmationColor,
) {
    AlertDialog(
        icon = { Icon(painter = icon, contentDescription = null, tint = Color.Unspecified) },
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                )
                LinearProgressIndicator(
                    color = progressIndicatorColor,
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .fillMaxWidth(),
                )
            }
        },
        confirmButton = { /* */ },
        onDismissRequest = { /* Do nothing since we have disabled back press and click outside */ },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
        modifier = Modifier.testTag("processing_screen_in_progress"),
    )
}

@VisibleForTesting
@Composable
internal fun ProcessingSuccessScreen(
    icon: Painter,
    title: String,
    subtitle: String,
    continueButtonText: String,
    onContinue: () -> Unit,
) {
    AlertDialog(
        icon = { Icon(painter = icon, contentDescription = null, tint = Color.Unspecified) },
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
            )
        },
        confirmButton = {
            Button(
                onClick = onContinue,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = SmileIdentityLightBlue),
            ) { Text(text = continueButtonText) }
        },
        onDismissRequest = { /* Do nothing since we have disabled back press and click outside */ },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
        modifier = Modifier.testTag("processing_screen_success"),
    )
}

@VisibleForTesting
@Composable
internal fun ProcessingErrorScreen(
    icon: Painter,
    title: String,
    subtitle: String,
    retryButtonText: String,
    onRetry: () -> Unit,
    closeButtonText: String,
    onClose: () -> Unit,
) {
    AlertDialog(
        icon = { Icon(painter = icon, contentDescription = null, tint = Color.Unspecified) },
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
            )
        },
        confirmButton = {
            Button(
                onClick = onRetry,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = SmileIdentityLightBlue),
            ) { Text(text = retryButtonText) }
        },
        dismissButton = {
            TextButton(
                onClick = onClose,
                modifier = Modifier.fillMaxWidth(),
            ) { Text(text = closeButtonText) }
        },
        onDismissRequest = { /* Do nothing since we have disabled back press and click outside */ },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
        modifier = Modifier.testTag("processing_screen_error"),
    )
}

@Preview
@Composable
private fun PreviewProcessingInProgressScreen() {
    ProcessingInProgressScreen(
        icon = painterResource(R.drawable.si_smart_selfie_processing_hero),
        title = stringResource(R.string.si_smart_selfie_processing_title),
        subtitle = stringResource(R.string.si_smart_selfie_processing_subtitle),
    )
}

@Preview
@Composable
private fun PreviewProcessingSuccessScreen() {
    ProcessingSuccessScreen(
        icon = painterResource(R.drawable.si_processing_success),
        title = stringResource(R.string.si_smart_selfie_processing_success_title),
        subtitle = stringResource(R.string.si_smart_selfie_processing_success_subtitle),
        continueButtonText = stringResource(R.string.si_smart_selfie_processing_continue_button),
        onContinue = {},
    )
}

@Preview
@Composable
private fun PreviewProcessingErrorScreen() {
    ProcessingErrorScreen(
        icon = painterResource(R.drawable.si_processing_error),
        title = stringResource(R.string.si_smart_selfie_processing_error_title),
        subtitle = stringResource(R.string.si_smart_selfie_processing_error_subtitle),
        retryButtonText = stringResource(R.string.si_smart_selfie_processing_retry_button),
        onRetry = {},
        closeButtonText = stringResource(R.string.si_smart_selfie_processing_close_button),
        onClose = {},
    )
}
