package com.smileidentity.sample.compose.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.DialogProperties
import com.smileidentity.sample.R

@Composable
fun SmileConfigConfirmationScreen(
    partnerId: String,
    modifier: Modifier = Modifier,
    goToMainScreen: () -> Unit,
) {
    AlertDialog(
        icon = {
            Icon(
                painter = painterResource(id = R.drawable.ic_icon_check_mark),
                contentDescription = null,
                tint = Color.Unspecified,
            )
        },
        title = {
            Text(
                text = stringResource(id = R.string.root_config_added),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.ExtraBold,
            )
        },
        text = {
            Text(
                text = stringResource(id = R.string.root_config_added_welcome, partnerId),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            Button(
                onClick = goToMainScreen,
                modifier = Modifier
                    .fillMaxWidth(),
            ) { Text(text = stringResource(id = R.string.cont)) }
        },
        dismissButton = {},
        onDismissRequest = { /* Do nothing since we have disabled back press and click outside */ },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
        modifier = modifier,
    )
}
