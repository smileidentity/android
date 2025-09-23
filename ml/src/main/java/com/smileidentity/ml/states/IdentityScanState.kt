package com.smileidentity.ml.states

import com.smileidentity.ml.model.AnalyzerInput
import com.smileidentity.ml.model.AnalyzerOutput

sealed class IdentityScanState(
    val type: ScanType,
    val transitioner: IdentityScanStateTransitioner,
) {

    /**
     * Type of documents being scanned
     */
    enum class ScanType {
        DOCUMENT_FRONT,
        DOCUMENT_BACK,
        SELFIE,
    }

    /**
     * Transitions to the next state based on model output.
     */
    abstract suspend fun consumeTransition(
        analyzerInput: AnalyzerInput,
        analyzerOutput: AnalyzerOutput,
    ): IdentityScanState

    /**
     * Initial state when scan starts, no faces/documents have been detected yet.
     */
    class Initial(type: ScanType, transitioner: IdentityScanStateTransitioner) :
        IdentityScanState(type, transitioner) {
        /**
         * Only transitions to [Found] when ML output type matches scan type
         */
        override suspend fun consumeTransition(
            analyzerInput: AnalyzerInput,
            analyzerOutput: AnalyzerOutput,
        ) = transitioner.transitionFromInitial(
            initialState = this,
            analyzerInput = analyzerInput,
            analyzerOutput = analyzerOutput,
        )
    }

    /**
     * State when scan has found the required type, the machine could stay in this state for a
     * while if more image needs to be processed to reach the next state.
     */
    class Found(type: ScanType, transitioner: IdentityScanStateTransitioner) :
        IdentityScanState(type, transitioner) {
        override suspend fun consumeTransition(
            analyzerInput: AnalyzerInput,
            analyzerOutput: AnalyzerOutput,
        ) = transitioner.transitionFromFound(
            foundState = this,
            analyzerInput = analyzerInput,
            analyzerOutput = analyzerOutput,
        )
    }

    /**
     * State when satisfaction checking passed.
     */
    class Satisfied(type: ScanType, transitioner: IdentityScanStateTransitioner) :
        IdentityScanState(type, transitioner) {
        override suspend fun consumeTransition(
            analyzerInput: AnalyzerInput,
            analyzerOutput: AnalyzerOutput,
        ) = transitioner.transitionFromSatisfied(
            satisfiedState = this,
            analyzerInput = analyzerInput,
            analyzerOutput = analyzerOutput,
        )
    }

    /**
     * State when satisfaction checking failed.
     */
    class Unsatisfied(
        val reason: String,
        type: ScanType,
        transitioner: IdentityScanStateTransitioner,
    ) : IdentityScanState(type, transitioner) {
        override suspend fun consumeTransition(
            analyzerInput: AnalyzerInput,
            analyzerOutput: AnalyzerOutput,
        ) = transitioner.transitionFromUnsatisfied(
            unsatisfiedState = this,
            analyzerInput = analyzerInput,
            analyzerOutput = analyzerOutput,
        )
    }

    /**
     * Terminal state, indicting the scan is finished.
     */
    class Finished(type: ScanType, transitioner: IdentityScanStateTransitioner) :
        IdentityScanState(type, transitioner) {
        override suspend fun consumeTransition(
            analyzerInput: AnalyzerInput,
            analyzerOutput: AnalyzerOutput,
        ) = this
    }

    /**
     * Terminal state, indicating the scan times out.
     */
    class TimeOut(type: ScanType, transitioner: IdentityScanStateTransitioner) :
        IdentityScanState(type, transitioner) {
        override suspend fun consumeTransition(
            analyzerInput: AnalyzerInput,
            analyzerOutput: AnalyzerOutput,
        ) = this
    }

    companion object {
        fun ScanType.isFront() = this == ScanType.DOCUMENT_FRONT

        fun ScanType.isBack() = this == ScanType.DOCUMENT_BACK

        fun ScanType?.isNullOrFront() = this == null || this.isFront()
    }
}
