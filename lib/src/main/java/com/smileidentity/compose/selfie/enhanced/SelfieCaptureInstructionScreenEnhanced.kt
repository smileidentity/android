package com.smileidentity.compose.selfie.enhanced

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smileidentity.R
import com.smileidentity.compose.components.AnimatedInstructions
import com.smileidentity.compose.components.ContinueButton
import com.smileidentity.compose.components.SmileIDAttribution
import com.smileidentity.compose.preview.Preview
import com.smileidentity.compose.preview.SmilePreviews

@Composable
fun SelfieCaptureInstructionScreenEnhanced(
    modifier: Modifier = Modifier,
    showAttribution: Boolean = true,
    onInstructionsAcknowledged: () -> Unit = { },
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .weight(1f),
        ) {
            AnimatedInstructions(
                modifier = Modifier
                    .size(256.dp)
                    .padding(bottom = 16.dp),
            )
            Text(
                text = stringResource(R.string.si_smart_selfie_enhanced_instructions),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                    textAlign = TextAlign.Center,
                ),
                modifier = Modifier
                    .padding(24.dp)
                    .testTag("smart_selfie_instructions_enhanced_instructions_text"),
            )
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
        ) {
            ContinueButton(
                buttonText = stringResource(R.string.si_smart_selfie_enhanced_get_started),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("smart_selfie_instructions_enhanced_get_started_button"),
                onClick = onInstructionsAcknowledged,
            )
            if (showAttribution) {
                SmileIDAttribution()
            }
        }
    }
}

@SmilePreviews
@Composable
private fun SelfieCaptureInstructionScreenEnhancedPreview() {
    Preview {
        Column {
            SelfieCaptureInstructionScreenEnhanced(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Gray),
            ) {}
        }
    }
}
