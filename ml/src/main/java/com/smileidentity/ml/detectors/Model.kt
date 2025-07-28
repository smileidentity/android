package com.smileidentity.ml.detectors

import android.graphics.Rect

/**
 * Input from CameraAdapter, note: the bitmap should already be encoded in RGB value
 */
data class AnalyzerInput(val viewFinderBounds: Rect)

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
data class DocumentDetectorOutput(
    val boundingBox: BoundingBox,
    val resultScore: Float,
    val allScores: List<Float>,
    val blurScore: Float,
) : AnalyzerOutput

/**
 * Output of FaceDetector
 */
data class FaceDetectorOutput(val boundingBox: BoundingBox, val resultScore: Float) :
    AnalyzerOutput
