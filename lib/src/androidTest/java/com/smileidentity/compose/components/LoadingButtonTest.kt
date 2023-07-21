package com.smileidentity.compose.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert
import org.junit.Rule
import org.junit.Test

class LoadingButtonTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testContinueButtonIsClickable() {
        // given
        var callbackInvoked = false
        val onConfirmButtonClicked = { callbackInvoked = true }
        val continueButtonText = "Continue"

        // when
        composeTestRule.setContent {
            LoadingButton(
                continueButtonText,
                onClick = onConfirmButtonClicked,
            )
        }
        composeTestRule.onNodeWithText(continueButtonText).performClick()

        // then
        Assert.assertTrue(callbackInvoked)
    }

    @Test
    fun testContinueButtonShowsLoadingStateWhenClicked() {
        // given
        var callbackInvoked = false
        val onConfirmButtonClicked = { callbackInvoked = true }
        val continueButtonText = "Continue"

        // when
        composeTestRule.setContent {
            LoadingButton(
                continueButtonText,
                onClick = onConfirmButtonClicked,
            )
        }
        composeTestRule.onNodeWithText(continueButtonText).performClick()

        // then
        Assert.assertTrue(callbackInvoked)
        composeTestRule.onNodeWithText(continueButtonText).assertIsNotDisplayed()
        composeTestRule.onNodeWithTag("circular_loading_indicator").assertIsDisplayed()
    }
}
