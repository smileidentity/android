package com.smileidentity.ml.detectors

import android.content.Context
import com.smileidentity.camera.Analyzer
import com.smileidentity.camera.AnalyzerFactory
import com.smileidentity.ml.model.AnalyzerInput
import com.smileidentity.ml.model.AnalyzerOutput
import com.smileidentity.ml.states.IdentityScanState

class TextRecognitionAnalyzer() : Analyzer<AnalyzerInput, IdentityScanState, AnalyzerOutput> {

    override suspend fun analyze(
        data: AnalyzerInput,
        state: IdentityScanState,
    ): AnalyzerOutput {

    }

    class Factory() :
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
            TextRecognitionAnalyzer()
    }
}
