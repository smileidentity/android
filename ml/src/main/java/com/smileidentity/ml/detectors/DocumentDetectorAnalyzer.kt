package com.smileidentity.ml.detectors

import android.content.Context
import android.graphics.Bitmap
import androidx.core.graphics.toRect
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.objectdetector.ObjectDetector
import com.smileidentity.camera.Analyzer
import com.smileidentity.camera.AnalyzerFactory
import com.smileidentity.ml.states.IdentityScanState
import com.smileidentity.ml.util.validateRect

/**
 * Analyzer to run DocumentDetector.
 */
class DocumentDetectorAnalyzer(context: Context, minDetectionConfidence: Float) :
    Analyzer<AnalyzerInput, IdentityScanState, AnalyzerOutput> {

    private val baseOptions = BaseOptions.builder().setModelAssetPath(MODEL_NAME).build()

    private val documentDetectorOptions =
        ObjectDetector.ObjectDetectorOptions
            .builder()
            .setBaseOptions(baseOptions)
            .setScoreThreshold(minDetectionConfidence)
            .setRunningMode(RunningMode.IMAGE)
            .setMaxResults(5)
            .build()

    private val documentDetector = ObjectDetector.createFromOptions(
        context,
        documentDetectorOptions,
    )

    /**
     * we will run document detection using MediaPipe and pass back the output
     *
     */
    override suspend fun analyze(data: AnalyzerInput, state: IdentityScanState): AnalyzerOutput {
        val cameraFrameBitmap = data.cameraPreviewImage.image
        val documents = documentDetector.detect(BitmapImageBuilder(cameraFrameBitmap).build())
            .detections()
            .filter { validateRect(cameraFrameBitmap, it.boundingBox().toRect()) }
            .map { detection -> detection.boundingBox().toRect() }
            .map { rect ->
                val croppedBitmap =
                    Bitmap.createBitmap(
                        cameraFrameBitmap,
                        rect.left,
                        rect.top,
                        rect.width(),
                        rect.height(),
                    )
                Pair(croppedBitmap, rect)
            }

        return DocumentDetectorOutput(documents = documents)
    }

    class Factory(val context: Context, val minDetectionConfidence: Float) :
        AnalyzerFactory<
            AnalyzerInput,
            IdentityScanState,
            AnalyzerOutput,
            Analyzer<AnalyzerInput, IdentityScanState, AnalyzerOutput>,
            > {

        override suspend fun newInstance(): Analyzer<
            AnalyzerInput,
            IdentityScanState,
            AnalyzerOutput,
            > =
            DocumentDetectorAnalyzer(
                context = context,
                minDetectionConfidence = minDetectionConfidence,
            )
    }

    companion object {
        const val MODEL_NAME = "id_detector.tflite"
    }
}
