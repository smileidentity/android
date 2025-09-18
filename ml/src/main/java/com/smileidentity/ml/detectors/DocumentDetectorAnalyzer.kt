package com.smileidentity.ml.detectors

import android.content.Context
import android.graphics.Bitmap
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.objectdetector.ObjectDetector
import com.google.mediapipe.tasks.vision.objectdetector.ObjectDetectorResult

/**
 * Analyzer to run DocumentDetector.
 */
class DocumentDetectorAnalyzer(context: Context) {

    val modelName = "harun.tflite"
//    val baseOptions = BaseOptions.builder().setModelAssetPath(modelName).build()
//    val faceDetectorOptions =
//        FaceDetector.FaceDetectorOptions
//            .builder()
//            .setBaseOptions(baseOptions)
//            .setRunningMode(RunningMode.IMAGE)
//            .build()
//
//    val faceDetector = FaceDetector.createFromOptions(context, faceDetectorOptions)

    val baseOptionsBuilder = BaseOptions.builder().setModelAssetPath(modelName)

    val optionsBuilder = ObjectDetector.ObjectDetectorOptions.builder()
        .setBaseOptions(baseOptionsBuilder.build())

    val options = optionsBuilder.build()
    val objectDetector = ObjectDetector.createFromOptions(context, options)

    /**
     * we will run document detection using MediaPipe and pass back the output
     *
     */
//    override suspend fun analyze(data: AnalyzerInput, state: IdentityScanState): AnalyzerOutput {
//        val image = BitmapImageBuilder(data.image).build()
//        val result = objectDetector.detect(image)
//        return DocumentDetectorOutput(result = result)
//    }

    fun detect(image: Bitmap): ObjectDetectorResult {
        val image = BitmapImageBuilder(image).build()
        return objectDetector.detect(image)
    }
}
