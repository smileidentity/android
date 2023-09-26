package com.smileidentity.compose.consent

import androidx.compose.ui.res.painterResource
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.smileidentity.R
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.net.URL

class OrchestratedConsentScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun shouldShowConsentScreenInitially() {
        // when
        composeTestRule.setContent {
            OrchestratedConsentScreen(
                partnerIcon = painterResource(id = R.drawable.si_logo_with_text),
                partnerName = "Smile ID",
                productName = "BVN",
                partnerPrivacyPolicy = URL("https://usesmileid.com/privacy"),
                onConsentGranted = {},
                onConsentDenied = {},
            )
        }

        // then
        composeTestRule.onNodeWithTag("consent_screen").assertExists()
    }

    @Test
    fun shouldInvokeConsentGrantedCallback() {
        // given
        var callbackInvoked = false

        // when
        composeTestRule.setContent {
            OrchestratedConsentScreen(
                partnerIcon = painterResource(id = R.drawable.si_logo_with_text),
                partnerName = "Smile ID",
                productName = "BVN",
                partnerPrivacyPolicy = URL("https://usesmileid.com/privacy"),
                onConsentGranted = { callbackInvoked = true },
                onConsentDenied = {},
            )
        }
        composeTestRule.onNodeWithText("Allow").performClick()

        // then
        assertTrue(callbackInvoked)
    }

    @Test
    fun shouldShowConsentDeniedScreen() {
        // when
        composeTestRule.setContent {
            OrchestratedConsentScreen(
                partnerIcon = painterResource(id = R.drawable.si_logo_with_text),
                partnerName = "Smile ID",
                productName = "BVN",
                partnerPrivacyPolicy = URL("https://usesmileid.com/privacy"),
                onConsentGranted = {},
                onConsentDenied = {},
            )
        }
        composeTestRule.onNodeWithText("Cancel").performClick()

        // then
        composeTestRule.onNodeWithTag("consent_denied_screen").assertExists()
    }

    @Test
    fun shouldGoBackFromConsentDeniedScreen() {
        // when
        composeTestRule.setContent {
            OrchestratedConsentScreen(
                partnerIcon = painterResource(id = R.drawable.si_logo_with_text),
                partnerName = "Smile ID",
                productName = "BVN",
                partnerPrivacyPolicy = URL("https://usesmileid.com/privacy"),
                onConsentGranted = {},
                onConsentDenied = {},
            )
        }
        composeTestRule.onNodeWithText("Cancel").performClick()
        composeTestRule.onNodeWithText("Yes, go back").performClick()

        // then
        composeTestRule.onNodeWithTag("consent_screen").assertExists()
    }

    @Test
    fun shouldInvokeConsentDeniedCallback() {
        // given
        var callbackInvoked = false

        // when
        composeTestRule.setContent {
            OrchestratedConsentScreen(
                partnerIcon = painterResource(id = R.drawable.si_logo_with_text),
                partnerName = "Smile ID",
                productName = "BVN",
                partnerPrivacyPolicy = URL("https://usesmileid.com/privacy"),
                onConsentGranted = {},
                onConsentDenied = { callbackInvoked = true },
            )
        }
        composeTestRule.onNodeWithText("Cancel").performClick()
        composeTestRule.onNodeWithText("No, cancel verification").performClick()

        // then
        assertTrue(callbackInvoked)
    }
}
