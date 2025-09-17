package com.smileidentity.ml.detectors

import android.graphics.Bitmap
import android.graphics.Rect
import com.google.mediapipe.tasks.vision.facedetector.FaceDetectorResult
import com.google.mediapipe.tasks.vision.objectdetector.ObjectDetectorResult

/**
 * Input from CameraAdapter, note: the bitmap should already be encoded in RGB value
 */
data class AnalyzerInput(val viewFinderBounds: Rect, val image: Bitmap)

/**
 * Result bounding box coordinates of DocumentDetector/FaceDetector,
 * in percentage values with regard to original image's width/height
 */
data class BoundingBox(val left: Float, val top: Float, val width: Float, val height: Float)

/**
 * Output interface of ML models
 */
sealed interface AnalyzerOutput

/**
 * Output of DocumentDetector
 */
data class DocumentDetectorOutput(val result: ObjectDetectorResult) : AnalyzerOutput

/**
 * Output of FaceDetector
 */
data class FaceDetectorOutput(val result: FaceDetectorResult) : AnalyzerOutput
