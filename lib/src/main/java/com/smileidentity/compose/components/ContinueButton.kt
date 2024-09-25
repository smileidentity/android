package com.smileidentity.compose.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.smileidentity.compose.preview.SmilePreviews

/**
 * A button that allows theming customizations
 */
@Composable
internal fun ContinueButton(
    buttonText: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
    ) {
        Text(
            text = buttonText,
        )
    }
}

@SmilePreviews
@Composable
private fun ContinueButtonButtonPreview() {
    ContinueButton(
        buttonText = "Continue",
        onClick = {},
    )
}
