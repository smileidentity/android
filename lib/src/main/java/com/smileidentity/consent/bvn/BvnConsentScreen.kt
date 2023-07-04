package com.smileidentity.consent.bvn

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smileidentity.R
import com.smileidentity.compose.components.BottomPinnedColumn
import com.smileidentity.compose.preview.Preview
import com.smileidentity.compose.preview.SmilePreviews
import com.smileidentity.viewmodel.BvnConsentViewModel
import com.smileidentity.viewmodel.viewModelFactory

@Composable
fun BvnConsentScreen(
    partnerIcon: Painter,
    modifier: Modifier = Modifier,
    viewModel: BvnConsentViewModel = viewModel(
        factory = viewModelFactory {
            BvnConsentViewModel()
        },
    ),
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    var otp by rememberSaveable { mutableStateOf("") }

    val focusRequester = remember { FocusRequester() }

    BottomPinnedColumn(
        scrollableContent = {
            Image(
                painter = partnerIcon,
                contentDescription = null,
                modifier = Modifier.padding(vertical = 24.dp),
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = stringResource(id = R.string.si_bvn_consent_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(24.dp))
            AnimatedVisibility(visible = uiState.showDeliveryMode) {
                Text(
                    text = stringResource(id = R.string.si_bvn_consent_nibss),
                    style = MaterialTheme.typography.titleSmall,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = stringResource(id = R.string.si_bvn_consent_otp_sent),
                    style = MaterialTheme.typography.titleSmall,
                    textAlign = TextAlign.Center,
                )
            }
            AnimatedVisibility(visible = uiState.showOtpScreen) {
                Text(
                    text = stringResource(id = R.string.si_bvn_consent_otp_sent),
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                )
                OutlinedTextField(
                    value = otp,
                    onValueChange = { otp = it },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                )
                Text(
                    text = stringResource(id = R.string.si_bvn_consent_nibss),
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                )
            }
            AnimatedVisibility(visible = uiState.showWrongOtpScreen) {
                Text(
                    text = stringResource(id = R.string.si_bvn_consent_otp_sent),
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                )
            }
            AnimatedVisibility(visible = uiState.showExpiredOtpScreen) {
                Text(
                    text = stringResource(id = R.string.si_bvn_consent_error_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                )
            }
        },
        pinnedContent = {
            AnimatedVisibility(
                visible = uiState.showDeliveryMode,
            ) {
                Text(
                    text = stringResource(id = R.string.si_bvn_consent_nibss_bvn),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Start,
                )
            }
            AnimatedVisibility(
                visible = uiState.showOtpScreen || uiState.showWrongOtpScreen,
            ) {
                Button(
                    onClick = {
                        viewModel.handleEvent(BvnConsentEvent.SubmitOTPMode(otp = otp))
                    },
                    modifier = Modifier
                        .testTag("bvn_consent_continue_button")
                        .fillMaxWidth(),
                ) {
                    Text(text = stringResource(id = R.string.si_continue))
                }
            }
            AnimatedVisibility(
                visible = uiState.showOtpScreen || uiState.showWrongOtpScreen ||
                    uiState.showExpiredOtpScreen,
            ) {
                Text(
                    text = stringResource(id = R.string.si_bvn_consent_receive_otp),
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = stringResource(id = R.string.si_bvn_consent_different_delivery_option),
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    color = colorResource(id = R.color.si_color_accent),
                    modifier = Modifier.clickable {
                        viewModel.handleEvent(BvnConsentEvent.GoToSelectOTPDeliveryMode)
                    },
                )
            }
        },
    )
}

@SmilePreviews
@Composable
private fun BvnConsentScreenPreview() {
    Preview {
        BvnConsentScreen(
            partnerIcon = painterResource(id = R.drawable.si_logo_with_text),
        )
    }
}
