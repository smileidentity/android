package com.smileidentity.ui.compose

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SmartSelfieInstructionScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun shouldInvokeCallbackOnButtonClick() {
        // given
        var callbackInvoked = false
        val onInstructionsAcknowledged = { callbackInvoked = true }

        // when
        composeTestRule.setContent { SmartSelfieInstructionsScreen(onInstructionsAcknowledged) }
        composeTestRule.onNodeWithTag("readyButton").performClick()

        // then
        assertTrue(callbackInvoked)
    }
}
