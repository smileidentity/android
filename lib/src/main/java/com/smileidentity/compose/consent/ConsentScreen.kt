package com.smileidentity.compose.consent

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.smileidentity.compose.components.BottomPinnedColumn
import com.smileidentity.compose.components.SmileIDAttribution
import com.smileidentity.compose.components.annotatedStringResource
import com.smileidentity.compose.preview.Preview
import com.smileidentity.compose.preview.SmilePreviews
import com.smileidentity.models.ConsentInformation
import com.smileidentity.util.getCurrentIsoTimestamp
import java.net.URL

/**
 * Consent screen for SmileID
 */
@Composable
fun ConsentScreen(
    partnerIcon: Painter,
    partnerName: String,
    productName: String,
    partnerPrivacyPolicy: URL,
    onContinue: (ConsentInformation) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    showAttribution: Boolean = true,
) {
    val uriHandler = LocalUriHandler.current

    BottomPinnedColumn(
        scrollableContent = {
            Image(
                painter = partnerIcon,
                contentDescription = null,
                modifier = Modifier.padding(vertical = 24.dp),
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
            consentScreenInformation.forEach {
                Row(
                    modifier = Modifier
                        .padding(vertical = 12.dp, horizontal = 16.dp)
                        .fillMaxWidth(),
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
        },
        pinnedContent = {
            val annotatedText = annotatedStringResource(
                id = R.string.si_consent_privacy_policy,
                partnerName,
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
                modifier = Modifier.padding(top = 16.dp),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(
                    id = R.string.si_consent_privacy_policy_description,
                    partnerName,
                ),
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 24.dp),
            )
            Button(
                onClick = {
                    onContinue(
                        ConsentInformation(
                            consentGrantedDate = getCurrentIsoTimestamp(),
                            personalDetailsConsentGranted = true,
                            contactInfoConsentGranted = true,
                            documentInfoConsentGranted = true,
                        ),
                    )
                },
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
        },
        modifier = modifier.testTag("consent_screen"),
        showDivider = true,
    )
}

private val consentScreenInformation = listOf(
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

@SmilePreviews
@Composable
private fun ConsentScreenPreview() {
    Preview {
        ConsentScreen(
            partnerIcon = painterResource(id = R.drawable.si_logo_with_text),
            partnerName = "Smile ID",
            productName = "BVN",
            partnerPrivacyPolicy = URL("https://usesmileid.com/privacy"),
            onContinue = {},
            onCancel = {},
        )
    }
}
