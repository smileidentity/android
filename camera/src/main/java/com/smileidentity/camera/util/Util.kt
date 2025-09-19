package com.smileidentity.camera.util

import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.annotation.CheckResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.smileidentity.camera.state.CameraState

/**
 * Camera State from [CameraPreview] Composable.
 * */
@Composable
fun rememberCameraState(): CameraState {
    val context = LocalContext.current
    return remember { CameraState(context) }
}

/**
 * Rotate a [Bitmap] by the given [rotationDegrees].
 */
@CheckResult
fun Bitmap.rotate(rotationDegrees: Float): Bitmap = if (rotationDegrees != 0F) {
    val matrix = Matrix()
    matrix.postRotate(rotationDegrees)
    Bitmap.createBitmap(this, 0, 0, this.width, this.height, matrix, true)
} else {
    this
}
