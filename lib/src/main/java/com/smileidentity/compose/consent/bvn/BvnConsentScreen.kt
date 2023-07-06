package com.smileidentity.compose.consent.bvn

import android.os.CountDownTimer
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
    bvnDeliveryOptionsList: List<BvnDeliveryOptions>,
    modifier: Modifier = Modifier,
    viewModel: BvnConsentViewModel = viewModel(
        factory = viewModelFactory {
            BvnConsentViewModel()
        },
    ),
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    var otp by rememberSaveable { mutableStateOf("") }
    val otpExpiresIn: Long = 10 * 1000 // TODO: get actual value
    var countdown by remember { mutableStateOf(otpExpiresIn) }

    val countDownTimer =
        object : CountDownTimer(otpExpiresIn, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                countdown = otpExpiresIn / 1000
            }

            override fun onFinish() {}
        }

    DisposableEffect(key1 = "key") {
        countDownTimer.start()
        onDispose {
            countDownTimer.cancel()
        }
    }

    val focusRequester = remember { FocusRequester() }

    BottomPinnedColumn(
        scrollableContent = {
            Image(
                painter = partnerIcon,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(vertical = 24.dp),
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
                Column(
                    modifier = Modifier
                        .padding(horizontal = 24.dp),
                ) {
                    Text(
                        text = stringResource(id = R.string.si_bvn_consent_nibss),
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Start,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(id = R.string.si_bvn_consent_bvn_delivery_method),
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Start,
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    bvnDeliveryOptionsList.forEach { bvnDeliveryOption ->
                        OtpDeliveryModeCard(
                            icon = painterResource(id = bvnDeliveryOption.icon),
                            otpDelivery = bvnDeliveryOption.otpDelivery,
                            deliveryDescription = stringResource(
                                id = bvnDeliveryOption.deliveryDescription,
                            ),
                            onClick = {
                                viewModel.handleEvent(
                                    BvnConsentEvent.SelectOTPDeliveryMode(
                                        otpDeliveryMode = it,
                                    ),
                                )
                            },
                        )
                    }
                }
            }
            AnimatedVisibility(visible = uiState.showOtpScreen) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = stringResource(
                            id = R.string.si_bvn_consent_otp_sent,
                            uiState.otpSentTo,
                        ),
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = otp,
                        onValueChange = { otp = it },
                        isError = uiState.showWrongOtp,
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    AnimatedVisibility(visible = uiState.showWrongOtp) {
                        Text(
                            text = stringResource(id = R.string.si_bvn_consent_error_wrong_otp),
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = colorResource(id = R.color.si_color_error),
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(id = R.string.si_bvn_consent_otp_timer, countdown),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                    )
                }
            }
            AnimatedVisibility(
                visible = uiState.showExpiredOtpScreen,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Spacer(modifier = Modifier.height(34.dp))
                    Text(
                        text = stringResource(id = R.string.si_bvn_consent_error_subtitle),
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = stringResource(id = R.string.si_bvn_consent_if_didnt_receive_otp),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(
                            id = R.string.si_bvn_consent_different_delivery_option,
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = Color.Blue,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable {
                            viewModel.handleEvent(BvnConsentEvent.GoToSelectOTPDeliveryMode)
                        },
                    )
                }
            }
        },
        pinnedContent = {
            Column(
                modifier = Modifier.fillMaxWidth(),
            ) {
                AnimatedVisibility(
                    visible = uiState.showDeliveryMode,
                ) {
                    Text(
                        text = stringResource(id = R.string.si_bvn_consent_nibss_bvn),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Start,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                    )
                }
                AnimatedVisibility(
                    visible = uiState.showOtpScreen || uiState.showWrongOtp,
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
                    visible = uiState.showOtpScreen || uiState.showWrongOtp,
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(id = R.string.si_bvn_consent_receive_otp),
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            text = stringResource(
                                id = R.string.si_bvn_consent_different_delivery_option,
                            ),
                            color = Color.Blue, // color = colorResource(id =
                            // R.color.si_color_accent) doesn't work (wierd)
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable {
                                viewModel.handleEvent(BvnConsentEvent.GoToSelectOTPDeliveryMode)
                            },
                        )
                    }
                }
            }
        },
        modifier = modifier,
    )
}

data class BvnDeliveryOptions(
    @DrawableRes val icon: Int,
    val otpDelivery: String,
    @StringRes val deliveryDescription: Int,
)

@Composable
fun OtpDeliveryModeCard(
    icon: Painter,
    otpDelivery: String,
    deliveryDescription: String,
    modifier: Modifier = Modifier,
    onClick: (OtpDeliveryMode) -> Unit,
) {
    // TODO - Logic to determine which enum to return "onClick" after API is implemented
    Column(modifier = modifier.padding(bottom = 16.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable {
                onClick(OtpDeliveryMode.EMAIL)
            },
        ) {
            Image(
                painter = icon,
                contentDescription = "OTP Delivery Mode Icon",
                modifier = Modifier
                    .size(40.dp),
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
            ) {
                Text(
                    text = otpDelivery,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Start,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = deliveryDescription,
                    style = MaterialTheme.typography.titleSmall,
                    textAlign = TextAlign.Start,
                )
            }
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "OTP Delivery Mode Click",
            )
        }
    }
}

@SmilePreviews
@Composable
private fun BvnConsentScreenPreview() {
    Preview {
        BvnConsentScreen(
            partnerIcon = painterResource(id = R.drawable.si_logo_with_text),
            bvnDeliveryOptionsList = listOf(
                BvnDeliveryOptions(
                    icon = R.drawable.si_logo_with_text,
                    otpDelivery = "email@example.com",
                    deliveryDescription = R.string.si_bvn_consent_bvn_delivery_method_email,
                ),
                BvnDeliveryOptions(
                    icon = R.drawable.si_camera_capture,
                    otpDelivery = "+254789564875",
                    deliveryDescription = R.string.si_bvn_consent_bvn_delivery_method_sms,
                ),
            ),
        )
    }
}
