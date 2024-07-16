package com.smileidentity.compose

import android.Manifest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
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
import com.smileidentity.compose.selfie.SelfieCaptureScreen
import com.smileidentity.viewmodel.SelfieViewModel
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
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
    private lateinit var permissionState: PermissionState

    @OptIn(ExperimentalPermissionsApi::class)
    @Test
    fun shouldShowPreviewWhenPermissionsGranted() {
        // given
        val cameraPreviewTag = "selfie_camera_preview"

        // when
        composeTestRule.setContent {
            permissionState = rememberPermissionState(Manifest.permission.CAMERA)
            SelfieCaptureScreen()
        }

        // then
        assertThat(permissionState.status.isGranted).isTrue()
        assertThat(permissionState.status.shouldShowRationale).isFalse()
        composeTestRule.onNodeWithTag(cameraPreviewTag).assertIsDisplayed()
    }

    @Test
    fun attributionShouldBeDisplayed() {
        // given
        val attributionTag = "smile_id_attribution"

        // when
        composeTestRule.setContent { SelfieCaptureScreen() }

        // then
        composeTestRule.onNodeWithTag(attributionTag).assertIsDisplayed()
    }

    @Test
    fun shouldShowAgentModeSwitchWhenEnabled() {
        // given
        val labelText = "Agent Mode"
        val switchTag = "agent_mode_switch"

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
        val switchTag = "agent_mode_switch"

        // when
        composeTestRule.setContent { SelfieCaptureScreen(allowAgentMode = false) }

        // then
        composeTestRule.onNodeWithText(labelText).assertDoesNotExist()
        composeTestRule.onNodeWithTag(switchTag).assertDoesNotExist()
    }

    @Test
    fun shouldShowCameraPreview() {
        // given
        val cameraPreviewTag = "selfie_camera_preview"

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
        val directiveSubstring = "Put your face inside the oval frame and wait until it turns green"

        // when
        composeTestRule.setContent { SelfieCaptureScreen() }

        // then
        composeTestRule.onNodeWithText(directiveSubstring, substring = true).assertIsDisplayed()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun shouldAnalyzeImage() {
        // given
        val takePictureTag = "takePictureButton"
        val viewModel: SelfieViewModel = spyk()
        every { viewModel.analyzeImage(any(), camSelector) } just Runs

        // when
        composeTestRule.apply {
            setContent { SelfieCaptureScreen(viewModel = viewModel) }
            waitUntilAtLeastOneExists(hasTestTag(takePictureTag))
        }

        // then
        verify(atLeast = 1, timeout = 1000) { viewModel.analyzeImage(any(), camSelector) }
    }
}
