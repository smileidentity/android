package com.smileidentity.compose.consent.bvn

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smileidentity.R
import com.smileidentity.compose.components.BottomPinnedColumn
import com.smileidentity.compose.components.LoadingButton
import com.smileidentity.compose.preview.Preview
import com.smileidentity.compose.preview.SmilePreviews
import com.smileidentity.viewmodel.BvnConsentViewModel
import com.smileidentity.viewmodel.viewModelFactory

internal data class BvnOtpVerificationMode(
    val mode: String,
    @StringRes val description: Int,
    @DrawableRes val icon: Int,
)

@Composable
internal fun BvnConsentScreen(
    modifier: Modifier = Modifier,
    viewModel: BvnConsentViewModel = viewModel(
        factory = viewModelFactory {
            BvnConsentViewModel()
        },
    ),
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    BottomPinnedColumn(
        modifier = modifier.padding(16.dp),
        scrollableContent = {
            Text(
                text = stringResource(id = R.string.si_bvn_select_contact_method),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(id = R.string.si_bvn_nibss),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
            )
            Text(
                text = stringResource(id = R.string.si_bvn_nibss_partner),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.ExtraBold,
            )
            uiState.bvnVerificationModes.map {
            }
        },
        pinnedContent = {
            LoadingButton(
                buttonText = stringResource(id = R.string.si_continue),
                onClick = { viewModel.requestBvnOtp() },
            )
        },
    )
}

@Composable
fun ContactMethod(
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier) {
    }
}

@SmilePreviews
@Composable
private fun BvnConsentScreenPreview() {
    Preview {
        BvnConsentScreen()
    }
}
