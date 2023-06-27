package com.smileidentity.compose

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import timber.log.Timber

/**
 * Forces a Compose screen to be at a given brightness level and reverts after exiting screen
 */
@Composable
fun ForceBrightness(brightness: Float = 1f) {
    val activity = LocalContext.current.getActivity() ?: return
    DisposableEffect(Unit) {
        val attributes = activity.window.attributes
        val originalBrightness = attributes.screenBrightness
        activity.window.attributes = attributes.apply { screenBrightness = brightness }
        onDispose {
            activity.window.attributes = attributes.apply { screenBrightness = originalBrightness }
        }
    }
}

private fun Context.getActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) {
            return context
        }
        context = context.baseContext
    }
    Timber.e("Could not find Activity from context")
    return null
}
