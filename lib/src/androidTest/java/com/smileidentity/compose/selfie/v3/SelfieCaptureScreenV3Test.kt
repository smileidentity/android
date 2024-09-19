package com.smileidentity.compose.selfie.v3

import android.Manifest
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.rule.GrantPermissionRule
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.rememberPermissionState
import com.smileidentity.compose.grantPermissionInDialog
import org.junit.Rule
import org.junit.Test

class SelfieCaptureScreenV3Test {
    @get:Rule
    val composeTestRule = createComposeRule()

    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.CAMERA,
        Manifest.permission.POST_NOTIFICATIONS,
    )

    @OptIn(ExperimentalPermissionsApi::class)
    private lateinit var permissionState: PermissionState

    @OptIn(ExperimentalPermissionsApi::class)
    @Test
    fun shouldShowInstructions() {
        // given
        val instructionsSubstring =
            "Position your head in the camera view. Then move in the direction that is indicated"

        // when
        composeTestRule.setContent {
            permissionState = rememberPermissionState(Manifest.permission.CAMERA)
            SelfieCaptureScreenV3()
        }

        grantPermissionInDialog()

        // then
        composeTestRule.onNodeWithText(instructionsSubstring, substring = true).assertIsDisplayed()
    }
}
