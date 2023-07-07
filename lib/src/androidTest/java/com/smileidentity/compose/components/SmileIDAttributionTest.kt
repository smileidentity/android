package com.smileidentity.compose.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.printToLog
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test

class SmileIDAttributionTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Ignore("We ignore this test until we enable attribution")
    @Test
    fun testSmileIDAttribution() {
        // given
        val logoContentDescription = "Smile ID logo"
        val attributionText = "Powered by |"

        // when
        composeTestRule.setContent { SmileIDAttribution() }
        composeTestRule.onRoot().printToLog("TAG")

        // then
        composeTestRule.onNodeWithContentDescription(logoContentDescription).assertIsDisplayed()
        composeTestRule.onNodeWithText(attributionText).assertIsDisplayed()
    }
}
