package com.smileidentity.ml.detectors

import com.smileidentity.camera.Analyzer
import com.smileidentity.ml.states.IdentityScanState

/**
 * Analyzer to run DocumentDetector.
 */
class DocumentDetectorAnalyzer : Analyzer<AnalyzerInput, IdentityScanState, AnalyzerOutput> {

    override suspend fun analyze(data: AnalyzerInput, state: IdentityScanState): AnalyzerOutput {
        TODO("Not yet implemented")

        /**
         * we will run document detection here, using Tensorflow and pass back the output
         *
         */
    }
}
