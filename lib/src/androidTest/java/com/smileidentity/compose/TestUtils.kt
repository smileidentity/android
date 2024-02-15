package com.smileidentity.compose

import android.app.Instrumentation
import android.os.Build
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject
import androidx.test.uiautomator.UiSelector

// Borrowed here - https://github.com/google/accompanist/blob/main/permissions/src/androidTest/java/com/google/accompanist/permissions/TestUtils.kt
internal fun grantPermissionInDialog(
    instrumentation: Instrumentation = InstrumentationRegistry.getInstrumentation(),
) {
    val uiDevice = UiDevice.getInstance(instrumentation)
    val sdkVersion = Build.VERSION.SDK_INT
    val clicked = uiDevice.findPermissionButton(
        when (sdkVersion) {
            in 24..28 -> "ALLOW"
            else -> "Allow"
        },
    ).clickForPermission(instrumentation)

    // Or maybe this permission doesn't have the Allow option
    if (!clicked && sdkVersion > 28) {
        uiDevice.findPermissionButton(
            when (sdkVersion) {
                29 -> "Allow only while using the app"
                else -> "While using the app"
            },
        ).clickForPermission(instrumentation)
    }
}

internal fun denyPermissionInDialog(
    instrumentation: Instrumentation = InstrumentationRegistry.getInstrumentation(),
) {
    val text = when (Build.VERSION.SDK_INT) {
        in 24..28 -> "DENY"
        in 29..30 -> "Deny"
        else -> "Don’t allow"
    }
    val permissionButton = UiDevice.getInstance(instrumentation).findPermissionButton(text)
    permissionButton.clickForPermission(instrumentation)
}

private fun UiDevice.findPermissionButton(text: String): UiObject = findObject(
    UiSelector()
        .textMatches(text)
        .clickable(true)
        .className("android.widget.Button"),
)

private fun UiObject.clickForPermission(
    instrumentation: Instrumentation = InstrumentationRegistry.getInstrumentation(),
): Boolean {
    waitUntil { exists() }
    if (!exists()) return false

    val clicked = waitUntil { exists() && click() }
    // Make sure that the tests waits for this click to be processed
    if (clicked) {
        instrumentation.waitForIdleSync()
    }
    return clicked
}

private fun waitUntil(timeoutMillis: Long = 2_000, condition: () -> Boolean): Boolean {
    val startTime = System.nanoTime()
    while (true) {
        if (condition()) return true
        // Let Android run measure, draw and in general any other async operations.
        Thread.sleep(10)
        if (System.nanoTime() - startTime > timeoutMillis * 1_000_000) {
            return false
        }
    }
}
