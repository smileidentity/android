package com.smileidentity.compose.document

import android.Manifest
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.test.rule.GrantPermissionRule
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.smileidentity.SmileID
import com.smileidentity.compose.DocumentVerification
import com.smileidentity.models.Document
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Rule
import org.junit.Test

class DocumentCaptureScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(Manifest.permission.CAMERA)

    private val testDocument = Document("KE", "ID_CARD")

    @OptIn(ExperimentalPermissionsApi::class)
    @Test
    fun shouldShowPreviewWhenPermissionsGranted() {
        // given
        val permissionState = mockk<PermissionState>(relaxed = true)
        every { permissionState.status.isGranted } returns true
        val cameraPreviewTag = "document_camera_preview"

        // when
        composeTestRule.setContent { SmileID.DocumentVerification(idType = testDocument) }

        // then
        verify(exactly = 0) { permissionState.launchPermissionRequest() }
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
