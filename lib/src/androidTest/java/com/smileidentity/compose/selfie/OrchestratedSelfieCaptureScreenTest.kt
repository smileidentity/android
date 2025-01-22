package com.smileidentity.compose.selfie

import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule

class OrchestratedSelfieCaptureScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    // @Test
    // fun shouldShowInstructions() {
    //     // given
    //     val instructionsSubstring = "Next, we'll take a quick selfie"
    //
    //     // when
    //     composeTestRule.setContent {
    //         OrchestratedSelfieCaptureScreen(
    //             content = {},
    //             resultCallbacks = ResultCallbacks(),
    //         )
    //     }
    //
    //     // then
    //     composeTestRule.onNodeWithText(instructionsSubstring, substring = true).assertIsDisplayed()
    // }
    //
    // @Test
    // fun shouldNotShowInstructionsWhenDisabled() {
    //     // given
    //     val instructionsSubstring = "Next, we'll take a quick selfie"
    //
    //     // when
    //     composeTestRule.setContent {
    //         OrchestratedSelfieCaptureScreen(
    //             showInstructions = false,
    //             content = {},
    //             resultCallbacks = ResultCallbacks(),
    //         )
    //     }
    //
    //     // then
    //     composeTestRule.onNodeWithText(instructionsSubstring, substring = true)
    //         .assertDoesNotExist()
    // }
}
