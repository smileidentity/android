package com.smileidentity.ml.states

import com.smileidentity.ml.detectors.AnalyzerInput
import com.smileidentity.ml.detectors.AnalyzerOutput

/**
 * Interface to determine how to transition between [IdentityScanState]s.
 */
internal interface IdentityScanStateTransitioner {
    suspend fun transitionFromInitial(
        initialState: IdentityScanState.Initial,
        analyzerInput: AnalyzerInput,
        analyzerOutput: AnalyzerOutput,
    ): IdentityScanState

    suspend fun transitionFromFound(
        foundState: IdentityScanState.Found,
        analyzerInput: AnalyzerInput,
        analyzerOutput: AnalyzerOutput,
    ): IdentityScanState

    suspend fun transitionFromSatisfied(
        satisfiedState: IdentityScanState.Satisfied,
        analyzerInput: AnalyzerInput,
        analyzerOutput: AnalyzerOutput,
    ): IdentityScanState

    suspend fun transitionFromUnsatisfied(
        unsatisfiedState: IdentityScanState.Unsatisfied,
        analyzerInput: AnalyzerInput,
        analyzerOutput: AnalyzerOutput,
    ): IdentityScanState
}
