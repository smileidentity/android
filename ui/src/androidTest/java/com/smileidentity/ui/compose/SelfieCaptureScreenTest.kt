package com.smileidentity.ui.compose

import android.Manifest
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.rule.GrantPermissionRule
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.isGranted
import com.smileidentity.ui.core.SelfieCaptureResultCallback
import com.smileidentity.ui.viewmodel.SelfieViewModel
import com.smileidentity.ui.waitUntilExists
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.junit.Rule
import org.junit.Test

class SelfieCaptureScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(Manifest.permission.CAMERA)

    @OptIn(ExperimentalPermissionsApi::class)
    @Test
    fun shouldRequestPermissionsWhenNotGranted() {
        // given
        val permissionState = mockk<PermissionState>()
        every { permissionState.status } returns PermissionStatus.Denied(true)
        every { permissionState.launchPermissionRequest() } just Runs

        // when
        composeTestRule.setContent {
            SelfieCaptureOrPermissionScreen(cameraPermissionState = permissionState)
        }

        // then
        verify(exactly = 1, timeout = 1000) { permissionState.launchPermissionRequest() }
    }

    @OptIn(ExperimentalPermissionsApi::class)
    @Test
    fun shouldShowPreviewWhenPermissionsGranted() {
        // given
        val permissionState = mockk<PermissionState>(relaxed = true)
        every { permissionState.status.isGranted } returns true
        val cameraPreviewTag = "cameraPreview"

        // when
        composeTestRule.setContent { SelfieCaptureOrPermissionScreen() }

        // then
        verify(exactly = 0) { permissionState.launchPermissionRequest() }
        composeTestRule.onNodeWithTag(cameraPreviewTag).assertIsDisplayed()
    }

    @Test
    fun attributionShouldBeDisplayed() {
        // given
        val attributionTag = "smileIdentityAttribution"

        // when
        composeTestRule.setContent { SelfieCaptureScreenContent() }

        // then
        composeTestRule.onNodeWithTag(attributionTag).assertIsDisplayed()
    }

    @Test
    fun shouldShowAgentModeSwitchWhenEnabled() {
        // given
        val labelText = "Agent Mode"
        val switchTag = "agentModeSwitch"

        // when
        composeTestRule.setContent { SelfieCaptureScreenContent(allowAgentMode = true) }

        // then
        composeTestRule.onNodeWithText(labelText).assertIsDisplayed()
        composeTestRule.onNodeWithTag(switchTag).assertIsDisplayed()
    }

    @Test
    fun shouldNotShowAgentModeSwitchWhenDisabled() {
        // given
        val labelText = "Agent Mode"
        val switchTag = "agentModeSwitch"

        // when
        composeTestRule.setContent { SelfieCaptureScreenContent(allowAgentMode = false) }

        // then
        composeTestRule.onNodeWithText(labelText).assertDoesNotExist()
        composeTestRule.onNodeWithTag(switchTag).assertDoesNotExist()
    }

    @Test
    fun shouldShowCameraPreview() {
        // given
        val cameraPreviewTag = "cameraPreview"

        // when
        composeTestRule.setContent { SelfieCaptureScreenContent() }

        // then
        composeTestRule.onNodeWithTag(cameraPreviewTag).assertIsDisplayed()
    }

    @Test
    fun shouldShowInstructions() {
        // given
        val instructionsSubstring = "Put your face inside the oval"

        // when
        composeTestRule.setContent { SelfieCaptureScreenContent() }

        // then
        composeTestRule.onNodeWithText(instructionsSubstring, substring = true).assertIsDisplayed()
    }

    @Test
    fun shouldShowDirective() {
        // given
        val directiveSubstring = "Smile for the camera"

        // when
        composeTestRule.setContent { SelfieCaptureScreenContent() }

        // then
        composeTestRule.onNodeWithText(directiveSubstring, substring = true).assertIsDisplayed()
    }

    @Test
    fun shouldCallTakePicture() {
        // given
        val takePictureTag = "takePictureButton"
        val viewModel: SelfieViewModel = spyk()
        val callback = SelfieCaptureResultCallback { }
        every { viewModel.takePicture(any(), any()) } just Runs

        // when
        composeTestRule.apply {
            setContent { SelfieCaptureScreenContent(viewModel = viewModel, onResult = callback) }
            waitUntilExists(hasTestTag(takePictureTag))
        }
        composeTestRule.onNodeWithTag(takePictureTag).performClick()

        // then
        verify(exactly = 1, timeout = 1000) { viewModel.takePicture(any(), eq(callback)) }
    }

    @Test
    fun shouldAnalyzeImage() {
        // given
        val takePictureTag = "takePictureButton"
        val viewModel: SelfieViewModel = spyk()
        every { viewModel.analyzeImage(any()) } just Runs

        // when
        composeTestRule.apply {
            setContent { SelfieCaptureScreenContent(viewModel = viewModel) }
            waitUntilExists(hasTestTag(takePictureTag))
        }

        // then
        verify(atLeast = 1, timeout = 1000) { viewModel.analyzeImage(any()) }
    }
}
