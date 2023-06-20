package com.smileidentity.consent

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.smileidentity.compose.preview.SmilePreview

@Composable
fun ConsentDeniedScreen(
    modifier: Modifier = Modifier,
    onGoBack: () -> Unit,
    onCancel: () -> Unit,
) {
    Surface(
        modifier = modifier.wrapContentWidth(),
    ) {
        Scaffold(
            content = { innerPadding ->
                Column(
                    modifier = modifier
                        .fillMaxWidth()
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState(), true),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.si_consent_denied),
                        contentDescription = null,
                        modifier = modifier.padding(top = 48.dp, bottom = 48.dp),
                    )
                    Spacer(modifier = Modifier.width(16.dp))
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
                    modifier = modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState(), true),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Button(
                        onClick = onGoBack,
                        modifier = Modifier
                            .testTag("consent_screen_denied_go_back_button")
                            .fillMaxWidth(),
                    ) { Text(text = stringResource(id = R.string.si_consent_go_back)) }

                    TextButton(
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

                    SmileIDAttribution()
                }
            },
        )
    }
}

@SmilePreview
@Composable
fun ConsentDeniedScreenPreview() {
    Preview {
        ConsentDeniedScreen(
            onGoBack = {},
            onCancel = {},
        )
    }
}
