package com.smileidentity.compose.selfie.v2

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smileidentity.compose.components.BottomPinnedColumn
import com.smileidentity.viewmodel.SmartSelfieV2ViewModel

/**
 * UI to show a list of Sliders to tune the parameters for the selfie capture. Meant to be removed
 * once the parameter values are finalized.
 */
@Composable
fun ParameterTuningScreen(
    viewModel: SmartSelfieV2ViewModel,
    modifier: Modifier = Modifier,
    onNextClicked: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    BottomPinnedColumn(
        modifier = modifier.padding(8.dp),
        showDivider = true,
        pinnedContent = {
            Button(onClick = onNextClicked, modifier = Modifier.fillMaxWidth()) {
                Text("Continue")
            }
        },
        scrollableContent = {
            Text(
                text = "Parameter Tuning",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )

            Text(
                "Active Liveness Stability Time (ms): ${uiState.LIVENESS_STABILITY_TIME_MS.toInt()}",
                modifier = Modifier.padding(top = 8.dp),
                style = MaterialTheme.typography.labelLarge,
            )
            Text(
                "Time that the face must be pointing in the requested direction before being considered satisfied",
                style = MaterialTheme.typography.bodySmall,
            )
            Slider(
                value = uiState.LIVENESS_STABILITY_TIME_MS,
                onValueChange = viewModel::onLivenessStabilityTimeMsUpdated,
                valueRange = 0f..3000f,
            )

            Text(
                "LR Halfway Qualifying Angles (°): ${uiState.MIDWAY_LR_ANGLE_MIN.toInt()}-${uiState.MIDWAY_LR_ANGLE_MAX.toInt()}",
                modifier = Modifier.padding(top = 8.dp),
                style = MaterialTheme.typography.labelLarge,
            )
            Text(
                "Head angle range that determines when we perform the left/right midpoint capture",
                style = MaterialTheme.typography.bodySmall,
            )
            RangeSlider(
                value = uiState.MIDWAY_LR_ANGLE_MIN..uiState.MIDWAY_LR_ANGLE_MAX,
                onValueChange = { range ->
                    viewModel.onMidwayLrAngleMinUpdated(range.start)
                    viewModel.onMidwayLrAngleMaxUpdated(range.endInclusive)
                },
                valueRange = 0f..90f,
            )

            Text(
                "LR Qualifying Angle (°): ${uiState.END_LR_ANGLE_MIN.toInt()}-${uiState.END_LR_ANGLE_MAX.toInt()}",
                modifier = Modifier.padding(top = 8.dp),
                style = MaterialTheme.typography.labelLarge,
            )
            Text(
                "Head angle range that determines when we perform the left/right end capture",
                style = MaterialTheme.typography.bodySmall,
            )
            RangeSlider(
                value = uiState.END_LR_ANGLE_MIN..uiState.END_LR_ANGLE_MAX,
                onValueChange = { range ->
                    viewModel.onEndLrAngleMinUpdated(range.start)
                    viewModel.onEndLrAngleMaxUpdated(range.endInclusive)
                },
                valueRange = 0f..90f,
            )

            Text(
                "Up Halfway Qualifying Angles Min (°): ${uiState.MIDWAY_UP_ANGLE_MIN.toInt()}-${uiState.MIDWAY_UP_ANGLE_MAX.toInt()}",
                modifier = Modifier.padding(top = 8.dp),
                style = MaterialTheme.typography.labelLarge,
            )
            Text(
                "Head angle range that determines when we perform the midpoint capture when looking up",
                style = MaterialTheme.typography.bodySmall,
            )
            RangeSlider(
                value = uiState.MIDWAY_UP_ANGLE_MIN..uiState.MIDWAY_UP_ANGLE_MAX,
                onValueChange = { range ->
                    viewModel.onMidwayUpAngleMinUpdated(range.start)
                    viewModel.onMidwayUpAngleMaxUpdated(range.endInclusive)
                },
                valueRange = 0f..90f,
            )

            Text(
                "Up Qualifying Angle (°): ${uiState.END_UP_ANGLE_MIN.toInt()}-${uiState.END_UP_ANGLE_MAX.toInt()}",
                modifier = Modifier.padding(top = 8.dp),
                style = MaterialTheme.typography.labelLarge,
            )
            Text(
                "Head angle range that determines when we perform the end capture when looking up",
                style = MaterialTheme.typography.bodySmall,
            )
            RangeSlider(
                value = uiState.END_UP_ANGLE_MIN..uiState.END_UP_ANGLE_MAX,
                onValueChange = { range ->
                    viewModel.onEndUpAngleMinUpdated(range.start)
                    viewModel.onEndUpAngleMaxUpdated(range.endInclusive)
                },
                valueRange = 0f..90f,
            )

            Text(
                "Orthogonal Angle Buffer (°): ${uiState.ORTHOGONAL_ANGLE_BUFFER.toInt()}",
                modifier = Modifier.padding(top = 8.dp),
                style = MaterialTheme.typography.labelLarge,
            )
            Text(
                "The angle buffer around the orthogonal angle (i.e. how much you can look up/down when asked to look left/right and vice versa)",
                style = MaterialTheme.typography.bodySmall,
            )
            Slider(
                value = uiState.ORTHOGONAL_ANGLE_BUFFER,
                onValueChange = viewModel::onOrthogonalAngleBufferUpdated,
                valueRange = 0f..90f,
            )

            Text(
                "Selfie Quality History: ${uiState.SELFIE_QUALITY_HISTORY_LENGTH}",
                modifier = Modifier.padding(top = 8.dp),
                style = MaterialTheme.typography.labelLarge,
            )
            Text(
                "Number of images to average selfie image quality model score over",
                style = MaterialTheme.typography.bodySmall,
            )
            Slider(
                value = uiState.SELFIE_QUALITY_HISTORY_LENGTH,
                onValueChange = viewModel::onSelfieQualityHistoryLengthUpdated,
                valueRange = 2f..20f,
                steps = 18,
            )

            Text(
                "Selfie Quality Threshold: ${uiState.FACE_QUALITY_THRESHOLD}",
                modifier = Modifier.padding(top = 8.dp),
                style = MaterialTheme.typography.labelLarge,
            )
            Text(
                "Min average score from selfie image quality model for a face to be considered valid",
                style = MaterialTheme.typography.bodySmall,
            )
            Slider(
                value = uiState.FACE_QUALITY_THRESHOLD,
                onValueChange = viewModel::onFaceQualityThresholdUpdated,
                valueRange = 0f..1f,
            )

            Text(
                "Active Liveness Forced Failure Timeout (s): ${(uiState.FORCED_FAILURE_TIMEOUT_MS / 1000).toInt()}",
                modifier = Modifier.padding(top = 8.dp),
                style = MaterialTheme.typography.labelLarge,
            )
            Text(
                "Time to wait before bypassing active liveness and force failing the job",
                style = MaterialTheme.typography.bodySmall,
            )
            Slider(
                value = uiState.FORCED_FAILURE_TIMEOUT_MS,
                onValueChange = viewModel::onForcedFailureTimeoutMsUpdated,
                valueRange = 0f..120_000f,
            )

            Text(
                "Intra Image min delay (ms): ${uiState.INTRA_IMAGE_MIN_DELAY_MS.toInt()}",
                modifier = Modifier.padding(top = 8.dp),
                style = MaterialTheme.typography.labelLarge,
            )
            Text(
                "Min time to wait between each image capture",
                style = MaterialTheme.typography.bodySmall,
            )
            Slider(
                value = uiState.INTRA_IMAGE_MIN_DELAY_MS,
                onValueChange = viewModel::onIntraImageMinDelayMsUpdated,
                valueRange = 0f..1000f,
            )

            Text(
                "No Face Reset Delay (ms): ${uiState.NO_FACE_RESET_DELAY_MS}",
                modifier = Modifier.padding(top = 8.dp),
                style = MaterialTheme.typography.labelLarge,
            )
            Text(
                "Time to wait after no face is detected before resetting",
                style = MaterialTheme.typography.bodySmall,
            )
            Slider(
                value = uiState.NO_FACE_RESET_DELAY_MS,
                onValueChange = viewModel::onNoFaceResetDelayMsUpdated,
                valueRange = 0f..2000f,
            )

            Text(
                "Min Face Fill Threshold (%): ${(uiState.MIN_FACE_FILL_THRESHOLD * 100).toInt()}",
                modifier = Modifier.padding(top = 8.dp),
                style = MaterialTheme.typography.labelLarge,
            )
            Text(
                "Min % of the frame that the face should fill (NB! the camera preview is zoomed in, but the percentage is wrt the full image)",
                style = MaterialTheme.typography.bodySmall,
            )
            Slider(
                value = uiState.MIN_FACE_FILL_THRESHOLD,
                onValueChange = viewModel::onMinFaceFillThresholdUpdated,
                valueRange = 0f..1f,
            )

            Text(
                "Max Face Fill Threshold (%): ${(uiState.MAX_FACE_FILL_THRESHOLD * 100).toInt()}",
                modifier = Modifier.padding(top = 8.dp),
                style = MaterialTheme.typography.labelLarge,
            )
            Text(
                "Max % of the frame that the face should fill (NB! the camera preview is zoomed in, but the percentage is wrt the full image)",
                style = MaterialTheme.typography.bodySmall,
            )
            Slider(
                value = uiState.MAX_FACE_FILL_THRESHOLD,
                onValueChange = viewModel::onMaxFaceFillThresholdUpdated,
                valueRange = 0f..1f,
            )

            Text(
                "Ignore faces smaller than (%): ${(uiState.IGNORE_FACES_SMALLER_THAN * 100).toInt()}",
                modifier = Modifier.padding(top = 8.dp),
                style = MaterialTheme.typography.labelLarge,
            )
            Text(
                "Face bounding boxes taking up less than this percentage of the frame will be ignored",
                style = MaterialTheme.typography.bodySmall,
            )
            Slider(
                value = uiState.IGNORE_FACES_SMALLER_THAN,
                onValueChange = viewModel::onIgnoreFacesSmallerThanUpdated,
                valueRange = 0f..0.1f,
            )

            Text(
                "Min Luminance Threshold: ${uiState.LUMINANCE_THRESHOLD}",
                modifier = Modifier.padding(top = 8.dp),
                style = MaterialTheme.typography.labelLarge,
            )
            Text(
                "Avg of the \"Y\" plane in YUV format (aka luma plane)",
                style = MaterialTheme.typography.bodySmall,
            )
            Slider(
                value = uiState.LUMINANCE_THRESHOLD,
                onValueChange = viewModel::onLuminanceThresholdUpdated,
                valueRange = 0f..255f,
            )

            Text(
                "Max Face Pitch Threshold (°): ${uiState.MAX_FACE_PITCH_THRESHOLD.toInt()}",
                modifier = Modifier.padding(top = 8.dp),
                style = MaterialTheme.typography.labelLarge,
            )
            Text(
                "Max pitch (x-axis) for *selfie capture only* for extreme head pose rejection",
                style = MaterialTheme.typography.bodySmall,
            )
            Slider(
                value = uiState.MAX_FACE_PITCH_THRESHOLD,
                onValueChange = viewModel::onMaxFacePitchThresholdUpdated,
                valueRange = 0f..90f,
            )

            Text(
                "Max Face Yaw Threshold (°): ${uiState.MAX_FACE_YAW_THRESHOLD.toInt()}",
                modifier = Modifier.padding(top = 8.dp),
                style = MaterialTheme.typography.labelLarge,
            )
            Text(
                "Max yaw (y-axis) for *selfie capture only* for extreme head pose rejection",
                style = MaterialTheme.typography.bodySmall,
            )
            Slider(
                value = uiState.MAX_FACE_YAW_THRESHOLD,
                onValueChange = viewModel::onMaxFaceYawThresholdUpdated,
                valueRange = 0f..90f,
            )

            Text(
                "Max Face Roll Threshold (°): ${uiState.MAX_FACE_ROLL_THRESHOLD.toInt()}",
                modifier = Modifier.padding(top = 8.dp),
                style = MaterialTheme.typography.labelLarge,
            )
            Text(
                "Max roll (z-axis) for *selfie capture only* for extreme head pose rejection",
                style = MaterialTheme.typography.bodySmall,
            )
            Slider(
                value = uiState.MAX_FACE_ROLL_THRESHOLD,
                onValueChange = viewModel::onMaxFaceRollThresholdUpdated,
                valueRange = 0f..90f,
            )

            Text(
                "Loading Indicator Delay (ms): ${uiState.LOADING_INDICATOR_DELAY_MS.toInt()}",
                modifier = Modifier.padding(top = 8.dp),
                style = MaterialTheme.typography.labelLarge,
            )
            Text(
                "Time to wait before showing the loading indicator (UI trick to make the network request feel faster)",
                style = MaterialTheme.typography.bodySmall,
            )
            Slider(
                value = uiState.LOADING_INDICATOR_DELAY_MS,
                onValueChange = viewModel::onLoadingIndicatorDelayMsUpdated,
                valueRange = 0f..3000f,
            )

            Text(
                "Completed Delay (ms): ${uiState.COMPLETED_DELAY_MS.toInt()}",
                modifier = Modifier.padding(top = 8.dp),
                style = MaterialTheme.typography.labelLarge,
            )
            Text(
                "Time after successful network submission to keep checkmark on screen before exiting",
                style = MaterialTheme.typography.bodySmall,
            )
            Slider(
                value = uiState.COMPLETED_DELAY_MS,
                onValueChange = viewModel::onCompletedDelayMsUpdated,
                valueRange = 0f..5000f,
            )
        },
    )
}
