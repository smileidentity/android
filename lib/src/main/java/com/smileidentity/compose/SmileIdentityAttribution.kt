package com.smileidentity.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.smileidentity.R

@Preview
@Composable
fun SmileIdentityAttribution() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(4.dp)
            .testTag("smileIdentityAttribution"),
    ) {
        Text(
            text = stringResource(R.string.si_attribution_powered_by),
            style = MaterialTheme.typography.labelSmall,
        )
        Image(
            painterResource(R.drawable.si_logo_lock_black),
            contentDescription = stringResource(R.string.si_cd_logo),
            modifier = Modifier
                .size(20.dp)
                .padding(2.dp),
        )
        Text(
            text = stringResource(R.string.si_company_name),
            style = MaterialTheme.typography.labelSmall,
        )
    }
}
