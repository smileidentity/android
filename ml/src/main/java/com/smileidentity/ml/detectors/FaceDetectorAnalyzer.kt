package com.smileidentity.ml.detectors

import android.content.Context
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.facedetector.FaceDetector
import com.smileidentity.camera.Analyzer
import com.smileidentity.ml.states.IdentityScanState

/**
 * Analyzer to run FaceDetector.
 */
class FaceDetectorAnalyzer(context: Context) :
    Analyzer<AnalyzerInput, IdentityScanState, AnalyzerOutput> {

    val baseOptionsBuilder: BaseOptions = BaseOptions.builder().setModelAssetPath(
        "modelName",
    ).build()
    val optionsBuilder: FaceDetector.FaceDetectorOptions =
        FaceDetector.FaceDetectorOptions.builder()
            .setBaseOptions(baseOptionsBuilder)
            .setMinDetectionConfidence(0.3F)
            .setRunningMode(RunningMode.IMAGE)
            .build()
    val faceDetector: FaceDetector = FaceDetector.createFromOptions(context, optionsBuilder)

    /**
     * we will run face detection here, using MediaPipe and pass back the output
     *
     * we can easily swap out implementations here for other analyzers like Huawei
     */
    override suspend fun analyze(data: AnalyzerInput, state: IdentityScanState): AnalyzerOutput {
        val image = BitmapImageBuilder(data.image).build()
        val result = faceDetector.detect(image)
        return FaceDetectorOutput(result = result)
    }
}
