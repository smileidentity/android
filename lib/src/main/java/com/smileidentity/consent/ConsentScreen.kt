package com.smileidentity.consent

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.smileidentity.R
import com.smileidentity.annotatedStringResource
import com.smileidentity.compose.SmileIDAttribution
import com.smileidentity.compose.preview.Preview
import com.smileidentity.compose.preview.SmilePreview
import java.net.URL

@Composable
fun ConsentScreen(
    partnerIcon: Painter,
    partnerName: String,
    productName: String,
    partnerPrivacyPolicy: URL,
    modifier: Modifier = Modifier,
    showAttribution: Boolean = true,
    onContinue: () -> Unit,
    onCancel: () -> Unit,
) {
    val uriHandler = LocalUriHandler.current

    Surface(
        modifier = modifier.wrapContentWidth(),
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
                        painter = partnerIcon,
                        contentDescription = null,
                        modifier = modifier.padding(top = 24.dp, bottom = 24.dp),
                    )
                    Text(
                        text = stringResource(
                            id = R.string.si_consent_title,
                            partnerName,
                            productName,
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(id = R.string.si_consent_sub_title, partnerName),
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    getConsentScreenInformation.forEach {
                        Row(
                            modifier = Modifier
                                .padding(vertical = 12.dp, horizontal = 16.dp)
                                .align(alignment = Alignment.Start),
                        ) {
                            Image(
                                painter = painterResource(id = it.third),
                                contentDescription = null,
                                modifier = Modifier.size(35.dp),
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = stringResource(id = it.first),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = colorResource(id = R.color.si_color_accent),
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = stringResource(id = it.second),
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                        }
                    }
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
                    Divider(thickness = 1.dp, modifier = modifier.padding(horizontal = 16.dp))
                    val annotatedText = annotatedStringResource(
                        id = R.string.si_consent_privacy_policy,
                        spanStyles = { annotation ->
                            when (annotation.key) {
                                "is_url" -> SpanStyle(color = Color.Blue)
                                else -> null
                            }
                        },
                    )
                    ClickableText(
                        text = annotatedText,
                        style = MaterialTheme.typography.bodySmall,
                        onClick = { offset ->
                            annotatedText.getStringAnnotations(
                                tag = "is_url",
                                start = offset,
                                end = offset,
                            ).firstOrNull()?.let {
                                uriHandler.openUri(partnerPrivacyPolicy.toString())
                            }
                        },
                        modifier = modifier.padding(top = 16.dp),
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(
                            id = R.string.si_consent_privacy_policy_description,
                            partnerName,
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        modifier = modifier.padding(bottom = 24.dp),
                    )
                    Button(
                        onClick = onContinue,
                        modifier = Modifier
                            .testTag("consent_screen_continue_button")
                            .fillMaxWidth(),
                    ) {
                        Text(text = stringResource(id = R.string.si_allow))
                    }
                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier
                            .testTag("consent_screen_cancel_button")
                            .fillMaxWidth(),
                    ) {
                        Text(
                            text = stringResource(id = R.string.si_cancel),
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

private val getConsentScreenInformation
    get() = listOf(
        Triple(
            R.string.si_consent_info_one_title,
            R.string.si_consent_info_one_description,
            R.drawable.si_consent_personal_information,
        ),
        Triple(
            R.string.si_consent_info_two_title,
            R.string.si_consent_info_two_description,
            R.drawable.si_consent_contact_information,
        ),
        Triple(
            R.string.si_consent_info_three_title,
            R.string.si_consent_info_three_description,
            R.drawable.si_consent_document_information,
        ),
    )

@SmilePreview
@Composable
private fun ConsentScreenPreview() {
    Preview {
        ConsentScreen(
            partnerIcon = painterResource(id = R.drawable.si_logo_with_text),
            partnerName = "Smile ID",
            productName = "BVN",
            partnerPrivacyPolicy = URL("https://smileidentity.com/privacy"),
            onContinue = {},
            onCancel = {},
        )
    }
}
