package com.smileidentity.ml.detectors

import android.content.Context
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.objectdetector.ObjectDetector
import com.smileidentity.camera.Analyzer
import com.smileidentity.camera.AnalyzerFactory
import com.smileidentity.ml.states.IdentityScanState

/**
 * Analyzer to run DocumentDetector.
 */
class DocumentDetectorAnalyzer(context: Context) :
    Analyzer<AnalyzerInput, IdentityScanState, AnalyzerOutput> {

    val baseOptionsBuilder: BaseOptions = BaseOptions.builder()
        .setModelAssetPath(MODEL_NAME)
        .build()

    val optionsBuilder: ObjectDetector.ObjectDetectorOptions =
        ObjectDetector.ObjectDetectorOptions.builder()
            .setBaseOptions(baseOptionsBuilder)
            .setRunningMode(RunningMode.IMAGE)
            .setMaxResults(5)
            .build()

    val objectDetector: ObjectDetector = ObjectDetector.createFromOptions(context, optionsBuilder)

    /**
     * we will run document detection using MediaPipe and pass back the output
     *
     */
    override suspend fun analyze(data: AnalyzerInput, state: IdentityScanState): AnalyzerOutput {
        val image = BitmapImageBuilder(data.cameraPreviewImage.image).build()
        val result = objectDetector.detect(image)
        val boundingBoxes = result.detections().map { it ->
            BoundingBox(
                left = it.boundingBox().left,
                top = it.boundingBox().top,
                width = it.boundingBox().width(),
                height = it.boundingBox().height(),
            )
        }
        return DocumentDetectorOutput(
            boundingBox = boundingBoxes,
            category = Category.ID_FRONT,
            resultScore = 0F,
            blurScore = 0F,
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
            DocumentDetectorAnalyzer(context = context)
    }

    companion object {
        const val MODEL_NAME = "id_detector.tflite"
    }
}
