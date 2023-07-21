package com.smileidentity.compose.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.smileidentity.compose.consent.bvn.BvnOtpVerificationMode
import com.smileidentity.compose.preview.Preview
import com.smileidentity.compose.preview.SmilePreviews

@Composable
internal fun ContactMethodItem(
    bvnOtpVerificationMode: BvnOtpVerificationMode,
    modifier: Modifier = Modifier,
    onItemSelected: (mode: String) -> Unit,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = true,
            onClick = { onItemSelected(bvnOtpVerificationMode.mode) },
        )
        Image(
            painterResource(bvnOtpVerificationMode.icon),
            contentDescription = null,
            modifier = Modifier.size(30.dp),
        )
        Column {
            Text(text = bvnOtpVerificationMode.mode)
            Text(text = stringResource(id = bvnOtpVerificationMode.description))
        }
    }
}

@SmilePreviews
@Composable
private fun ContactMethodItemPreview() {
    Preview {
        // ContactMethodItem()
    }
}