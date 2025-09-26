package com.smileidentity.ml.states

import androidx.annotation.IntegerRes
import com.smileidentity.ml.model.AnalyzerInput
import com.smileidentity.ml.model.AnalyzerOutput
import com.smileidentity.ml.scan.ScanState
import kotlin.time.ComparableTimeMark
import kotlin.time.TimeSource

/**
 * States during scanning a document.
 */
sealed class IdentityScanState(
    val type: ScanType,
    val transitioner: IdentityScanStateTransitioner,
    isFinal: Boolean,
) : ScanState(isFinal = isFinal) {

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
     * Initial state when scan starts, no documents have been detected yet.
     */
    class Initial(type: ScanType, transitioner: IdentityScanStateTransitioner) :
        IdentityScanState(type = type, transitioner = transitioner, isFinal = false) {
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
    class Found(
        type: ScanType,
        transitioner: IdentityScanStateTransitioner,
        internal var reachedStateAt: ComparableTimeMark = TimeSource.Monotonic.markNow(),
        @IntegerRes val feedbackRes: Int? = null,
    ) : IdentityScanState(type = type, transitioner = transitioner, isFinal = false) {
        override suspend fun consumeTransition(
            analyzerInput: AnalyzerInput,
            analyzerOutput: AnalyzerOutput,
        ) = transitioner.transitionFromFound(
            foundState = this,
            analyzerInput = analyzerInput,
            analyzerOutput = analyzerOutput,
        )

        fun withFeedback(@IntegerRes feedbackRes: Int?) = Found(
            type = type,
            transitioner = transitioner,
            reachedStateAt = reachedStateAt,
            feedbackRes = feedbackRes,
        )
    }

    /**
     * State when satisfaction checking passed.
     *
     * Note when Satisfied is reached, [timeoutAt] won't be checked.
     */
    class Satisfied(
        type: ScanType,
        transitioner: IdentityScanStateTransitioner,
        val reachedStateAt: ComparableTimeMark = TimeSource.Monotonic.markNow(),
    ) : IdentityScanState(type = type, transitioner = transitioner, isFinal = false) {
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
        val reachedStateAt: ComparableTimeMark = TimeSource.Monotonic.markNow(),
    ) : IdentityScanState(type = type, transitioner = transitioner, isFinal = false) {
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
        IdentityScanState(type = type, transitioner = transitioner, isFinal = true) {
        override suspend fun consumeTransition(
            analyzerInput: AnalyzerInput,
            analyzerOutput: AnalyzerOutput,
        ) = this
    }

    /**
     * Terminal state, indicating the scan times out.
     */
    class TimeOut(type: ScanType, transitioner: IdentityScanStateTransitioner) :
        IdentityScanState(type = type, transitioner = transitioner, isFinal = true) {
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
