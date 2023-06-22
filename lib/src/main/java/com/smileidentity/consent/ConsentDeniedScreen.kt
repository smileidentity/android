package com.smileidentity.consent

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.smileidentity.R
import com.smileidentity.compose.SmileIDAttribution
import com.smileidentity.compose.preview.Preview
import com.smileidentity.compose.preview.SmilePreviews

@Composable
fun ConsentDeniedScreen(
    onGoBack: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    showAttribution: Boolean = true,
) {
    Surface(
        modifier = modifier.fillMaxSize(),
    ) {
        Scaffold(
            content = { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.si_consent_denied),
                        contentDescription = null,
                        modifier = Modifier.padding(vertical = 48.dp),
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(id = R.string.si_consent_consent_denied),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        color = colorResource(id = R.color.si_color_material_error_container),
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(id = R.string.si_consent_consent_denied_title),
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Spacer(modifier = Modifier.height(46.dp))
                    Text(
                        text = stringResource(id = R.string.si_consent_consent_denied_description),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        color = colorResource(id = R.color.si_color_accent),
                    )
                }
            },
            bottomBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Button(
                        onClick = onGoBack,
                        modifier = Modifier
                            .testTag("consent_screen_denied_go_back_button")
                            .fillMaxWidth(),
                    ) { Text(text = stringResource(id = R.string.si_consent_go_back)) }

                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier
                            .testTag("consent_screen_denied_cancel_button")
                            .fillMaxWidth(),
                    ) {
                        Text(
                            text = stringResource(id = R.string.si_consent_cancel_verification),
                            color = colorResource(id = R.color.si_color_material_error_container),
                        )
                    }
                    if (showAttribution) {
                        SmileIDAttribution()
                    }
                }
            },
        )
    }
}

@SmilePreviews
@Composable
private fun ConsentDeniedScreenPreview() {
    Preview {
        ConsentDeniedScreen(
            onGoBack = {},
            onCancel = {},
        )
    }
}
