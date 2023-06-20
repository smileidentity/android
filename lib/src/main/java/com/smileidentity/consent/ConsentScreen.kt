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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.smileidentity.R
import com.smileidentity.compose.SmileIDAttribution
import com.smileidentity.compose.preview.Preview
import com.smileidentity.compose.preview.SmilePreview

@Composable
fun ConsentScreen(
    modifier: Modifier = Modifier,
    partnerIcon: Painter,
    partnerName: String,
    productName: String,
    partnerPrivacyPolicy: String,
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
                        modifier = modifier.padding(top = 48.dp, bottom = 48.dp),
                    )
                    Spacer(modifier = Modifier.width(16.dp))
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
                    getConsentScreenInformation().forEach {
                        Row(modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp)) {
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
                    // https://developer.android.com/jetpack/compose/text/user-interactions
                    val annotatedText = buildAnnotatedString {
                        append(stringResource(id = R.string.si_consent_privacy_policy))
                        // We attach this *URL* annotation to the following content
                        // until `pop()` is called
                        pushStringAnnotation(
                            tag = "URL",
                            annotation = partnerPrivacyPolicy,
                        )
                        withStyle(style = SpanStyle(color = Color.Blue)) {
                            append(stringResource(id = R.string.si_consent_privacy_policy_here))
                        }
                        pop()
                    }
                    ClickableText(
                        text = annotatedText,
                        style = MaterialTheme.typography.bodySmall,
                        onClick = { offset ->
                            // We check if there is an *URL* annotation attached to the text
                            // at the clicked position
                            annotatedText.getStringAnnotations(
                                tag = "URL",
                                start = offset,
                                end = offset,
                            ).firstOrNull()?.let { annotation ->
                                uriHandler.openUri(annotation.item)
                            }
                        },
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
                    TextButton(
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
                    SmileIDAttribution()
                }
            },
        )
    }
}

private fun getConsentScreenInformation() = listOf(
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
fun ConsentScreenPreview() {
    Preview {
        ConsentScreen(
            partnerIcon = painterResource(id = R.drawable.si_logo_with_text),
            partnerName = "Smile ID",
            productName = "BVN",
            partnerPrivacyPolicy = "https://smileidentity.com/privacy",
            onContinue = {},
            onCancel = {},
        )
    }
}