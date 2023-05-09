package com.smileidentity.compose.preview

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.smileidentity.SmileID
import com.smileidentity.compose.theme.colorScheme
import com.smileidentity.compose.theme.typography

@Composable
internal fun Preview(
    content: @Composable () -> Unit,
) {
    MaterialTheme(colorScheme = SmileID.colorScheme, typography = SmileID.typography) {
        content()
    }
}
