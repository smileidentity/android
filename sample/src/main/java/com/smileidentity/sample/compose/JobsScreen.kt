package com.smileidentity.sample.compose

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.graphics.StrokeCap.Companion.Round
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smileidentity.compose.preview.SmilePreviews
import com.smileidentity.models.Actions
import com.smileidentity.sample.R

@Composable
fun JobsScreen(
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier = modifier.fillMaxSize()) {
        item {
            JobListItem(
                sourceIcon = {
                    Image(
                        painter = painterResource(
                            id = com.smileidentity.R.drawable.si_doc_v_instructions_hero,
                        ),
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                    )
                },
                timestamp = "7/7/23 09:15 AM",
                jobElapsedTime = "5m 17s",
                jobType = "Document Verification",
                jobStatus = null,
                jobMessage = null,
            )
        }
        item {
            JobListItem(
                sourceIcon = {
                    Image(
                        painter = painterResource(
                            id = com.smileidentity.R.drawable.si_smart_selfie_instructions_hero,
                        ),
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                    )
                },
                timestamp = "7/6/23 12:08 PM",
                jobElapsedTime = "7s",
                jobType = "SmartSelfie™ Authentication",
                jobStatus = "Rejected",
                jobMessage = "Failed Authentication - Spoof Detected",
            )
        }
        item {
            JobListItem(
                sourceIcon = {
                    Image(
                        painter = painterResource(id = R.drawable.biometric_kyc),
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                    )
                },
                timestamp = "7/7/23 12:06 PM",
                jobElapsedTime = "9s",
                jobType = "Biometric KYC",
                jobStatus = "Provisional Approval",
                jobMessage = "Provisional Enroll - Under Review",
            )
        }
        item {
            JobListItem(
                sourceIcon = {
                    Image(
                        painter = painterResource(
                            id = com.smileidentity.R.drawable.si_smart_selfie_instructions_hero,
                        ),
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                    )
                },
                timestamp = "7/6/23 12:04 PM",
                jobElapsedTime = "1m 52s",
                jobType = "SmartSelfie™ Enrollment",
                jobStatus = "Approved",
                jobMessage = "Enroll User",
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun JobListItem(
    sourceIcon: @Composable () -> Unit,
    timestamp: String,
    jobElapsedTime: String,
    jobType: String,
    jobStatus: String?,
    jobMessage: String?,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    Card(
        onClick = { expanded = !expanded },
        modifier = modifier
            .animateContentSize()
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        ListItem(
            leadingContent = sourceIcon,
            overlineContent = { Text("$timestamp • $jobElapsedTime") },
            headlineContent = { Text(jobType) },
            supportingContent = {
                if (jobMessage != null) {
                    Text(jobMessage)
                }
                if (expanded) {
                    JobListItemAdditionalDetails()
                }
                if (jobStatus == null) {
                    Spacer(modifier = Modifier.size(4.dp))
                    LinearProgressIndicator(strokeCap = Round)
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
                    val animatedProgress = animateFloatAsState(
                        targetValue = if (expanded) 180f else 0f,
                        animationSpec = spring(),
                        label = "Dropdown Icon Rotation",
                    ).value
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
                    Text(
                        text = jobStatus ?: "Processing",
                        fontWeight = FontWeight.Thin,
                        fontSize = 8.sp,
                        modifier = Modifier
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.onSurface,
                                RoundedCornerShape(4.dp),
                            )
                            .padding(4.dp),
                    )
                }
            },
        )
    }
}

@Suppress("UnusedReceiverParameter")
@Composable
private fun ColumnScope.JobListItemAdditionalDetails(
    userId: String? = null,
    jobId: String? = null,
    smileJobId: String? = null,
    resultCode: String? = null,
    resultText: String? = null,
    code: String? = null,
    actions: Actions? = null,
) {
    // TODO
    Text("User ID: $userId")
    Text("Job ID: $jobId")
    Text("Smile Job ID: $smileJobId")
    Text("Result Code: $resultCode")
    Text("Result Text: $resultText")
    Text("Code: $code")
    Text("Actions: $actions")
}

@Composable
@SmilePreviews
fun JobListItemPreview() {
    JobListItem(
        sourceIcon = {
            Image(
                painter = painterResource(
                    id = com.smileidentity.R.drawable.si_smart_selfie_instructions_hero,
                ),
                contentDescription = null,
                modifier = Modifier.size(64.dp),
            )
        },
        timestamp = "7/6/23 12:04 PM",
        jobElapsedTime = "1m 52s",
        jobType = "SmartSelfie™ Enrollment",
        jobStatus = null,
        jobMessage = "Enroll User",
    )
}
