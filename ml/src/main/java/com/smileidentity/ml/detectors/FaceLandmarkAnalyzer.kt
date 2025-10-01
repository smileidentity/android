package com.smileidentity.ml.detectors

import android.content.Context
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarker
import com.smileidentity.camera.Analyzer
import com.smileidentity.camera.AnalyzerFactory
import com.smileidentity.ml.model.AnalyzerInput
import com.smileidentity.ml.model.AnalyzerOutput
import com.smileidentity.ml.model.FaceLandmarkOutput
import com.smileidentity.ml.states.IdentityScanState

/**
 * Analyzer to run FaceLandmarkDetector.
 */
class FaceLandmarkAnalyzer(
    context: Context,
    minDetectionConfidence: Float,
    minTrackingConfidence: Float,
    minFacePresenceConfidence: Float,
) : Analyzer<AnalyzerInput, IdentityScanState, AnalyzerOutput> {

    private val baseOptions =
        BaseOptions.builder().setModelAssetPath(MODEL_NAME).build()

    private val faceLandmarkerOptions =
        FaceLandmarker.FaceLandmarkerOptions
            .builder()
            .setBaseOptions(baseOptions)
            .setMinFaceDetectionConfidence(minDetectionConfidence)
            .setMinTrackingConfidence(minTrackingConfidence)
            .setMinFacePresenceConfidence(minFacePresenceConfidence)
            .setOutputFaceBlendshapes(true)
            .setOutputFacialTransformationMatrixes(true)
            .setNumFaces(MAX_FACES)
            .setRunningMode(RunningMode.IMAGE)
            .build()

    private val faceLandmarker = FaceLandmarker.createFromOptions(context, faceLandmarkerOptions)

    override suspend fun analyze(data: AnalyzerInput, state: IdentityScanState): AnalyzerOutput {
        val cameraFrameBitmap = data.cameraPreviewImage.image
        val landmarks = faceLandmarker.detect(BitmapImageBuilder(cameraFrameBitmap).build())
        return FaceLandmarkOutput(landmarks = landmarks)
    }

    class Factory(
        val context: Context,
        val minDetectionConfidence: Float,
        val minTrackingConfidence: Float,
        val minFacePresenceConfidence: Float,
    ) : AnalyzerFactory<
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
            FaceLandmarkAnalyzer(
                context = context,
                minDetectionConfidence = minDetectionConfidence,
                minTrackingConfidence = minTrackingConfidence,
                minFacePresenceConfidence = minFacePresenceConfidence,
            )
    }

    companion object {
        const val MODEL_NAME = "blaze_face_short_range.tflite"

        /**
         * Smoothing is only applied when num_faces is set to 1
         *
         * We need to set this to once face since [FaceDetectorAnalyzer] filters more than one face
         * anyway, and land marking should happen after face detection is done
         */
        const val MAX_FACES = 1
    }
}
