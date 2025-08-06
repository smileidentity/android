package com.smileidentity.ui

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import org.junit.Rule
import org.junit.Test

class SmileIDInstructionsScreenTest {

    @get:Rule(order = 0)
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun test_continue_button_is_clickable() {
        composeTestRule.setContent {
            Box {
                SmileIDInstructionsScreen { }
            }
        }

        composeTestRule
            .onNodeWithTag("continue_button")
            .assertExists()
            .assertHasClickAction()
    }
}
