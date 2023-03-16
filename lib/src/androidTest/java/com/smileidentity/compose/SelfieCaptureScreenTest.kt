package com.smileidentity.compose

import android.Manifest
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.test.rule.GrantPermissionRule
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.smileidentity.SmileIdentity
import com.smileidentity.viewmodel.SelfieViewModel
import com.smileidentity.waitUntilExists
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.junit.Rule
import org.junit.Test
import kotlin.time.Duration.Companion.seconds

class SelfieCaptureScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(Manifest.permission.CAMERA)

    @OptIn(ExperimentalPermissionsApi::class)
    @Test
    fun shouldShowPreviewWhenPermissionsGranted() {
        // given
        val permissionState = mockk<PermissionState>(relaxed = true)
        every { permissionState.status.isGranted } returns true
        val cameraPreviewTag = "cameraPreview"

        // when
        composeTestRule.setContent { SmileIdentity.SmartSelfieRegistrationScreen() }

        // then
        verify(exactly = 0) { permissionState.launchPermissionRequest() }
        composeTestRule.onNodeWithTag(cameraPreviewTag).assertIsDisplayed()
    }

    @Test
    fun attributionShouldBeDisplayed() {
        // given
        val attributionTag = "smileIdentityAttribution"

        // when
        composeTestRule.setContent { SelfieCaptureScreen() }

        // then
        composeTestRule.onNodeWithTag(attributionTag).assertIsDisplayed()
    }

    @Test
    fun shouldShowAgentModeSwitchWhenEnabled() {
        // given
        val labelText = "Agent Mode"
        val switchTag = "agentModeSwitch"

        // when
        composeTestRule.setContent { SelfieCaptureScreen(allowAgentMode = true) }

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
        composeTestRule.setContent { SelfieCaptureScreen(allowAgentMode = false) }

        // then
        composeTestRule.onNodeWithText(labelText).assertDoesNotExist()
        composeTestRule.onNodeWithTag(switchTag).assertDoesNotExist()
    }

    @Test
    fun shouldShowCameraPreview() {
        // given
        val cameraPreviewTag = "cameraPreview"

        // when
        composeTestRule.setContent { SelfieCaptureScreen() }

        // then
        composeTestRule.onNodeWithTag(cameraPreviewTag).assertIsDisplayed()
    }

    @Test
    fun shouldShowInstructions() {
        // given
        val instructionsSubstring = "Put your face inside the oval"

        // when
        composeTestRule.setContent { SelfieCaptureScreen() }

        // then
        composeTestRule.onNodeWithText(instructionsSubstring, substring = true).assertIsDisplayed()
    }

    @Test
    fun shouldShowDirective() {
        // given
        val directiveSubstring = "Smile for the camera"

        // when
        composeTestRule.setContent { SelfieCaptureScreen() }

        // then
        composeTestRule.onNodeWithText(directiveSubstring, substring = true).assertIsDisplayed()
    }

    @Test
    fun shouldAnalyzeImage() {
        // given
        val takePictureTag = "takePictureButton"
        val viewModel: SelfieViewModel = spyk()
        every { viewModel.analyzeImage(any()) } just Runs

        // when
        composeTestRule.apply {
            setContent { SelfieCaptureScreen(viewModel = viewModel) }
            waitUntilExists(hasTestTag(takePictureTag), 1.seconds)
        }

        // then
        verify(atLeast = 1, timeout = 1000) { viewModel.analyzeImage(any()) }
    }
}
