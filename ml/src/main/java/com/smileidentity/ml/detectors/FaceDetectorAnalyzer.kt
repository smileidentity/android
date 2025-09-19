package com.smileidentity.ml.detectors

import android.content.Context
import android.graphics.Bitmap
import android.os.SystemClock
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.framework.image.MPImage
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.facedetector.FaceDetector
import com.google.mediapipe.tasks.vision.facedetector.FaceDetectorResult
import com.smileidentity.camera.Analyzer
import com.smileidentity.camera.AnalyzerFactory
import com.smileidentity.ml.states.IdentityScanState
import timber.log.Timber

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
            .setResultListener(this::returnLivestreamResult)
            .setErrorListener(this::returnLivestreamError)
            .setRunningMode(RunningMode.LIVE_STREAM)
            .build()

    val faceDetector: FaceDetector = FaceDetector.createFromOptions(context, optionsBuilder)

    private fun returnLivestreamResult(result: FaceDetectorResult, input: MPImage) {
        Timber.d("Juuuuuuuuuuuma result $result")
        Timber.d("Juuuuuuuuuuuma result ${input.height} ${input.width}")
    }

    private fun returnLivestreamError(error: RuntimeException) {
        Timber.d("Juuuuuuuuuuuma error $error")
    }

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

    fun detect(image: Bitmap) {
        val croppedImage = data.cameraPreviewImage.image.cropCenter(
            size = maxAspectRatioInSize(
                area = data.cameraPreviewImage.image.size(),
                aspectRatio = 1f,
            ),
        )

        val image = BitmapImageBuilder(image).build()
        val frameTime = SystemClock.uptimeMillis()
        faceDetector.detectAsync(image, frameTime)
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
