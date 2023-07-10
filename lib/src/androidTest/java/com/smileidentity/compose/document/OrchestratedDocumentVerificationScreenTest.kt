package com.smileidentity.compose.document

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.smileidentity.models.Document
import org.junit.Rule
import org.junit.Test

class OrchestratedDocumentVerificationScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val document = Document(countryCode = "254", documentType = "NATIONAL_ID")

    @Test
    fun shouldShowInstructions() {
        // given
        val instructionsSubstring = "Submit Front of ID"

        // when
        composeTestRule.setContent {
            OrchestratedDocumentVerificationScreen(
                idType = document,
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
                idType = document,
                showInstructions = false,
            )
        }

        // then
        composeTestRule.onNodeWithText(instructionsSubstring, substring = true)
            .assertDoesNotExist()
    }
}
