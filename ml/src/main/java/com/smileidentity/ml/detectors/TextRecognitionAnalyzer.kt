package com.smileidentity.ml.detectors

import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.smileidentity.camera.Analyzer
import com.smileidentity.camera.AnalyzerFactory
import com.smileidentity.ml.model.AnalyzerInput
import com.smileidentity.ml.model.AnalyzerOutput
import com.smileidentity.ml.model.TextRecognitionOutput
import com.smileidentity.ml.states.IdentityScanState

class TextRecognitionAnalyzer : Analyzer<AnalyzerInput, IdentityScanState, AnalyzerOutput> {

    val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    override suspend fun analyze(data: AnalyzerInput, state: IdentityScanState): AnalyzerOutput {
        val cameraFrameBitmap = data.cameraPreviewImage.image
        val image = InputImage.fromBitmap(cameraFrameBitmap, 0)
        val result = recognizer.process(image)
        return TextRecognitionOutput(text = result.result)
    }

    class Factory :
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
