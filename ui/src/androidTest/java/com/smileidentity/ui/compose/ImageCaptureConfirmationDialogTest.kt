package com.smileidentity.ui.compose

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ImageCaptureConfirmationDialogTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    @Suppress("MoveLambdaOutsideParentheses")
    fun shouldInvokeConfirmationCallbackOnConfirmButtonClick() {
        // given
        var callbackInvoked = false
        val onConfirmImageButtonClicked = { callbackInvoked = true }
        val confirmButtonText = "Confirm"

        // when
        composeTestRule.setContent {
            ImageCaptureConfirmationDialog(
                "title",
                "subtitle",
                ColorPainter(Color.Blue),
                confirmButtonText,
                onConfirmImageButtonClicked,
                "Retake",
                { },
            )
        }
        composeTestRule.onNodeWithText(confirmButtonText).performClick()

        // then
        assertTrue(callbackInvoked)
    }

    @Test
    @Suppress("MoveLambdaOutsideParentheses")
    fun shouldInvokeRetakeCallbackOnRetakeButtonClick() {
        // given
        var callbackInvoked = false
        val onRetakeImageButtonClicked = { callbackInvoked = true }
        val retakeButtonText = "Retake"

        // when
        composeTestRule.setContent {
            ImageCaptureConfirmationDialog(
                "title",
                "subtitle",
                ColorPainter(Color.Blue),
                "Confirm",
                { },
                retakeButtonText,
                onRetakeImageButtonClicked,
            )
        }
        composeTestRule.onNodeWithText(retakeButtonText).performClick()

        // then
        assertTrue(callbackInvoked)
    }
}
