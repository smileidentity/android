package com.smileidentity.compose

import android.app.Instrumentation
import android.os.Build
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until
import java.io.ByteArrayOutputStream

// Borrowed here - https://github.com/google/accompanist/blob/main/permissions/src/androidTest/java/com/google/accompanist/permissions/TestUtils.kt
internal fun grantPermissionInDialog(
    instrumentation: Instrumentation = InstrumentationRegistry.getInstrumentation(),
) {
    val uiDevice = UiDevice.getInstance(instrumentation)
    val sdkVersion = Build.VERSION.SDK_INT
    val button = uiDevice.findPermissionButton(
        when (sdkVersion) {
            in 24..28 -> "ALLOW"
            29 -> "Allow"
            else -> "While using the app"
        },
    )

    button.clickForPermission(instrumentation)
}

internal fun denyPermissionInDialog(
    instrumentation: Instrumentation = InstrumentationRegistry.getInstrumentation(),
) {
    val text = when (Build.VERSION.SDK_INT) {
        in 24..28 -> "DENY"
        in 29..30 -> "Deny"
        else -> "t allow" // Different sdks and devices seem to have either ' or ’
    }
    val permissionButton = UiDevice.getInstance(instrumentation).findPermissionButton(text)
    permissionButton.clickForPermission(instrumentation)
}

private fun UiDevice.findPermissionButton(text: String): UiObject2 {
    val selector = By
        .textContains(text)
        .clickable(true)

    val found = wait(Until.hasObject(selector), 3000)

    if (!found) {
        val output = ByteArrayOutputStream()
        dumpWindowHierarchy(output)
        println(output.toByteArray().decodeToString())

        error("Could not find button with text $text")
    }

    return findObject(selector)
}

private fun UiObject2.clickForPermission(
    instrumentation: Instrumentation = InstrumentationRegistry.getInstrumentation(),
): Boolean {
    click()
    // Make sure that the tests waits for this click to be processed
    instrumentation.waitForIdleSync()
    return true
}
