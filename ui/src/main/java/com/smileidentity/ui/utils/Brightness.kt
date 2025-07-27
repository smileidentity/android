package com.smileidentity.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext

/**
 * Forces a Compose screen to be at a given brightness level and reverts after exiting screen
 */
@Composable
fun ForceMaxBrightness(brightness: Float = 1f) {
    val activity = LocalContext.current.getActivity() ?: return
    DisposableEffect(Unit) {
        val window = activity.window
        val attributes = window.attributes
        val originalBrightness = attributes.screenBrightness
        window.attributes = attributes.apply {
            screenBrightness = brightness
            flags = flags or android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        }
        onDispose {
            window.attributes = attributes.apply {
                screenBrightness = originalBrightness
                flags = flags and android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON.inv()
            }
        }
    }
}
