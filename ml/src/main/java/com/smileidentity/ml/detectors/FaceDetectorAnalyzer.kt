package com.smileidentity.ml.detectors

import android.content.Context
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.facedetector.FaceDetector
import com.smileidentity.camera.Analyzer
import com.smileidentity.camera.AnalyzerFactory
import com.smileidentity.ml.states.IdentityScanState

/**
 * Analyzer to run FaceDetector.
 */
class FaceDetectorAnalyzer(context: Context) :
    Analyzer<AnalyzerInput, IdentityScanState, AnalyzerOutput> {

    val baseOptionsBuilder: BaseOptions = BaseOptions.builder()
        .setModelAssetPath(MODEL_NAME)
        .build()

    val optionsBuilder: FaceDetector.FaceDetectorOptions =
        FaceDetector.FaceDetectorOptions.builder()
            .setBaseOptions(baseOptionsBuilder)
            .setMinDetectionConfidence(0.5F)
            .setRunningMode(RunningMode.IMAGE)
            .build()

    val faceDetector: FaceDetector = FaceDetector.createFromOptions(context, optionsBuilder)

    /**
     * we will run face detection here, using MediaPipe and pass back the output
     *
     * we can easily swap out implementations here for other analyzers like Huawei
     */
    override suspend fun analyze(data: AnalyzerInput, state: IdentityScanState): AnalyzerOutput {
        val image = BitmapImageBuilder(data.cameraPreviewImage.image).build()
        val result = faceDetector.detect(image)
        val boundingBoxes = result.detections().map { it ->
            BoundingBox(
                left = it.boundingBox().left,
                top = it.boundingBox().top,
                width = it.boundingBox().width(),
                height = it.boundingBox().height(),
            )
        }
        return FaceDetectorOutput(
            boundingBox = boundingBoxes,
            resultScore = 0F,
            timestampMs = result.timestampMs(),
        )
    }

    class Factory(val context: Context) :
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
            FaceDetectorAnalyzer(context = context)
    }

    companion object {
        const val INPUT_WIDTH = 128
        const val INPUT_HEIGHT = 128
        const val MODEL_NAME = "blaze_face_short_range.tflite"
    }
}
