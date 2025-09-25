package com.smileidentity.ml.scan

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.LifecycleOwner
import com.smileidentity.ml.detectors.DocumentDetectorAnalyzer
import com.smileidentity.ml.detectors.FaceDetectorAnalyzer
import com.smileidentity.ml.model.AnalyzerInput
import com.smileidentity.ml.model.AnalyzerOutput
import com.smileidentity.ml.states.IdentityScanState
import com.smileidentity.ml.viewmodel.CameraPreviewImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Identity's [ScanFlow] implementation, uses a pool of [com.smileidentity.ml.detectors.FaceDetectorAnalyzer]
 * or [com.smileidentity.ml.detectors.DocumentDetectorAnalyzer] to within a [ProcessBoundAnalyzerLoop] to analyze
 * a [Flow] of [CameraPreviewImage]s. The results are handled in [IdentityAggregator].
 */
class IdentityScanFlow : ScanFlow<IdentityScanState.ScanType, CameraPreviewImage<Bitmap>> {

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

            try {
            } catch (e: IllegalStateException) {
                withContext(Dispatchers.Main) {
                    onError(e)
                }

                return@launch
            }
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
