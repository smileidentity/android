package com.smileidentity.compose

import android.Manifest
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DocumentCaptureInstructionScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(Manifest.permission.CAMERA)

    @Test
    fun shouldInvokeTakePhotoCallbackOnButtonClick() {
        // given
        var callbackInvoked = false
        val onTakePhoto = { callbackInvoked = true }

        // when
        composeTestRule.setContent {
            DocumentCaptureInstructionsScreen(onInstructionsAcknowledgedTakePhoto = onTakePhoto)
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
        var callbackInvoked = false
        val onUploadPhoto = { callbackInvoked = true }

        // when
        composeTestRule.setContent {
            DocumentCaptureInstructionsScreen(
                allowPhotoFromGallery = true,
                onInstructionsAcknowledgedSelectFromGallery = onUploadPhoto,
                onInstructionsAcknowledgedTakePhoto = { },
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
        // when
        composeTestRule.setContent {
            DocumentCaptureInstructionsScreen(
                allowPhotoFromGallery = false,
                onInstructionsAcknowledgedTakePhoto = { },
            )
        }

        // then
        composeTestRule.onNodeWithText("Take Photo").assertIsDisplayed()
        composeTestRule.onNodeWithText("Upload Photo").assertDoesNotExist()
    }
}
