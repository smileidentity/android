package com.smileidentity.compose.preview

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview
import com.smileidentity.SmileIDOptIn

/**
 * Multi preview annotation that represents various device sizes, and dark mode. Add this annotation to a composable
 * to render various devices.
 */
@Preview(
    name = "A/Phone",
    group = "phone-preview",
    device = "spec:shape=Normal,width=360,height=640,unit=dp,dpi=480",
)
@Preview(
    name = "E/Dark mode",
    group = "dark-mode",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@SmileIDOptIn
annotation class SmilePreviews
