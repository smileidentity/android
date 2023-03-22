package com.smileidentity.compose

import android.Manifest
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.rule.GrantPermissionRule
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.PermissionStatus
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SmartSelfieInstructionScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(Manifest.permission.CAMERA)

    @OptIn(ExperimentalPermissionsApi::class)
    @Test
    fun shouldInvokeCallbackOnButtonClick() {
        // given
        val permissionState = mockk<PermissionState>(relaxed = true)
        every { permissionState.status } returns PermissionStatus.Granted
        var callbackInvoked = false
        val onInstructionsAcknowledged = { callbackInvoked = true }

        // when
        composeTestRule.setContent {
            SmartSelfieInstructionsScreen(permissionState, onInstructionsAcknowledged)
        }
        composeTestRule.onNodeWithTag("readyButton").performClick()

        // then
        assertTrue(callbackInvoked)
    }

    @OptIn(ExperimentalPermissionsApi::class)
    @Test
    fun shouldRequestPermissionsWhenNotGranted() {
        // given
        val permissionState = mockk<PermissionState>()
        every { permissionState.status } returns PermissionStatus.Denied(false)
        every { permissionState.launchPermissionRequest() } just Runs

        // when
        composeTestRule.setContent {
            SmartSelfieInstructionsScreen(cameraPermissionState = permissionState)
        }
        composeTestRule.onNodeWithTag("readyButton").performClick()

        // then
        verify(exactly = 1, timeout = 1000) { permissionState.launchPermissionRequest() }
    }
}
