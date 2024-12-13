package com.smileidentity.compose.selfie

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.smileidentity.compose.nav.ResultCallbacks
import org.junit.Rule
import org.junit.Test

class OrchestratedSelfieCaptureScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun shouldShowInstructions() {
        // given
        val instructionsSubstring = "Next, we'll take a quick selfie"

        // when
        composeTestRule.setContent {
            OrchestratedSelfieCaptureScreen(
                content = {},
                resultCallbacks = ResultCallbacks(),
            )
        }

        // then
        composeTestRule.onNodeWithText(instructionsSubstring, substring = true).assertIsDisplayed()
    }

    @Test
    fun shouldNotShowInstructionsWhenDisabled() {
        // given
        val instructionsSubstring = "Next, we'll take a quick selfie"

        // when
        composeTestRule.setContent {
            OrchestratedSelfieCaptureScreen(
                showInstructions = false,
                content = {},
                resultCallbacks = ResultCallbacks(),
            )
        }

        // then
        composeTestRule.onNodeWithText(instructionsSubstring, substring = true)
            .assertDoesNotExist()
    }
}
