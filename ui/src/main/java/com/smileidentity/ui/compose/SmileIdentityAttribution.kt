package com.smileidentity.ui.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.smileidentity.ui.R
import com.smileidentity.ui.theme.SmileIdentityMediumBlue

@Preview
@Composable
fun SmileIdentityAttribution() {
    val shape = RoundedCornerShape(4.dp)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .border(1.dp, Color.LightGray.copy(alpha = 0.2f), shape)
            .shadow(2.dp, shape)
            .clip(shape)
            .background(Color.White)
            .testTag("smileIdentityAttribution"),
    ) {
        Image(
            painterResource(R.drawable.si_logo_lock_white),
            contentDescription = stringResource(R.string.si_cd_logo),
            modifier = Modifier
                .size(24.dp)
                .background(SmileIdentityMediumBlue, shape)
                .padding(2.dp),
        )
        Text(
            text = stringResource(R.string.si_attribution),
            style = MaterialTheme.typography.labelSmall,
            color = Color.Black,
            modifier = Modifier.padding(4.dp, 0.dp),
        )
    }
}
