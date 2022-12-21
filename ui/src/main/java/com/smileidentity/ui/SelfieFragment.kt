package com.smileidentity.ui

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.BottomCenter
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.ujizin.camposer.CameraPreview
import com.ujizin.camposer.state.CamSelector
import com.ujizin.camposer.state.ScaleType
import com.ujizin.camposer.state.rememberCamSelector
import com.ujizin.camposer.state.rememberCameraState

class SelfieFragment : Fragment() {

    companion object {
        fun newInstance() = SelfieFragment()
    }

    private lateinit var viewModel: SelfieViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            // Dispose of the Composition when the view's LifecycleOwner is destroyed. see:
            // https://developer.android.com/jetpack/compose/interop/interop-apis#compose-in-fragments
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent { PermissionOrSelfieCaptureScreen() }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[SelfieViewModel::class.java]
        // TODO: Use the ViewModel
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Preview
@Composable
fun PermissionOrSelfieCaptureScreen() {
    val context = LocalContext.current
    val cameraPermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)
    if (cameraPermissionState.status.isGranted) {
        SelfieCaptureScreen()
    } else {
        SideEffect {
            if (cameraPermissionState.status.shouldShowRationale) {
                cameraPermissionState.launchPermissionRequest()
            } else {
                // The user has permanently denied the permission, so we can't request it again.
                // We can, however, direct the user to the app settings screen to manually
                // enable the permission.
                Toast.makeText(context, R.string.si_camera_permission_rationale, Toast.LENGTH_LONG).show()
                context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = android.net.Uri.fromParts("package", context.packageName, null)
                })
            }
        }
    }
}

@Preview
@Composable
fun SelfieCaptureScreen() {
    val cameraState = rememberCameraState()
    val camSelector = rememberCamSelector(CamSelector.Front)
    // Use a white background to implicitly light up user's face
    val backgroundColor = Color.White
    // The progress of the multiple liveness captures
    // TODO: Replace with actual progress
    val captureProgress = 0.5f
    val viewfinderSize = 256.dp
    val progressStrokeWidth = 8.dp
    val progressBarSize = viewfinderSize + progressStrokeWidth * 2
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .padding(16.dp)
    ) {
        // Display only this shape in the Preview -- however, capture the whole image. This is so
        // that the user only sees their face but captures the whole scene, which may provide
        // additional information for the verification process/identifying fraud
        CameraPreview(
            cameraState = cameraState,
            camSelector = camSelector.value,
            // scaleType = ScaleType.FitCenter,
            scaleType = ScaleType.FillCenter,
            modifier = Modifier
                .align(Center)
                .clip(CircleShape)
                .size(viewfinderSize)
        )
        CircularProgressIndicator(
            captureProgress,
            modifier = Modifier
                .size(progressBarSize)
                .align(Center),
            color = SmileIdentityColor.primary,
            strokeWidth = progressStrokeWidth
        )
        Button(
            modifier = Modifier.align(Alignment.TopEnd),
            onClick = { camSelector.value = camSelector.value.inverse }
        ) {
            Icon(Icons.Outlined.Refresh, contentDescription = null)
            Text(text = stringResource(R.string.si_switch_camera))
        }


        // CameraPreview(
        //     cameraState = cameraState,
        //     camSelector = camSelector.value,
        //     modifier = Modifier
        //         .align(Alignment.Center)
        //         .clip(cameraPreviewShape)
        //         // TODO: Border based on liveness progress
        //         .border(8.dp, SmileIdentityColor.primary, cameraPreviewShape)
        //         .width(256.dp)
        //         // .height(256.dp)
        //         .aspectRatio(2/3f)
        // )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(BottomCenter),
            verticalAlignment = CenterVertically,
            horizontalArrangement = spacedBy(16.dp)
        ) {
            Icon(imageVector = Icons.Outlined.Info, contentDescription = null)
            Text(text = stringResource(id = R.string.si_selfie_capture_instructions))
        }
    }
}

@Preview
@Composable
fun PermissionNotGrantedScreen() {

}
