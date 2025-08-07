package com.smileidentity.ui

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.smileidentity.ml.states.IdentityScanState
import org.junit.Rule
import org.junit.Test

class SmileIDCaptureScreenTest {

    @get:Rule(order = 0)
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun test_continue_button_exists_and_clickable() {
        composeTestRule.setContent {
            Box {
                SmileIDCaptureScreen(scanType = IdentityScanState.ScanType.SELFIE) {}
            }
        }

        composeTestRule
            .onNodeWithTag(testTag = "capture:continue_button")
            .assertExists()
            .assertHasClickAction()
    }
}
