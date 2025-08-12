package com.smileidentity.ml.detectors

import com.smileidentity.camera.Analyzer
import com.smileidentity.ml.interpreter.InterpreterWrapper
import com.smileidentity.ml.interpreter.InterpreterWrapperImpl
import com.smileidentity.ml.states.IdentityScanState
import java.io.File

/**
 * Analyzer to run DocumentDetector.
 */
class DocumentDetectorAnalyzer(
    modelFile: File,
) : Analyzer<AnalyzerInput, IdentityScanState, AnalyzerOutput> {

    private val interpreterApi: InterpreterWrapper = InterpreterWrapperImpl(
        file = modelFile,
        options = InterpreterOptionsWrapper.Builder().build()
    )

    override suspend fun analyze(data: AnalyzerInput, state: IdentityScanState): AnalyzerOutput {
        TODO("Not yet implemented")

        /**
         * we will run document detection here, using Tensorflow and pass back the output
         *
         */
    }
}
