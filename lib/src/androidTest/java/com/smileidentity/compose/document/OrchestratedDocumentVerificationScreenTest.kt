package com.smileidentity.compose.document

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.smileidentity.compose.components.LocalMetadata
import com.smileidentity.models.JobType
import com.smileidentity.util.randomUserId
import com.smileidentity.viewmodel.document.DocumentVerificationViewModel
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
            val metadata = LocalMetadata.current
            OrchestratedDocumentVerificationScreen(
                viewModel = DocumentVerificationViewModel(
                    jobType = JobType.DocumentVerification,
                    userId = randomUserId(),
                    jobId = randomUserId(),
                    allowNewEnroll = false,
                    countryCode = "254",
                    documentType = "NATIONAL_ID",
                    captureBothSides = false,
                    metadata = metadata,
                ),
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
            val metadata = LocalMetadata.current
            OrchestratedDocumentVerificationScreen(
                viewModel = DocumentVerificationViewModel(
                    jobType = JobType.DocumentVerification,
                    userId = randomUserId(),
                    jobId = randomUserId(),
                    allowNewEnroll = false,
                    countryCode = "254",
                    documentType = "NATIONAL_ID",
                    captureBothSides = false,
                    metadata = metadata,
                ),
            )
        }

        // then
        composeTestRule.onNodeWithText(instructionsSubstring, substring = true)
            .assertDoesNotExist()
    }
}
