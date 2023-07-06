package com.smileidentity.compose.consent.bvn

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class BvnInputScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun shouldInvokeCancelCallbackOnCancelButtonClick() {
        // given
        var callbackInvoked = false
        val onCancelClicked = { callbackInvoked = true }
        val cancelIconTestTag = "bvn_input_screen_cancel"

        // when
        composeTestRule.setContent {
            BvnInputScreen(
                cancelBvnVerification = onCancelClicked,
            )
        }
        composeTestRule.onNodeWithTag(cancelIconTestTag).performClick()

        // then
        assertTrue(callbackInvoked)
    }
}
