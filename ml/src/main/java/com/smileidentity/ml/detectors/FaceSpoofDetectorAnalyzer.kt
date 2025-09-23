package com.smileidentity.ml.detectors

import android.content.Context
import com.smileidentity.camera.Analyzer
import com.smileidentity.camera.AnalyzerFactory
import com.smileidentity.ml.model.AnalyzerInput
import com.smileidentity.ml.model.AnalyzerOutput
import com.smileidentity.ml.states.IdentityScanState

class FaceSpoofDetectorAnalyzer(
    context: Context,
    useGpu: Boolean,
    useXNNPack: Boolean,
    useNNAPI: Boolean,
) :
    Analyzer<AnalyzerInput, IdentityScanState, AnalyzerOutput> {

    override suspend fun analyze(
        data: AnalyzerInput,
        state: IdentityScanState,
    ): AnalyzerOutput {
        TODO("Not yet implemented")
    }

    class Factory(
        val context: Context,
        val useGpu: Boolean,
        val useXNNPack: Boolean,
        val useNNAPI: Boolean,
    ) :
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
            FaceSpoofDetectorAnalyzer(
                context = context,
                useGpu = useGpu,
                useXNNPack = useXNNPack,
                useNNAPI = useNNAPI,
            )
    }

    companion object {
        const val MODEL_NAME = "spoof_detector.tflite"
    }

}
