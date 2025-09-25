package com.smileidentity.ml.scan

import com.smileidentity.ml.model.AnalyzerInput
import com.smileidentity.ml.model.AnalyzerOutput
import com.smileidentity.ml.states.FaceDetectorTransitioner
import com.smileidentity.ml.states.IdentityScanState

abstract class ScanState(val isFinal: Boolean)

/**
 * [ResultAggregator] for Identity.
 *
 * Initialize the [IdentityScanState.Initial] state with corresponding
 * [com.smileidentity.ml.states.IdentityScanStateTransitioner] based on [IdentityScanState.ScanType].
 */
class IdentityAggregator(
    identityScanType: IdentityScanState.ScanType,
    aggregateResultListener: AggregateResultListener<InterimResult, FinalResult>,
) : ResultAggregator<
    AnalyzerInput,
    IdentityScanState,
    AnalyzerOutput,
    IdentityAggregator.InterimResult,
    IdentityAggregator.FinalResult,
    >(
    aggregateResultListener,
    initialState = IdentityScanState.Initial(
        type = identityScanType,
        transitioner = FaceDetectorTransitioner(
            selfieCaptureTimeout = 10,
            numSamples = 5,
            sampleInterval = 2,
            faceDetectorMinScore = 0F,
            minEdgeThreshold = 1F,
            stayInFoundDuration = 1,
            maxCoverageThreshold = 1F,
            minCoverageThreshold = 1F,
            maxCenteredThresholdY = 1F,
            maxCenteredThresholdX = 1F,
        ),
    ),
) {

    private var isFirstResultReceived = false

    data class InterimResult(val identityState: IdentityScanState)

    data class FinalResult(
        val frame: AnalyzerInput,
        val result: AnalyzerOutput,
        val identityState: IdentityScanState,
    )

    override suspend fun aggregateResult(
        frame: AnalyzerInput,
        result: AnalyzerOutput,
    ): Pair<InterimResult, FinalResult?> {
        if (isFirstResultReceived) {
            val previousState = state
            state = previousState.consumeTransition(analyzerInput = frame, analyzerOutput = result)
            val interimResult = InterimResult(identityState = state)
            return interimResult to
                if (state.isFinal) {
                    FinalResult(
                        frame = frame,
                        result = result,
                        identityState = state,
                    )
                } else {
                    null
                }
        } else {
            // If this is the very first result, don't transition state and post InterimResult with
            // current state(IdentityScanState.Initial).
            // This makes sure the receiver always receives IdentityScanState.Initial as the first
            // callback.
            isFirstResultReceived = true
            return InterimResult(identityState = state) to null
        }
    }
}
