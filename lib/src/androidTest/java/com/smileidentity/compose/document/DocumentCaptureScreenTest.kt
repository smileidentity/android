package com.smileidentity.compose.document

import android.Manifest
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.test.rule.GrantPermissionRule
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.google.common.truth.Truth.assertThat
import com.smileidentity.R
import com.smileidentity.models.Document
import org.junit.Rule
import org.junit.Test

class DocumentCaptureScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(Manifest.permission.CAMERA)

    @OptIn(ExperimentalPermissionsApi::class)
    private lateinit var permissionState: PermissionState

    private val testDocument = Document("KE", "ID_CARD")

    @OptIn(ExperimentalPermissionsApi::class)
    @Test
    fun shouldShowPreviewWhenPermissionsGranted() {
        // given
        val cameraPreviewTag = "document_camera_preview"

        // when
        composeTestRule.setContent {
            permissionState = rememberPermissionState(Manifest.permission.CAMERA)
            DocumentCaptureScreen(
                titleText = stringResource(id = R.string.si_doc_v_capture_instructions_front_title),
                subtitleText = stringResource(id = R.string.si_doc_v_capture_instructions_subtitle),
                idType = testDocument,
            )
        }

        // then
        assertThat(permissionState.status.isGranted).isTrue()
        assertThat(permissionState.status.shouldShowRationale).isFalse()
        composeTestRule.onNodeWithTag(cameraPreviewTag).assertIsDisplayed()
    }

    @Test
    fun shouldShowCameraPreview() {
        // given
        val titleText = "Front of ID"
        val subtitleText = "Make sure all the corners are visible and there is no glare"
        val cameraPreviewTag = "document_camera_preview"

        // when
        composeTestRule.setContent {
            DocumentCaptureScreen(
                titleText = titleText,
                subtitleText = subtitleText,
                idType = testDocument,
            )
        }

        // then
        composeTestRule.onNodeWithTag(cameraPreviewTag).assertIsDisplayed()
    }

    @Test
    fun shouldShowDocumentInstructions() {
        // given
        val titleText = "Front of ID"
        val subtitleText = "Make sure all the corners are visible and there is no glare"

        // when
        composeTestRule.setContent {
            DocumentCaptureScreen(
                titleText = titleText,
                subtitleText = subtitleText,
                idType = testDocument,
            )
        }

        // then
        composeTestRule.onNodeWithText(titleText, substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText(subtitleText, substring = true).assertIsDisplayed()
    }
}
