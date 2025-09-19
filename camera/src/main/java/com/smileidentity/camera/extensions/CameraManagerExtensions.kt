package com.smileidentity.camera.extensions

import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager

/**
 * Check if image analysis is supported by camera hardware level.
 * */
internal fun CameraManager.isImageAnalysisSupported(lensFacing: Int?): Boolean {
    val cameraId = cameraIdList.firstOrNull {
        getCameraCharacteristics(it).get(CameraCharacteristics.LENS_FACING) == lensFacing
    } ?: return false

    val level = getCameraCharacteristics(cameraId)
        .get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL) ?: 0

    return level >= CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_3
}
