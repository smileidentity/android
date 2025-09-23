package com.smileidentity.ml.detectors

import android.graphics.Bitmap
import android.graphics.Rect
import com.smileidentity.ml.viewmodel.CameraPreviewImage

/**
 * Result category of IDDetector
 */
enum class Category { NO_ID, PASSPORT, ID_FRONT, ID_BACK, INVALID }

/**
 * Result bounding box coordinates of IDDetector, in percentage values with regard to original image's width/height
 */
data class BoundingBox(val left: Float, val top: Float, val width: Float, val height: Float)

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
data class FaceDetectorOutput(val faces: List<Pair<Bitmap, Rect>>) : AnalyzerOutput
