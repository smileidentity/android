package com.smileidentity.compose.selfie.v3

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.smileidentity.R
import com.smileidentity.compose.components.LoadingButton
import com.smileidentity.compose.components.LottieInstruction
import com.smileidentity.compose.components.SmileIDAttribution

@Composable
fun SelfieCaptureScreenV3(modifier: Modifier = Modifier) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.tertiaryContainer)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        LottieInstruction(
            modifier = Modifier.size(200.dp),
        )
        Text(
            text = stringResource(R.string.si_smart_selfie_v3_instructions),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(24.dp),
        )
        LoadingButton(
            buttonText = stringResource(R.string.si_smart_selfie_v3_get_started),
            onClick = {},
        )
        SmileIDAttribution()
    }
}
