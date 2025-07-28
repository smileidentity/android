package com.smileidentity.ui.utils

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import timber.log.Timber

internal fun Context.getActivity(): Activity? {
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
