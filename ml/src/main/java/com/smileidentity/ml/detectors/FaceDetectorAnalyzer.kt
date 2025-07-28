package com.smileidentity.ml.detectors

import com.smileidentity.camera.Analyzer
import com.smileidentity.ml.states.IdentityScanState

/**
 * Analyzer to run FaceDetector.
 */
class FaceDetectorAnalyzer : Analyzer<AnalyzerInput, IdentityScanState, AnalyzerOutput> {

    override suspend fun analyze(data: AnalyzerInput, state: IdentityScanState): AnalyzerOutput {
        TODO("Not yet implemented")

        /**
         * we will run face detection here, using MLKit, and supplement the checks using Tensorflow
         * and pass back the output
         *
         * we can easily swap out implementations here for other analyzers like Huawei
         */
    }
}
