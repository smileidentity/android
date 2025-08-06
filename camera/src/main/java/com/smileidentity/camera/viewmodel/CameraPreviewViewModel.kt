package com.smileidentity.camera.viewmodel

import android.content.Context
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.CaptureResult
import android.hardware.camera2.TotalCaptureResult
import androidx.camera.camera2.interop.Camera2Interop
import androidx.camera.camera2.interop.ExperimentalCamera2Interop
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraSelector.DEFAULT_FRONT_CAMERA
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceOrientedMeteringPointFactory
import androidx.camera.core.SurfaceRequest
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.toComposeRect
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@ExperimentalCamera2Interop
class CameraPreviewViewModel : ViewModel() {
    private val _surfaceRequest = MutableStateFlow<SurfaceRequest?>(null)
    val surfaceRequest: StateFlow<SurfaceRequest?> = _surfaceRequest
    private var surfaceMeteringPointFactory: SurfaceOrientedMeteringPointFactory? = null
    private var cameraControl: CameraControl? = null
    private val _sensorFaceRects = MutableStateFlow(listOf<Rect>())
    val sensorFaceRects: StateFlow<List<Rect>> = _sensorFaceRects.asStateFlow()

    private val cameraPreviewUseCase = Preview.Builder()
        .apply {
            Camera2Interop.Extender(this)
                .setCaptureRequestOption(
                    CaptureRequest.STATISTICS_FACE_DETECT_MODE,
                    CaptureRequest.STATISTICS_FACE_DETECT_MODE_FULL,
                )
                .setSessionCaptureCallback(
                    object : CameraCaptureSession.CaptureCallback() {
                        override fun onCaptureCompleted(
                            session: CameraCaptureSession,
                            request: CaptureRequest,
                            result: TotalCaptureResult,
                        ) {
                            super.onCaptureCompleted(session, request, result)
                            result.get(CaptureResult.STATISTICS_FACES)
                                ?.map { face -> face.bounds.toComposeRect() }
                                ?.toList()
                                ?.let { faces -> _sensorFaceRects.update { faces } }
                        }
                    },
                )
        }
        .build().apply {
            setSurfaceProvider { newSurfaceRequest ->
                _surfaceRequest.update { newSurfaceRequest }
                surfaceMeteringPointFactory = SurfaceOrientedMeteringPointFactory(
                    newSurfaceRequest.resolution.width.toFloat(),
                    newSurfaceRequest.resolution.height.toFloat(),
                )
            }
        }

    suspend fun bindToCamera(context: Context, lifecycleOwner: LifecycleOwner) {
        val processCameraProvider = ProcessCameraProvider.awaitInstance(context)
        val camera = processCameraProvider.bindToLifecycle(
            lifecycleOwner,
            DEFAULT_FRONT_CAMERA,
            cameraPreviewUseCase,
        )
        cameraControl = camera.cameraControl

        // Cancellation signals we're done with the camera
        try {
            awaitCancellation()
        } finally {
            processCameraProvider.unbindAll()
            cameraControl = null
        }
    }
}
