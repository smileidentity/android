package com.smileidentity.compose.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.smileidentity.R
import com.smileidentity.SmileIDOptIn
import com.smileidentity.compose.preview.Preview
import com.smileidentity.compose.preview.SmilePreviews

/**
 * Attribution for SmileID
 */
@SmileIDOptIn
@Composable
fun SmileIDAttribution(modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .padding(4.dp)
            .testTag("smile_id_attribution"),
    ) {
        Text(
            text = stringResource(R.string.si_attribution_powered_by),
            style = MaterialTheme.typography.labelSmall,
            // Color hardcoded to prevent being overridden by a custom theme
            color = Color(0xFF151F72),
        )
        Image(
            painter = painterResource(R.drawable.si_logo_with_text),
            contentDescription = stringResource(R.string.si_cd_logo),
            modifier = Modifier
                .height(12.dp)
                .padding(horizontal = 4.dp),
        )
    }
}

@SmilePreviews
@Composable
private fun SmileIDAttributionPreview() {
    Preview {
        SmileIDAttribution()
    }
}
