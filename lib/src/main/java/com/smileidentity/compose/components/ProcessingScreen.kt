package com.smileidentity.compose.components

import android.os.Parcelable
import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.smileidentity.R
import com.smileidentity.SmileID
import com.smileidentity.SmileIDOptIn
import com.smileidentity.compose.preview.Preview
import com.smileidentity.compose.preview.SmilePreviews
import com.smileidentity.compose.theme.colorScheme
import com.smileidentity.compose.theme.typography
import kotlinx.android.parcel.Parcelize
import kotlinx.serialization.Serializable

@SmileIDOptIn
@Serializable
@Parcelize
enum class ProcessingState : Parcelable {
    InProgress,
    Success,
    Error,
}

/**
 * This screen represents a generic Processing state. It has 3 sub-states: In Progress, Success, and
 * Error. These sub-states are represented by the [processingState] parameter.
 *
 * Note: because we accept [Painter] for the icons, this may cause some extra recompositions.
 * However, this is done so that partners can have the flexibility to replace icons with normal
 * images, if they so choose (as opposed to an optimization where we accept only [ImageVector],
 * which is immutable)
 *
 * @param processingState The state of the processing.
 * @param inProgressTitle The title to display when the processing is in progress.
 * @param inProgressSubtitle The subtitle to display when the processing is in progress.
 * @param inProgressIcon The icon to display when the processing is in progress.
 * @param successTitle The title to display when the processing is successful.
 * @param successSubtitle The subtitle to display when the processing is successful.
 * @param successIcon The icon to display when the processing is successful.
 * @param errorTitle The title to display when the processing is unsuccessful.
 * @param errorSubtitle The subtitle to display when the processing is unsuccessful.
 * @param errorIcon The icon to display when the processing is unsuccessful.
 * @param continueButtonText The text to display on the continue button when the processing is
 * successful.
 * @param onContinue The callback to invoke when the continue button is clicked.
 * @param retryButtonText The text to display on the retry button when the processing is
 * unsuccessful.
 * @param onRetry The callback to invoke when the retry button is clicked.
 * @param closeButtonText The text to display on the close button when the processing is
 * unsuccessful.
 * @param onClose The callback to invoke when the close button is clicked.
 */
@SmileIDOptIn
@Composable
fun ProcessingScreen(
    processingState: ProcessingState,
    inProgressTitle: String,
    inProgressSubtitle: String,
    inProgressIcon: Painter,
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
        ProcessingState.InProgress -> ProcessingInProgressScreen(
            icon = inProgressIcon,
            title = inProgressTitle,
            subtitle = inProgressSubtitle,
        )

        ProcessingState.Success -> ProcessingSuccessScreen(
            icon = successIcon,
            title = successTitle,
            subtitle = successSubtitle,
            continueButtonText = continueButtonText,
            onContinue = onContinue,
        )

        ProcessingState.Error -> ProcessingErrorScreen(
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
    progressIndicatorColor: Color = MaterialTheme.colorScheme.tertiary,
) {
    AlertDialog(
        icon = { Icon(painter = icon, contentDescription = null, tint = Color.Unspecified) },
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.ExtraBold,
            )
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
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
                fontWeight = FontWeight.ExtraBold,
            )
        },
        text = {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            Button(
                onClick = onContinue,
                modifier = Modifier
                    .testTag("processing_screen_continue_button")
                    .fillMaxWidth(),
            ) { Text(text = continueButtonText) }
        },
        onDismissRequest = onContinue,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true),
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
                fontWeight = FontWeight.ExtraBold,
            )
        },
        text = {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            Button(
                onClick = onRetry,
                modifier = Modifier
                    .testTag("processing_screen_retry_button")
                    .fillMaxWidth(),
            ) { Text(text = retryButtonText) }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onClose,
                modifier = Modifier
                    .testTag("processing_screen_close_button")
                    .fillMaxWidth(),
            ) { Text(text = closeButtonText) }
        },
        onDismissRequest = onClose,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = false),
        modifier = Modifier.testTag("processing_screen_error"),
    )
}

@SmilePreviews
@Composable
private fun ProcessingInProgressScreenPreview() {
    Preview {
        ProcessingInProgressScreen(
            icon = painterResource(R.drawable.si_smart_selfie_processing_hero),
            title = stringResource(R.string.si_smart_selfie_processing_title),
            subtitle = stringResource(R.string.si_smart_selfie_processing_subtitle),
        )
    }
}

@SmilePreviews
@Composable
private fun ProcessingSuccessScreenPreview() {
    Preview {
        ProcessingSuccessScreen(
            icon = painterResource(R.drawable.si_processing_success),
            title = stringResource(R.string.si_smart_selfie_processing_success_title),
            subtitle = stringResource(R.string.si_smart_selfie_processing_success_subtitle),
            continueButtonText = stringResource(R.string.si_continue),
            onContinue = {},
        )
    }
}

@Preview
@Composable
private fun ProcessingErrorScreenPreview() {
    MaterialTheme(colorScheme = SmileID.colorScheme, typography = SmileID.typography) {
        ProcessingErrorScreen(
            icon = painterResource(R.drawable.si_processing_error),
            title = stringResource(R.string.si_smart_selfie_processing_error_title),
            subtitle = stringResource(R.string.si_processing_error_subtitle),
            retryButtonText = stringResource(R.string.si_smart_selfie_processing_retry_button),
            onRetry = {},
            closeButtonText = stringResource(R.string.si_smart_selfie_processing_close_button),
            onClose = {},
        )
    }
}
