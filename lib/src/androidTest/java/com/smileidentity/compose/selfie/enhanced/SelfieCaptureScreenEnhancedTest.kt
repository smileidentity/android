package com.smileidentity.compose.selfie.enhanced

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.smileidentity.SmileID
import com.smileidentity.compose.theme.SmileThemeSurface
import com.smileidentity.compose.theme.colorScheme
import com.smileidentity.compose.theme.typography
import org.junit.Rule
import org.junit.Test

class SelfieCaptureScreenEnhancedTest {
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
                SelfieCaptureInstructionScreenEnhanced {}
            }
        }

        // then
        composeTestRule.onNodeWithText(instructionsSubstring, substring = true).assertIsDisplayed()
    }
}
