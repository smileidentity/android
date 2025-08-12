package com.smileidentity.ui

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.smileidentity.ui.screens.SmileIDInstructionsScreen
import org.junit.Rule
import org.junit.Test

class SmileIDInstructionsScreenTest {

    @get:Rule(order = 0)
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun test_continue_button_exists_and_clickable() {
        composeTestRule.setContent {
            Box {
                SmileIDInstructionsScreen()
            }
        }

        composeTestRule
            .onNodeWithTag(testTag = "instructions:continue_button")
            .assertExists()
            .assertHasClickAction()
    }

    @Test
    fun test_cancel_button_exists_and_clickable() {
        composeTestRule.setContent {
            Box {
                SmileIDInstructionsScreen()
            }
        }

        composeTestRule
            .onNodeWithTag(testTag = "instructions:cancel_button")
            .assertExists()
            .assertHasClickAction()
    }

    @Test
    fun test_header_subtitle_visible() {
        composeTestRule.setContent {
            Box {
                SmileIDInstructionsScreen()
            }
        }

        composeTestRule
            .onNodeWithTag(testTag = "instructions:header")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag(testTag = "instructions:subtitle")
            .assertIsDisplayed()
    }
}
