package com.smileidentity.compose.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.smileidentity.R
import com.smileidentity.compose.preview.SmilePreviews

@Composable
internal fun LoadingButton(
    buttonText: String,
    modifier: Modifier = Modifier,
    loading: Boolean = false,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        enabled = !loading,
    ) {
        Box {
            if (loading) {
                CircularProgressIndicator(
                    color = colorResource(id = R.color.si_color_accent),
                    strokeWidth = 2.dp,
                    modifier = Modifier
                        .size(15.dp)
                        .align(Alignment.Center)
                        .testTag("circular_loading_indicator"),
                )
            } else {
                Text(text = buttonText)
            }
        }
    }
}

@SmilePreviews
@Composable
fun LoadingButtonPreview() {
    LoadingButton(
        buttonText = "Continue",
        onClick = {},
    )
}
