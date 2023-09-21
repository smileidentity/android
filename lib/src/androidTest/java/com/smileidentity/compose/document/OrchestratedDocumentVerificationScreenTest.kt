package com.smileidentity.compose.document

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test

class OrchestratedDocumentVerificationScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun shouldShowInstructions() {
        // given
        val instructionsSubstring = "Submit Front of ID"

        // when
        composeTestRule.setContent {
            OrchestratedDocumentVerificationScreen(
                countryCode = "254",
                documentType = "NATIONAL_ID",
            )
        }

        // then
        composeTestRule.onNodeWithText(instructionsSubstring, substring = true).assertIsDisplayed()
    }

    @Test
    fun shouldNotShowInstructionsWhenDisabled() {
        // given
        val instructionsSubstring = "Submit Front of ID"

        // when
        composeTestRule.setContent {
            OrchestratedDocumentVerificationScreen(
                countryCode = "254",
                documentType = "NATIONAL_ID",
            )
        }

        // then
        composeTestRule.onNodeWithText(instructionsSubstring, substring = true)
            .assertDoesNotExist()
    }
}
