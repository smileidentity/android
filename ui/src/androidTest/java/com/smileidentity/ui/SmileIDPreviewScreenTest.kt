package com.smileidentity.ui

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.smileidentity.ui.screens.SmileIDPreviewScreen
import org.junit.Rule
import org.junit.Test

class SmileIDPreviewScreenTest {

    @get:Rule(order = 0)
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun test_continue_button_exists_and_clickable() {
        composeTestRule.setContent {
            Box {
                SmileIDPreviewScreen()
            }
        }

        composeTestRule
            .onNodeWithTag(testTag = "preview:continue_button")
            .assertExists()
            .assertHasClickAction()
    }

    @Test
    fun test_cancel_button_exists_and_clickable() {
        composeTestRule.setContent {
            Box {
                SmileIDPreviewScreen()
            }
        }

        composeTestRule
            .onNodeWithTag(testTag = "preview:retry_button")
            .assertExists()
            .assertHasClickAction()
    }
}
