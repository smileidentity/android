package com.smileidentity.compose.document

import android.Manifest
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.google.common.truth.Truth.assertThat
import com.smileidentity.compose.denyPermissionInDialog
import com.smileidentity.compose.selfie.SmartSelfieInstructionsScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalPermissionsApi::class)
@RunWith(AndroidJUnit4::class)
class DocumentCaptureInstructionScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        android.Manifest.permission.CAMERA,
    )

    @OptIn(ExperimentalPermissionsApi::class)
    private lateinit var permissionState: PermissionState

    @Test
    fun shouldInvokeTakePhotoCallbackOnButtonClick() {
        // given
        var callbackInvoked = false
        val onInstructionsAcknowledged = { callbackInvoked = true }

        // when
        composeTestRule.setContent {
            permissionState = rememberPermissionState(Manifest.permission.CAMERA)
            SmartSelfieInstructionsScreen(onInstructionsAcknowledged = onInstructionsAcknowledged)
        }
        composeTestRule.onNodeWithTag("smart_selfie_instructions_ready_button").performClick()
        denyPermissionInDialog()

        // then
        assertThat(permissionState.status.shouldShowRationale).isFalse()
        assertThat(callbackInvoked).isTrue()
    }

    // @Test
    // fun shouldInvokeUploadPhotoCallbackOnButtonClick() {
    //     // given
    //     val titleText = "Front of ID"
    //     val subtitleText = "Make sure all the corners are visible and there is no glare"
    //     var callbackInvoked = false
    //     val onUploadPhoto = { _:String? -> callbackInvoked = true }
    //
    //     // when
    //     composeTestRule.setContent {
    //         DocumentCaptureInstructionsScreen(
    //             allowPhotoFromGallery = true,
    //             onInstructionsAcknowledgedSelectFromGallery = onUploadPhoto,
    //             onInstructionsAcknowledgedTakePhoto = { },
    //             heroImage = R.drawable.si_doc_v_front_hero,
    //             title = titleText,
    //             subtitle = subtitleText,
    //         )
    //     }
    //     composeTestRule.onNodeWithText("Take Photo").assertIsDisplayed()
    //     composeTestRule.onNodeWithText("Upload Photo").assertIsDisplayed()
    //     composeTestRule.onNodeWithText("Upload Photo").performClick()
    //     composeTestRule.waitForIdle()
    //
    //     // then
    //     assertTrue(callbackInvoked)
    // }
    //
    // @Test
    // fun shouldNotShowUploadPhotoButtonWhenGalleryIsNotAllowed() {
    //     // given
    //     val titleText = "Front of ID"
    //     val subtitleText = "Make sure all the corners are visible and there is no glare"
    //
    //     // when
    //     composeTestRule.setContent {
    //         DocumentCaptureInstructionsScreen(
    //             allowPhotoFromGallery = false,
    //             onInstructionsAcknowledgedTakePhoto = { },
    //             heroImage = R.drawable.si_doc_v_front_hero,
    //             title = titleText,
    //             subtitle = subtitleText,
    //         )
    //     }
    //
    //     // then
    //     composeTestRule.onNodeWithText("Take Photo").assertIsDisplayed()
    //     composeTestRule.onNodeWithText("Upload Photo").assertDoesNotExist()
    // }
    //
    // @Test
    // fun shouldShowUploadPhotoButtonWhenGalleryIsNotAllowed() {
    //     // given
    //     val titleText = "Front of ID"
    //     val subtitleText = "Make sure all the corners are visible and there is no glare"
    //
    //     // when
    //     composeTestRule.setContent {
    //         DocumentCaptureInstructionsScreen(
    //             allowPhotoFromGallery = true,
    //             onInstructionsAcknowledgedTakePhoto = { },
    //             heroImage = R.drawable.si_doc_v_front_hero,
    //             title = titleText,
    //             subtitle = subtitleText,
    //         )
    //     }
    //
    //     // then
    //     composeTestRule.onNodeWithText("Take Photo").assertIsDisplayed()
    //     composeTestRule.onNodeWithText("Upload Photo").assertExists()
    // }
}
