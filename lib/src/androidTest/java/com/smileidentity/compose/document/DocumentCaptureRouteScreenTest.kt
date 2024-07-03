package com.smileidentity.compose.document

import android.Manifest
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.rule.GrantPermissionRule
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.google.common.truth.Truth.assertThat
import com.smileidentity.R
import org.junit.Rule
import org.junit.Test

class DocumentCaptureRouteScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(Manifest.permission.CAMERA)

    @OptIn(ExperimentalPermissionsApi::class)
    private lateinit var permissionState: PermissionState

    @OptIn(ExperimentalPermissionsApi::class)
    @Test
    fun shouldShowPreviewWhenPermissionsGranted() {
        // given
        val cameraPreviewTag = "document_camera_preview"
        val instructionsTag = "document_capture_instructions_screen"

        // when
        composeTestRule.setContent {
            permissionState = rememberPermissionState(Manifest.permission.CAMERA)
            DocumentCaptureScreen(
                jobId = "jobId",
                side = DocumentCaptureSide.Front,
                showInstructions = true,
                showAttribution = true,
                allowGallerySelection = true,
                instructionsHeroImage = R.drawable.si_doc_v_front_hero,
                instructionsTitleText = "",
                instructionsSubtitleText = "",
                captureTitleText = "",
                knownIdAspectRatio = null,
                onConfirm = {},
                onError = {},
                showSkipButton = true,
            )
        }

        // then
        assertThat(permissionState.status.isGranted).isTrue()
        assertThat(permissionState.status.shouldShowRationale).isFalse()
        composeTestRule.onNodeWithTag(instructionsTag).performClick()
        composeTestRule.onNodeWithTag(cameraPreviewTag).assertIsDisplayed()
    }

    @Test
    fun shouldShowDocumentInstructions() {
        // given
        val titleText = "Front of ID"
        val subtitleText = "Make sure all the corners are visible and there is no glare"
        val captureTitle = "captureTitle"

        // when
        composeTestRule.setContent {
            DocumentCaptureScreen(
                jobId = "jobId",
                side = DocumentCaptureSide.Front,
                showInstructions = true,
                showAttribution = true,
                allowGallerySelection = true,
                instructionsHeroImage = R.drawable.si_doc_v_front_hero,
                instructionsTitleText = titleText,
                instructionsSubtitleText = subtitleText,
                captureTitleText = "",
                knownIdAspectRatio = null,
                onConfirm = {},
                onError = {},
                showSkipButton = true,
            )
        }

        // then
        composeTestRule.onNodeWithText(titleText, substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText(subtitleText, substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText(captureTitle, substring = true).assertIsDisplayed()
    }
}
