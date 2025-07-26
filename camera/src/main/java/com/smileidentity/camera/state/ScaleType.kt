package com.smileidentity.camera.state

import androidx.camera.view.PreviewView.ScaleType as CameraScaleType

/**
 * Camera scale type.
 *
 * @param type internal scale type from cameraX
 * @see CameraScaleType
 * */
enum class ScaleType(val type: CameraScaleType) {
    FillCenter(CameraScaleType.FILL_CENTER),
}
