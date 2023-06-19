package com.smileidentity.compose.consent

import androidx.compose.ui.res.painterResource
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.smileidentity.R
import com.smileidentity.consent.ConsentScreen
import org.junit.Assert
import org.junit.Rule
import org.junit.Test

class ConsentScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun shouldInvokeContinueCallbackOnAllowButtonClick() {
        // given
        var callbackInvoked = false
        val onContinueClicked = { callbackInvoked = true }
        val continueButtonText = "Allow"

        // when
        composeTestRule.setContent {
            ConsentScreen(
                partnerIcon = painterResource(id = R.drawable.si_logo_with_text),
                partnerName = "Smile ID",
                productName = "BVN",
                partnerPrivacyPolicy = "https://smileidentity.com/privacy",
                onContinue = onContinueClicked,
                onCancel = {},
            )
        }
        composeTestRule.onNodeWithText(continueButtonText).performClick()

        // then
        Assert.assertTrue(callbackInvoked)
    }

    @Test
    fun shouldInvokeOnGoBackCallbackOnYesButtonClick() {
        // given
        var callbackInvoked = false
        val onCancelClicked = { callbackInvoked = true }
        val cancelButtonText = "Cancel"

        // when
        composeTestRule.setContent {
            ConsentScreen(
                partnerIcon = painterResource(id = R.drawable.si_logo_with_text),
                partnerName = "Smile ID",
                productName = "BVN",
                partnerPrivacyPolicy = "https://smileidentity.com/privacy",
                onContinue = {},
                onCancel = onCancelClicked,
            )
        }
        composeTestRule.onNodeWithText(cancelButtonText).performClick()

        // then
        Assert.assertTrue(callbackInvoked)
    }
}
