package com.smileidentity.ml.detectors

import android.content.Context
import com.smileidentity.camera.Analyzer
import com.smileidentity.camera.AnalyzerFactory
import com.smileidentity.ml.model.AnalyzerInput
import com.smileidentity.ml.model.AnalyzerOutput
import com.smileidentity.ml.model.FaceSpoofDetectorOutput
import com.smileidentity.ml.states.IdentityScanState
import com.smileidentity.ml.util.cropCenter
import com.smileidentity.ml.util.maxAspectRatioInSize
import com.smileidentity.ml.util.size
import org.tensorflow.lite.DataType
import org.tensorflow.lite.InterpreterApi
import org.tensorflow.lite.gpu.CompatibilityList
import org.tensorflow.lite.gpu.GpuDelegate
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.ops.CastOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage

class FaceSpoofDetectorAnalyzer(
    context: Context,
    useGpu: Boolean,
    useXNNPack: Boolean,
    useNNAPI: Boolean,
) : Analyzer<AnalyzerInput, IdentityScanState, AnalyzerOutput> {

    private lateinit var interpreter: InterpreterApi

    private val imageTensorProcessor =
        ImageProcessor
            .Builder()
            .add(CastOp(INPUT_TENSOR_TYPE))
            .build()

    init {
        val interpreterOptions =
            InterpreterApi.Options().apply {
                if (useGpu) {
                    // Add the GPU Delegate if supported.
                    // See -> https://www.tensorflow.org/lite/performance/gpu#android
                    if (CompatibilityList().isDelegateSupportedOnThisDevice) {
                        addDelegate(GpuDelegate(CompatibilityList().bestOptionsForThisDevice))
                    }
                } else {
                    // Number of threads for computation
                    numThreads = 4
                }
                useXNNPACK = useXNNPack
                this.useNNAPI = useNNAPI
            }
        interpreter =
            InterpreterApi.create(
                FileUtil.loadMappedFile(context, MODEL_NAME),
                interpreterOptions,
            )
    }

    override suspend fun analyze(data: AnalyzerInput, state: IdentityScanState): AnalyzerOutput {
        val croppedImage = data.cameraPreviewImage.image.cropCenter(
            maxAspectRatioInSize(
                data.cameraPreviewImage.image.size(),
                1f,
            ),
        )

        imageTensorProcessor.process(TensorImage.fromBitmap(croppedImage))

        // process spoof detection here

        return FaceSpoofDetectorOutput(
            isSpoof = false,
            score = 0.7F,
            timeMillis = 0L,
        )
    }

    class Factory(
        val context: Context,
        val useGpu: Boolean,
        val useXNNPack: Boolean,
        val useNNAPI: Boolean,
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
            FaceSpoofDetectorAnalyzer(
                context = context,
                useGpu = useGpu,
                useXNNPack = useXNNPack,
                useNNAPI = useNNAPI,
            )
    }

    companion object {
        val INPUT_TENSOR_TYPE: DataType = DataType.FLOAT32
        const val MODEL_NAME = "spoof_detector.tflite"
    }
}
