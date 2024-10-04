package com.smileidentity.compose.selfie.v2

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.smileidentity.SmileID
import com.smileidentity.compose.components.SmileThemeSurface
import com.smileidentity.compose.theme.colorScheme
import com.smileidentity.compose.theme.typography
import org.junit.Rule
import org.junit.Test

class SelfieCaptureScreenV2Test {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun shouldShowInstructions() {
        // given
        val instructionsSubstring =
            "Position your head in the camera view. Then move in the direction that is indicated"

        // when
        composeTestRule.setContent {
            SmileThemeSurface(
                SmileID.colorScheme,
                SmileID.typography,
            ) {
                SelfieCaptureInstructionScreenV2 {}
            }
        }

        // then
        composeTestRule.onNodeWithText(instructionsSubstring, substring = true).assertIsDisplayed()
    }
}
