package com.smileidentity.compose.selfie

import android.Manifest
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.rule.GrantPermissionRule
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.google.common.truth.Truth.assertThat
import com.smileidentity.compose.denyPermissionInDialog
import com.smileidentity.compose.grantPermissionInDialog
import com.smileidentity.compose.selfie.ui.SmartSelfieInstructionsScreen
import org.junit.Rule
import org.junit.Test

class SmartSelfieInstructionScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(Manifest.permission.CAMERA)

    @OptIn(ExperimentalPermissionsApi::class)
    private lateinit var permissionState: PermissionState

    @OptIn(ExperimentalPermissionsApi::class)
    @Test
    fun shouldInvokeCallbackOnButtonClickAndPermissionGranted() {
        // given
        var callbackInvoked = false
        val onInstructionsAcknowledged = { callbackInvoked = true }

        // when
        composeTestRule.setContent {
            permissionState = rememberPermissionState(Manifest.permission.CAMERA)
            SmartSelfieInstructionsScreen(onInstructionsAcknowledged = onInstructionsAcknowledged)
        }
        composeTestRule.onNodeWithTag("smart_selfie_instructions_ready_button").performClick()
        grantPermissionInDialog()

        // then
        assertThat(permissionState.status.shouldShowRationale).isFalse()
        assertThat(callbackInvoked).isTrue()
    }

    @OptIn(ExperimentalPermissionsApi::class)
    @Test
    fun shouldInvokeCallbackOnButtonClickAndPermissionDenied() {
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
}
