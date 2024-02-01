package com.smileidentity

import com.smileidentity.SmileIDCrashReporting.SMILE_ID_PACKAGE_PREFIX
import timber.log.Timber

/**
 * A Timber tree that logs only Smile Identity logs. This allows us to plant regardless of whether
 * the app is in debug mode or not, and not rely on partners to plant Timber trees.
 */
class SmileTree : Timber.DebugTree() {
    override fun isLoggable(tag: String?, priority: Int) = tag != null

    override fun createStackElementTag(element: StackTraceElement): String? {
        return if (element.className.contains(SMILE_ID_PACKAGE_PREFIX)) {
            super.createStackElementTag(element)
        } else {
            null
        }
    }
}
