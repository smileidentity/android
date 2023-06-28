package com.smileidentity.compose.consent.bvn

import androidx.compose.ui.res.painterResource
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.smileidentity.R
import com.smileidentity.consent.bvn.BvnConsentScreen
import org.junit.Rule
import org.junit.Test

class BvnConsentScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun shouldDisplayBvnConsentScreen() {

        // when
        composeTestRule.setContent {
            BvnConsentScreen(partnerIcon = painterResource(id = R.drawable.si_logo_with_text))
        }

        // then
        composeTestRule.onNodeWithText("For added security,", substring = true).assertExists()
        composeTestRule.onNodeWithText("you want the BVN", substring = true).assertExists()
        composeTestRule.onNodeWithTag("bvn_consent_continue_button").assertExists()
    }
}