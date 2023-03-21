package com.smileidentity.compose

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.printToLog
import org.junit.Rule
import org.junit.Test

class SmileIdentityAttributionTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testSmileIdentityAttribution() {
        // given
        val logoContentDescription = "Smile Identity logo"
        val attributionText = "Powered by Smile Identity"

        // when
        composeTestRule.setContent { SmileIdentityAttribution() }
        composeTestRule.onRoot().printToLog("TAG")

        // then
        composeTestRule.onNodeWithContentDescription(logoContentDescription).assertIsDisplayed()
        composeTestRule.onNodeWithText(attributionText).assertIsDisplayed()
    }
}
