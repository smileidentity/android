package com.smileidentity.ml.model

import android.graphics.Bitmap
import android.graphics.Rect
import com.smileidentity.camera.CameraPreviewImage

/**
 * Input from CameraAdapter, note: the bitmap should already be encoded in RGB value
 */
data class AnalyzerInput(val cameraPreviewImage: CameraPreviewImage<Bitmap>)

/**
 * Output interface of ML models
 */
sealed interface AnalyzerOutput

/**
 * Output of DocumentDetector
 */
data class DocumentDetectorOutput(val documents: List<Pair<Bitmap, Rect>>) : AnalyzerOutput

/**
 * Output of FaceDetector
 */
data class FaceDetectorOutput(val faces: List<Pair<Bitmap, Rect>>, val resultScore: Float) :
    AnalyzerOutput

/**
 * Output of FaceSpoofDetector
 */
data class FaceSpoofDetectorOutput(val isSpoof: Boolean, val score: Float, val timeMillis: Long) :
    AnalyzerOutput
