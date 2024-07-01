package com.smileidentity.sample.compose.jobs

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidthIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.smileidentity.R
import com.smileidentity.compose.preview.SmilePreviews
import com.smileidentity.models.JobType
import com.smileidentity.models.JobType.BVN
import com.smileidentity.models.JobType.BiometricKyc
import com.smileidentity.models.JobType.DocumentVerification
import com.smileidentity.models.JobType.EnhancedDocumentVerification
import com.smileidentity.models.JobType.EnhancedKyc
import com.smileidentity.models.JobType.SmartSelfieAuthentication
import com.smileidentity.models.JobType.SmartSelfieEnrollment
import com.smileidentity.sample.compose.SmileIDTheme
import com.smileidentity.sample.compose.components.ErrorScreen
import com.smileidentity.sample.label
import com.smileidentity.sample.model.Job
import kotlinx.collections.immutable.ImmutableList
import timber.log.Timber

@Composable
fun JobsListScreen(jobs: ImmutableList<Job>, modifier: Modifier = Modifier) {
    if (jobs.isEmpty()) {
        ErrorScreen(
            errorText = stringResource(com.smileidentity.sample.R.string.jobs_no_jobs_found),
            onRetry = {},
        )
        return
    }
    LazyColumn(modifier = modifier.fillMaxSize()) {
        items(jobs) {
            @DrawableRes
            val iconRes = when (it.jobType) {
                SmartSelfieEnrollment ->
                    com.smileidentity.sample.R.drawable.smart_selfie_enrollment_v2
                SmartSelfieAuthentication ->
                    com.smileidentity.sample.R.drawable.smart_selfie_authentication_v2

                DocumentVerification -> com.smileidentity.sample.R.drawable.doc_v
                EnhancedDocumentVerification -> R.drawable.si_smart_selfie_instructions_hero
                BiometricKyc -> com.smileidentity.sample.R.drawable.biometric_kyc
                EnhancedKyc -> com.smileidentity.sample.R.drawable.enhanced_kyc
                BVN -> com.smileidentity.sample.R.drawable.biometric_kyc
                JobType.Unknown -> {
                    Timber.e("Unknown job type")
                    R.drawable.si_smart_selfie_instructions_hero
                }
            }
            JobListItem(
                sourceIcon = {
                    Image(
                        painter = painterResource(iconRes),
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                    )
                },
                timestamp = it.timestamp,
                jobType = stringResource(it.jobType.label),
                isProcessing = !it.jobComplete,
                resultText = it.resultText,
            ) {
                JobListItemAdditionalDetails(
                    userId = it.userId,
                    jobId = it.jobId,
                    smileJobId = it.smileJobId,
                    resultCode = it.resultCode,
                    code = it.code,
                )
            }
        }
    }
}

@Composable
private fun JobListItem(
    sourceIcon: @Composable () -> Unit,
    timestamp: String,
    jobType: String,
    isProcessing: Boolean,
    resultText: String?,
    modifier: Modifier = Modifier,
    expandedContent: @Composable ColumnScope.() -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Card(
        onClick = { expanded = !expanded },
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        ListItem(
            leadingContent = sourceIcon,
            overlineContent = { Text(timestamp) },
            headlineContent = { Text(jobType) },
            supportingContent = {
                Column {
                    if (resultText != null) {
                        SelectionContainer {
                            Text(resultText, style = MaterialTheme.typography.labelLarge)
                        }
                    }
                    AnimatedVisibility(visible = expanded) {
                        Column(modifier = Modifier.animateContentSize()) {
                            expandedContent()
                        }
                    }
                    if (isProcessing) {
                        Spacer(modifier = Modifier.size(4.dp))
                        LinearProgressIndicator(strokeCap = StrokeCap.Round)
                    }
                }
            },
            trailingContent = {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .requiredWidthIn(max = 64.dp),
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    val halfCircleRotationDegrees = 180f
                    val animatedProgress by animateFloatAsState(
                        targetValue = if (expanded) halfCircleRotationDegrees else 0f,
                        animationSpec = spring(),
                        label = "Dropdown Icon Rotation",
                    )
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.background,
                        modifier = Modifier
                            .size(16.dp)
                            .rotate(animatedProgress)
                            .background(
                                color = MaterialTheme.colorScheme.onBackground,
                                shape = CircleShape,
                            ),
                    )
                }
            },
        )
    }
}

@Suppress("UnusedReceiverParameter")
@Composable
private fun ColumnScope.JobListItemAdditionalDetails(
    userId: String?,
    jobId: String?,
    smileJobId: String?,
    resultCode: String?,
    code: String?,
) {
    JobMetadataItem(
        label = stringResource(com.smileidentity.sample.R.string.jobs_detail_user_id_label),
        value = userId,
    )
    JobMetadataItem(
        label = stringResource(com.smileidentity.sample.R.string.jobs_detail_job_id_label),
        value = jobId,
    )
    JobMetadataItem(
        label = stringResource(com.smileidentity.sample.R.string.jobs_detail_smile_job_id_label),
        value = smileJobId,
    )
    JobMetadataItem(
        label = stringResource(com.smileidentity.sample.R.string.jobs_detail_result_code_label),
        value = resultCode,
    )
    JobMetadataItem(
        label = stringResource(com.smileidentity.sample.R.string.jobs_detail_code_label),
        value = code,
    )
}

@Composable
private fun JobMetadataItem(label: String, value: String?) {
    if (value != null) {
        Column {
            Spacer(modifier = Modifier.size(8.dp))
            Text(text = label, style = MaterialTheme.typography.labelMedium)
            Spacer(modifier = Modifier.size(2.dp))
            SelectionContainer {
                Text(value, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
@SmilePreviews
private fun JobListItemPreview(modifier: Modifier = Modifier) {
    SmileIDTheme {
        JobListItem(
            sourceIcon = {
                Image(
                    painter = painterResource(
                        id = R.drawable.si_smart_selfie_instructions_hero,
                    ),
                    contentDescription = null,
                    modifier = modifier.size(64.dp),
                )
            },
            timestamp = "7/6/23 12:04 PM",
            jobType = "SmartSelfie™ Enrollment",
            isProcessing = true,
            resultText = "Enroll User",
        ) { }
    }
}

@Composable
@SmilePreviews
private fun JobListItemAdditionalDetailsPreview() {
    SmileIDTheme {
        Column {
            JobListItemAdditionalDetails(
                userId = "1234567890",
                jobId = "1234567890",
                smileJobId = "1234567890",
                resultCode = "1234567890",
                code = "1234567890",
            )
        }
    }
}
