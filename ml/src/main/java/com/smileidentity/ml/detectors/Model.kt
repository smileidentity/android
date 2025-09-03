package com.smileidentity.ml.detectors

import android.graphics.Bitmap
import com.google.mediapipe.tasks.vision.facedetector.FaceDetectorResult
import com.google.mediapipe.tasks.vision.objectdetector.ObjectDetectorResult

/**
 * Input from CameraAdapter, note: the bitmap should already be encoded in RGB value
 */
data class AnalyzerInput(val image: Bitmap)

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
