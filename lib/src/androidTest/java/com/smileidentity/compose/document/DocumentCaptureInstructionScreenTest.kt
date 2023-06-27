package com.smileidentity.compose.document

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DocumentCaptureInstructionScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun shouldInvokeTakePhotoCallbackOnButtonClick() {
        // given
        val titleText = "Front of ID"
        val subtitleText = "Make sure all the corners are visible and there is no glare"
        var callbackInvoked = false
        val onTakePhoto = { callbackInvoked = true }

        // when
        composeTestRule.setContent {
            DocumentCaptureInstructionsScreen(
                onInstructionsAcknowledgedTakePhoto = onTakePhoto,
                title = titleText,
                subtitle = subtitleText,
            )
        }
        composeTestRule.onNodeWithText("Take Photo").assertIsDisplayed()
        composeTestRule.onNodeWithText("Take Photo").performClick()
        composeTestRule.waitForIdle()

        // then
        assertTrue(callbackInvoked)
    }

    @Test
    fun shouldInvokeUploadPhotoCallbackOnButtonClick() {
        // given
        val titleText = "Front of ID"
        val subtitleText = "Make sure all the corners are visible and there is no glare"
        var callbackInvoked = false
        val onUploadPhoto = { callbackInvoked = true }

        // when
        composeTestRule.setContent {
            DocumentCaptureInstructionsScreen(
                allowPhotoFromGallery = true,
                onInstructionsAcknowledgedSelectFromGallery = onUploadPhoto,
                onInstructionsAcknowledgedTakePhoto = { },
                title = titleText,
                subtitle = subtitleText,
            )
        }
        composeTestRule.onNodeWithText("Take Photo").assertIsDisplayed()
        composeTestRule.onNodeWithText("Upload Photo").assertIsDisplayed()
        composeTestRule.onNodeWithText("Upload Photo").performClick()
        composeTestRule.waitForIdle()

        // then
        assertTrue(callbackInvoked)
    }

    @Test
    fun shouldNotShowUploadPhotoButtonWhenGalleryIsNotAllowed() {
        // given
        val titleText = "Front of ID"
        val subtitleText = "Make sure all the corners are visible and there is no glare"

        // when
        composeTestRule.setContent {
            DocumentCaptureInstructionsScreen(
                allowPhotoFromGallery = false,
                onInstructionsAcknowledgedTakePhoto = { },
                title = titleText,
                subtitle = subtitleText,
            )
        }

        // then
        composeTestRule.onNodeWithText("Take Photo").assertIsDisplayed()
        composeTestRule.onNodeWithText("Upload Photo").assertDoesNotExist()
    }

    @Test
    fun shouldShowUploadPhotoButtonWhenGalleryIsNotAllowed() {
        // given
        val titleText = "Front of ID"
        val subtitleText = "Make sure all the corners are visible and there is no glare"

        // when
        composeTestRule.setContent {
            DocumentCaptureInstructionsScreen(
                allowPhotoFromGallery = true,
                onInstructionsAcknowledgedTakePhoto = { },
                title = titleText,
                subtitle = subtitleText,
            )
        }

        // then
        composeTestRule.onNodeWithText("Take Photo").assertIsDisplayed()
        composeTestRule.onNodeWithText("Upload Photo").assertExists()
    }
}
