package com.smileidentity.compose.selfie

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
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
        // composeTestRule.setContent { OrchestratedSelfieCaptureScreen() }

        // then
        composeTestRule.onNodeWithText(instructionsSubstring, substring = true).assertIsDisplayed()
    }

    @Test
    fun shouldNotShowInstructionsWhenDisabled() {
        // given
        // val instructionsSubstring = "Next, we'll take a quick selfie"
        // val navController = TestNavHostController(
        //     ApplicationProvider.getApplicationContext())
        // val navHost = NavHost(navController,startDestination = getSelfieCaptureRoute(useStrictMode = false,
        //     SelfieCaptureParams(userId = "1", jobId = "2"))){
        //     screensNavGraph()
        // }
        //
        // // when
        // composeTestRule.setContent {
        //
        //     OrchestratedSelfieCaptureScreen(
        //         ResultCallbacks(),
        //         content = getDocumentCaptureRoute(params = DocumentCaptureParams(userId = "1", jobId = "2")),
        //         showInstructions = false,
        //     )
        // }
        //
        // // then
        // composeTestRule.onNodeWithText(instructionsSubstring, substring = true)
        //     .assertDoesNotExist()
    }
}
