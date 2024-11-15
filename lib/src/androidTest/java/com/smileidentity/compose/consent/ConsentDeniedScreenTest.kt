package com.smileidentity.compose.consent

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ConsentDeniedScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun shouldInvokeOnGoBackCallbackOnYesButtonClick() {
        // given
        var callbackInvoked = false
        val onGoBackClicked = { callbackInvoked = true }
        val yesButtonText = "Yes, go back"

        // when
        composeTestRule.setContent {
            ConsentDeniedScreen(
                onGoBack = onGoBackClicked,
                onCancel = {},
            )
        }
        composeTestRule.onNodeWithText(yesButtonText).performClick()

        // then
        assertTrue(callbackInvoked)
    }

    @Test
    fun shouldInvokeOnCancelCallbackOnCancelButtonClick() {
        // given
        var callbackInvoked = false
        val onCancelClicked = { callbackInvoked = true }
        val cancelButtonText = "No, cancel verification"

        // when
        composeTestRule.setContent {
            ConsentDeniedScreen(
                onGoBack = {},
                onCancel = onCancelClicked,
            )
        }
        composeTestRule.onNodeWithText(cancelButtonText).performClick()

        // then
        assertTrue(callbackInvoked)
    }
}
