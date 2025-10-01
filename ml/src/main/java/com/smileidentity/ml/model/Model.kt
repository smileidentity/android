package com.smileidentity.ml.model

import android.graphics.Bitmap
import android.graphics.Rect
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarkerResult
import com.google.mlkit.vision.text.Text
import com.smileidentity.ml.viewmodel.CameraPreviewImage

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

/**
 * Output of FaceLandmark
 */
data class FaceLandmarkOutput(val landmarks: FaceLandmarkerResult) : AnalyzerOutput

/**
 * Output of FaceSpoofDetector
 */
data class FaceSpoofDetectorOutput(val isSpoof: Boolean, val score: Float, val timeMillis: Long) :
    AnalyzerOutput

/**
 * Output of TextRecognition
 */
data class TextRecognitionOutput(val text: Text) : AnalyzerOutput
