package com.smileidentity.ml.detectors

import android.content.Context
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.objectdetector.ObjectDetector
import com.smileidentity.camera.Analyzer
import com.smileidentity.ml.states.IdentityScanState

/**
 * Analyzer to run DocumentDetector.
 */
class DocumentDetectorAnalyzer(context: Context) :
    Analyzer<AnalyzerInput, IdentityScanState, AnalyzerOutput> {

    val baseOptions: ObjectDetector.ObjectDetectorOptions =
        ObjectDetector.ObjectDetectorOptions.builder()
            .setBaseOptions(BaseOptions.builder().setModelAssetPath("modelName").build())
            .setRunningMode(RunningMode.IMAGE)
            .setMaxResults(5)
            .build()
    val objectDetector: ObjectDetector = ObjectDetector.createFromOptions(context, baseOptions)

    /**
     * we will run document detection using MediaPipe and pass back the output
     *
     */
    override suspend fun analyze(data: AnalyzerInput, state: IdentityScanState): AnalyzerOutput {
        val image = BitmapImageBuilder(data.image).build()
        val result = objectDetector.detect(image)
        return DocumentDetectorOutput(result = result)
    }
}
