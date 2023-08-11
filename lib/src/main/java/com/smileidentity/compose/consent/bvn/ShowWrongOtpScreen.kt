package com.smileidentity.compose.consent.bvn

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.smileidentity.R
import com.smileidentity.compose.preview.Preview
import com.smileidentity.compose.preview.SmilePreviews

@Composable
fun ShowWrongOtpScreen(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(id = R.string.si_bvn_verification),
            style = MaterialTheme.typography.headlineSmall,
        )
        Text(text = stringResource(id = R.string.si_bvn_verification_if_didnt_receive_otp))
        Text(text = stringResource(id = R.string.si_bvn_verification_different_contact_method))
        Image(
            painter = painterResource(id = R.drawable.si_bvn_otp_expired),
            contentDescription = null,
            modifier = Modifier.size(64.dp),
        )
        Text(text = stringResource(id = R.string.si_bvn_verification_otp_expired))
    }
}

@SmilePreviews
@Composable
private fun ShowWrongOtpScreenPreview() {
    Preview {
        ShowWrongOtpScreen()
    }
}
