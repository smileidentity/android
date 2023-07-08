package com.smileidentity.compose.components

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.smileidentity.R
import com.smileidentity.SmileIDOptIn
import com.smileidentity.util.toast

@SmileIDOptIn
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraPermissionButton(
    text: String,
    modifier: Modifier = Modifier,
    onGranted: () -> Unit,
) {
    val context = LocalContext.current
    val permissionState = rememberPermissionState(Manifest.permission.CAMERA) { granted ->
        if (granted) {
            onGranted()
        } else {
            // The user denied the permission which means Android won't show the permission request
            // dialog again. So, we need to direct the user to the app settings screen to manually
            // enable the permission.
            context.toast(R.string.si_camera_permission_rationale)
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
            }
            context.startActivity(intent)
        }
    }
    Button(
        onClick = {
            permissionState.launchPermissionRequest()
            if (permissionState.status.shouldShowRationale) {
                context.toast(R.string.si_camera_permission_rationale)
            }
        },
        modifier = modifier,
    ) { Text(text) }
}
