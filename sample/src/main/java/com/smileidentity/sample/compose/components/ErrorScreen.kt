package com.smileidentity.sample.compose.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.smileidentity.sample.R
import com.smileidentity.sample.compose.SmileIDTheme

@Composable
fun ErrorScreen(
    modifier: Modifier = Modifier,
    errorText: String = stringResource(id = R.string.jobs_loading_error_message),
    onRetry: () -> Unit,
) {
    Column(
        horizontalAlignment = CenterHorizontally,
        modifier = modifier,
    ) {
        Icon(imageVector = Icons.Default.Warning, contentDescription = null)
        Text(text = errorText)
        TextButton(onClick = onRetry) {
            Text(text = stringResource(id = R.string.try_again))
        }
    }
}

@Preview
@Composable
fun ErrorScreenPreview() {
    SmileIDTheme {
        Surface {
            ErrorScreen(onRetry = {})
        }
    }
}
