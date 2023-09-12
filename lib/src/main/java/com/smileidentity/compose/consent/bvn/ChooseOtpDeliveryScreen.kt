package com.smileidentity.compose.consent.bvn

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smileidentity.R
import com.smileidentity.compose.components.BottomPinnedColumn
import com.smileidentity.compose.components.LoadingButton
import com.smileidentity.compose.components.annotatedStringResource
import com.smileidentity.compose.preview.Preview
import com.smileidentity.compose.preview.SmilePreviews
import com.smileidentity.viewmodel.BvnConsentViewModel
import com.smileidentity.viewmodel.viewModelFactory
import kotlinx.collections.immutable.ImmutableList

internal data class BvnOtpVerificationMode(
    val mode: String,
    val otpSentBy: String,
    @StringRes val description: Int,
    @DrawableRes val icon: Int,
)

@Composable
internal fun ChooseOtpDeliveryScreen(
    userId: String,
    modifier: Modifier = Modifier,
    viewModel: BvnConsentViewModel = viewModel(
        factory = viewModelFactory {
            BvnConsentViewModel(userId = userId)
        },
    ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    BottomPinnedColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .imePadding(),
        scrollableContent = {
            Text(
                text = stringResource(id = R.string.si_bvn_select_contact_method),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
            )
            Spacer(modifier = Modifier.height(48.dp))
            val annotatedText = annotatedStringResource(
                id = R.string.si_bvn_nibss,
                spanStyles = { annotation ->
                    when (annotation.key) {
                        "is_link" -> SpanStyle(color = Color.Blue)
                        else -> null
                    }
                },
            )
            Text(
                text = annotatedText,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(id = R.string.si_bvn_nibss_partner),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(modifier = Modifier.height(32.dp))
            ContactMethods(
                bvnVerificationModes = uiState.bvnVerificationModes,
                selectedBvnOtpVerificationMode = uiState.selectedBvnOtpVerificationMode,
                onClick = viewModel::updateMode,
            )
        },
        pinnedContent = {
            LoadingButton(
                buttonText = stringResource(id = R.string.si_continue),
                loading = uiState.showLoading,
                enabled = uiState.selectedBvnOtpVerificationMode != null,
                onClick = viewModel::requestBvnOtp,
                modifier = Modifier
                    .testTag("choose_otp_delivery_continue_button")
                    .fillMaxWidth(),
            )
        },
    )
}

@Composable
internal fun ContactMethods(
    bvnVerificationModes: ImmutableList<BvnOtpVerificationMode>,
    selectedBvnOtpVerificationMode: BvnOtpVerificationMode?,
    modifier: Modifier = Modifier,
    onClick: (BvnOtpVerificationMode) -> Unit,
) {
    bvnVerificationModes.forEach {
        val selected = selectedBvnOtpVerificationMode == it
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier
                .fillMaxWidth()
                .padding(4.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surface)
                .selectable(
                    selected = selected,
                    onClick = { onClick(it) },
                    role = Role.RadioButton,
                )
                .padding(vertical = 8.dp),
        ) {
            RadioButton(selected = selected, onClick = { onClick(it) })
            Spacer(Modifier.width(8.dp))
            Image(
                painter = painterResource(id = it.icon),
                contentDescription = null,
                modifier = Modifier.size(32.dp),
            )
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    text = it.mode,
                    style = MaterialTheme.typography.bodyLarge,
                )
                Text(
                    text = stringResource(id = it.description),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@SmilePreviews
@Composable
private fun ChooseOtpDeliveryScreenPreview() {
    Preview {
        ChooseOtpDeliveryScreen(userId = "")
    }
}
