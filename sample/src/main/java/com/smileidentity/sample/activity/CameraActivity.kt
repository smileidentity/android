package com.smileidentity.sample.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.OptIn
import androidx.camera.camera2.interop.ExperimentalCamera2Interop
import androidx.compose.runtime.remember
import com.smileidentity.camera.ui.CameraPreview
import com.smileidentity.camera.viewmodel.CameraPreviewViewModel

class CameraActivity : ComponentActivity() {

    @OptIn(ExperimentalCamera2Interop::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel = remember { CameraPreviewViewModel() }
            CameraPreview(viewModel)
        }
    }
}
