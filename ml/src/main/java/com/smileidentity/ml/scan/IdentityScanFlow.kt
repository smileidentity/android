package com.smileidentity.ml.scan

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.LifecycleOwner
import com.smileidentity.camera.CameraPreviewImage
import com.smileidentity.ml.detectors.DocumentDetectorAnalyzer
import com.smileidentity.ml.detectors.FaceDetectorAnalyzer
import com.smileidentity.ml.model.AnalyzerInput
import com.smileidentity.ml.model.AnalyzerOutput
import com.smileidentity.ml.states.IdentityScanState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Identity's [ScanFlow] implementation, uses a pool of [com.smileidentity.ml.detectors.FaceDetectorAnalyzer]
 * or [com.smileidentity.ml.detectors.DocumentDetectorAnalyzer] to within a [ProcessBoundAnalyzerLoop] to analyze
 * a [Flow] of [CameraPreviewImage]s. The results are handled in [IdentityAggregator].
 */
class IdentityScanFlow(
    private val analyzerLoopErrorListener: AnalyzerLoopErrorListener,
    private val aggregateResultListener:
    AggregateResultListener<IdentityAggregator.InterimResult, IdentityAggregator.FinalResult>,
) : ScanFlow<IdentityScanState.ScanType, CameraPreviewImage<Bitmap>> {

    private var aggregator: IdentityAggregator? = null

    /**
     * If this is true, do not start the flow.
     */
    private var canceled = false

    /**
     * Pool of analyzers, initialized when [startFlow] is called.
     */
    private var analyzerPool:
        AnalyzerPool<
            AnalyzerInput,
            IdentityScanState,
            AnalyzerOutput,
            >? = null

    /**
     * The loop to execute analyze, initialized upon [analyzerPool] is initialized.
     */
    private var loop:
        ProcessBoundAnalyzerLoop<
            AnalyzerInput,
            IdentityScanState,
            AnalyzerOutput,
            >? = null

    /**
     * The [Job] to track loop, initialized upon [loop] starts.
     */
    private var loopJob: Job? = null

    override fun startFlow(
        context: Context,
        imageStream: Flow<CameraPreviewImage<Bitmap>>,
        lifecycleOwner: LifecycleOwner,
        coroutineScope: CoroutineScope,
        parameters: IdentityScanState.ScanType,
        onError: (Exception) -> Unit,
    ) {
        coroutineScope.launch {
            if (canceled) {
                return@launch
            }

            aggregator = IdentityAggregator(
                identityScanType = parameters,
                aggregateResultListener = aggregateResultListener,
            )

            requireNotNull(aggregator).bindToLifecycle(lifecycleOwner)

            try {
                analyzerPool =
                    AnalyzerPool.of(
                        analyzerFactory = if (parameters == IdentityScanState.ScanType.SELFIE) {
                            FaceDetectorAnalyzer.Factory(
                                context = context,
                                minDetectionConfidence = 0F,
                            )
                        } else {
                            DocumentDetectorAnalyzer.Factory(
                                context = context,
                                minDetectionConfidence = 0F,
                            )
                        },
                    )
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onError(e)
                }

                return@launch
            }

            loop = ProcessBoundAnalyzerLoop(
                analyzerPool = requireNotNull(analyzerPool),
                resultHandler = requireNotNull(aggregator),
                analyzerLoopErrorListener = analyzerLoopErrorListener,
            )

            loopJob = requireNotNull(loop).subscribeTo(
                flow = imageStream.map { cameraPreviewImage ->
                    AnalyzerInput(cameraPreviewImage)
                },
                processingCoroutineScope = coroutineScope,
            )
        }
    }

    override fun cancelFlow() {
        canceled = true
        cleanUp()
    }

    /**
     * Reset the flow to the initial state, ready to be started again
     */
    internal fun resetFlow() {
        canceled = false
        cleanUp()
    }

    private fun cleanUp() {
        aggregator?.run { cancel() }
        aggregator = null

        loop?.unsubscribe()
        loop = null

        analyzerPool?.closeAllAnalyzers()
        analyzerPool = null

        loopJob?.apply {
            if (isActive) {
                cancel()
            }
        }
        loopJob = null
    }
}
