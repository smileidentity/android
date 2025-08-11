package com.smileidentity.navigation.ui

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.ramcosta.composedestinations.navigation.EmptyDestinationsNavigator
import org.junit.Rule
import org.junit.Test

class OrchestratedCaptureScreenTest {

    @get:Rule(order = 0)
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun test_continue_button_is_displayed() {
        composeTestRule.setContent {
            Box {
                OrchestratedCaptureScreen(
                    navigator = EmptyDestinationsNavigator,
                )
            }
        }

        composeTestRule
            .onNodeWithText(text = "Continue")
            .assertIsDisplayed()
    }
}
