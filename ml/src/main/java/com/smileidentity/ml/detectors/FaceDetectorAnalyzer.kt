package com.smileidentity.ml.detectors

import android.content.Context
import android.graphics.Bitmap
import androidx.core.graphics.toRect
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.facedetector.FaceDetector
import com.smileidentity.camera.Analyzer
import com.smileidentity.camera.AnalyzerFactory
import com.smileidentity.ml.model.AnalyzerInput
import com.smileidentity.ml.model.AnalyzerOutput
import com.smileidentity.ml.model.FaceDetectorOutput
import com.smileidentity.ml.states.IdentityScanState
import com.smileidentity.ml.util.validateRect

/**
 * Analyzer to run FaceDetector.
 */
class FaceDetectorAnalyzer(context: Context, minDetectionConfidence: Float) :
    Analyzer<AnalyzerInput, IdentityScanState, AnalyzerOutput> {

    private val baseOptions = BaseOptions.builder().setModelAssetPath(MODEL_NAME).build()

    private val faceDetectorOptions =
        FaceDetector.FaceDetectorOptions
            .builder()
            .setBaseOptions(baseOptions)
            .setMinDetectionConfidence(minDetectionConfidence)
            .setRunningMode(RunningMode.IMAGE)
            .build()

    private val faceDetector = FaceDetector.createFromOptions(context, faceDetectorOptions)

    /**
     * we will run face detection here, using MediaPipe and pass back the output
     *
     * we can easily swap out implementations here for other analyzers like Huawei
     */
    override suspend fun analyze(data: AnalyzerInput, state: IdentityScanState): AnalyzerOutput {
        val cameraFrameBitmap = data.cameraPreviewImage.image
        val faces = faceDetector.detect(BitmapImageBuilder(cameraFrameBitmap).build())
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

        return FaceDetectorOutput(faces = faces, resultScore = 1F)
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
            FaceDetectorAnalyzer(context = context, minDetectionConfidence = minDetectionConfidence)
    }

    companion object {
        const val MODEL_NAME = "blaze_face_short_range.tflite"
    }
}
