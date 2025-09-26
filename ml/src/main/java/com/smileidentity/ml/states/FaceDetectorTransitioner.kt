package com.smileidentity.ml.states

import android.graphics.Rect
import androidx.annotation.VisibleForTesting
import com.smileidentity.camera.util.FrameSaver
import com.smileidentity.ml.model.AnalyzerInput
import com.smileidentity.ml.model.AnalyzerOutput
import com.smileidentity.ml.model.FaceDetectorOutput
import com.smileidentity.ml.util.roundToMaxDecimals
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.time.ComparableTimeMark
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource
import timber.log.Timber

/**
 * [IdentityScanStateTransitioner] for FaceDetector model.
 *
 * To transition from [com.smileidentity.ml.states.IdentityScanState.Initial] state -
 * * Check if it's timeout since the start of the scan.
 * * Check if a valid face is present, see [isFaceValid] for details. Save the frame and transition to Found if so.
 * * Otherwise stay in [com.smileidentity.ml.states.IdentityScanState.Initial]
 *
 * To transition from [com.smileidentity.ml.states.IdentityScanState.Found] state -
 * * Check if it's timeout since the start of the scan.
 * * Wait for an interval between two Found state, if the interval is not reached, keep waiting.
 * * Check if a valid face is present, save the frame and check if enough frames have been collected
 *  * If so, transition to [com.smileidentity.ml.states.IdentityScanState.Satisfied]
 *  * Otherwise check how long it's been since the last transition to [com.smileidentity.ml.states.IdentityScanState.Found]
 *  *   If it's within [stayInFoundDuration], stay in [com.smileidentity.ml.states.IdentityScanState.Found]
 *  *   Otherwise transition to [com.smileidentity.ml.states.IdentityScanState.Unsatisfied]
 *
 * To transition from [com.smileidentity.ml.states.IdentityScanState.Satisfied] state -
 * * Directly transitions to [com.smileidentity.ml.states.IdentityScanState.Finished]
 *
 * To transition from [com.smileidentity.ml.states.IdentityScanState.Unsatisfied] state -
 * * Directly transitions to [com.smileidentity.ml.states.IdentityScanState.Initial]
 */
internal class FaceDetectorTransitioner(
    private val selfieCaptureTimeout: Int,
    private val numSamples: Int,
    private val sampleInterval: Int,
    private val faceDetectorMinScore: Float,
    private val minEdgeThreshold: Float,
    private val maxCoverageThreshold: Float,
    private val minCoverageThreshold: Float,
    private val maxCenteredThresholdY: Float,
    private val maxCenteredThresholdX: Float,
    internal val selfieFrameSaver: SelfieFrameSaver = SelfieFrameSaver(),
    private val stayInFoundDuration: Int = DEFAULT_STAY_IN_FOUND_DURATION,
) : IdentityScanStateTransitioner {
    @VisibleForTesting
    var timeoutAt: ComparableTimeMark =
        TimeSource.Monotonic.markNow() + selfieCaptureTimeout.milliseconds

    @VisibleForTesting
    fun resetAndReturn(): FaceDetectorTransitioner {
        timeoutAt = TimeSource.Monotonic.markNow() + selfieCaptureTimeout.milliseconds
        return this
    }

    internal val filteredFrames: List<Pair<AnalyzerInput, FaceDetectorOutput>>
        get() {
            val savedFrames = requireNotNull(selfieFrameSaver.getSavedFrames()[SELFIES]) {
                "No frames saved"
            }
            require(savedFrames.size >= NUM_FILTERED_FRAMES) {
                "Not enough frames saved, frames saved: ${savedFrames.size}"
            }

            // return the first, the best(based on resultScore) and the last frame collected
            return mutableListOf(
                savedFrames.last(),
                requireNotNull(
                    savedFrames.subList(1, savedFrames.size - 1)
                        .maxByOrNull { it.second.resultScore },
                ) { "Couldn't find best frame" },
                savedFrames.first(),
            )
        }

    internal val numFrames = numSamples

    internal val bestFaceScore: Float
        get() {
            return filteredFrames[INDEX_BEST].second.resultScore
        }

    internal val scoreVariance: Float
        get() {
            val savedFrames = requireNotNull(selfieFrameSaver.getSavedFrames()[SELFIES]) {
                "No frames saved"
            }
            require(savedFrames.size == numFrames) {
                "Not enough frames saved, score variance not calculated"
            }
            val mean =
                savedFrames.fold(initial = 0f) { acc, pair ->
                    acc + pair.second.resultScore
                }.div(other = numFrames.toFloat())

            return sqrt(
                savedFrames.fold(initial = 0f) { acc, pair ->
                    acc + (pair.second.resultScore - mean).pow(2)
                }.div(numFrames.toFloat()),
            ).roundToMaxDecimals(decimals = 2)
        }

    internal class SelfieFrameSaver :
        FrameSaver<String, Pair<AnalyzerInput, FaceDetectorOutput>, AnalyzerOutput>() {
        // Don't limit max number of saved frames, let the transitioner decide when to stop saving
        // new frames.
        override fun getMaxSavedFrames(savedFrameIdentifier: String) = Int.MAX_VALUE

        override fun getSaveFrameIdentifier(
            frame: Pair<AnalyzerInput, FaceDetectorOutput>,
            metaData: AnalyzerOutput,
        ) = SELFIES

        fun selfieCollected(): Int = getSavedFrames()[SELFIES]?.size ?: 0
    }

    override suspend fun transitionFromInitial(
        initialState: IdentityScanState.Initial,
        analyzerInput: AnalyzerInput,
        analyzerOutput: AnalyzerOutput,
    ): IdentityScanState {
        require(analyzerOutput is FaceDetectorOutput) {
            "Unexpected output type: $analyzerOutput"
        }
        selfieFrameSaver.reset()
        return when {
            timeoutAt.hasPassedNow() -> {
                Timber.d("Timeout in Initial state: $initialState")
                IdentityScanState.TimeOut(initialState.type, this)
            }

            isFaceValid(analyzerOutput) -> {
                Timber.d("Valid face found, transition to Found")
                selfieFrameSaver.saveFrame(
                    frame =
                    (analyzerInput to analyzerOutput),
                    metaData = analyzerOutput,
                )
                IdentityScanState.Found(initialState.type, this)
            }

            else -> {
                Timber.d("Valid face not found, stay in Initial")
                initialState
            }
        }
    }

    override suspend fun transitionFromFound(
        foundState: IdentityScanState.Found,
        analyzerInput: AnalyzerInput,
        analyzerOutput: AnalyzerOutput,
    ): IdentityScanState {
        require(analyzerOutput is FaceDetectorOutput) { "Unexpected output type: $analyzerOutput" }
        return when {
            timeoutAt.hasPassedNow() -> {
                Timber.d("Timeout in Found state: $foundState")
                IdentityScanState.TimeOut(foundState.type, this)
            }

            foundState.reachedStateAt.elapsedNow() < sampleInterval.milliseconds -> {
                Timber.d(
                    "Get a selfie before selfie capture interval, ignored. " +
                        "Current selfieCollected: ${selfieFrameSaver.selfieCollected()}",
                )
                foundState
            }

            isFaceValid(analyzerOutput) -> {
                selfieFrameSaver.saveFrame(
                    frame = (analyzerInput to analyzerOutput),
                    metaData = analyzerOutput,
                )
                if (selfieFrameSaver.selfieCollected() >= numSamples) {
                    Timber.d(
                        "A valid selfie captured, enough selfie " +
                            "collected($numSamples), transitions to Satisfied",
                    )
                    IdentityScanState.Satisfied(foundState.type, this)
                } else {
                    Timber.d(
                        "A valid selfie captured, need $numSamples selfies" +
                            " but has ${selfieFrameSaver.selfieCollected()}, stays in Found",
                    )
                    IdentityScanState.Found(foundState.type, this)
                }
            }

            foundState.reachedStateAt.elapsedNow() < stayInFoundDuration.milliseconds -> {
                Timber.d(
                    "Get an invalid selfie in Found state, but not enough time " +
                        "passed(${foundState.reachedStateAt.elapsedNow()}), stays in Found. " +
                        "Current selfieCollected: ${selfieFrameSaver.selfieCollected()}",
                )
                foundState
            }

            else -> {
                Timber.d(
                    "Didn't get a valid selfie in Found state after $stayInFoundDuration " +
                        "milliseconds, transition to Unsatisfied",
                )
                return IdentityScanState.Unsatisfied(
                    reason = "Didn't get a valid selfie in Found state after " +
                        "$stayInFoundDuration milliseconds",
                    type = foundState.type,
                    transitioner = foundState.transitioner,
                )
            }
        }
    }

    override suspend fun transitionFromSatisfied(
        satisfiedState: IdentityScanState.Satisfied,
        analyzerInput: AnalyzerInput,
        analyzerOutput: AnalyzerOutput,
    ): IdentityScanState =
        IdentityScanState.Finished(type = satisfiedState.type, transitioner = this)

    override suspend fun transitionFromUnsatisfied(
        unsatisfiedState: IdentityScanState.Unsatisfied,
        analyzerInput: AnalyzerInput,
        analyzerOutput: AnalyzerOutput,
    ): IdentityScanState = IdentityScanState.Initial(
        type = unsatisfiedState.type,
        transitioner = this.resetAndReturn(),
    )

    private fun isFaceValid(analyzerOutput: FaceDetectorOutput) =
        isMoreThanOneFace(analyzerOutput = analyzerOutput) &&
            isFaceCentered(boundingBox = analyzerOutput.faces.first().second) &&
            isFaceAwayFromEdges(boundingBox = analyzerOutput.faces.first().second) &&
            isFaceCoverageOK(boundingBox = analyzerOutput.faces.first().second) &&
            isFaceScoreOverThreshold(actualScore = analyzerOutput.resultScore)

    /**
     * Check if more than one face first
     */
    private fun isMoreThanOneFace(analyzerOutput: FaceDetectorOutput): Boolean =
        analyzerOutput.faces.size <= 1

    /**
     * Check face is centered by making sure center of face is
     * within corresponding threshold of center of image in both dimensions.
     */
    private fun isFaceCentered(boundingBox: Rect): Boolean =
        abs(1 - (boundingBox.top + boundingBox.top + boundingBox.height())) <
            maxCenteredThresholdY &&
            abs(1 - (boundingBox.left + boundingBox.left + boundingBox.width())) <
            maxCenteredThresholdX

    private fun isFaceAwayFromEdges(boundingBox: Rect): Boolean {
        minEdgeThreshold.let { edgeThreshold ->
            return boundingBox.top > edgeThreshold &&
                boundingBox.left > edgeThreshold &&
                (boundingBox.top + boundingBox.height()) < (1 - edgeThreshold) &&
                (boundingBox.left + boundingBox.width()) < (1 - edgeThreshold)
        }
    }

    /**
     * Check coverage is within range.
     *
     * coverage = (area of bounding box)/(area of input image)
     */
    private fun isFaceCoverageOK(boundingBox: Rect): Boolean {
        (boundingBox.width() * boundingBox.height()).let { coverage ->
            return coverage < maxCoverageThreshold &&
                coverage > minCoverageThreshold
        }
    }

    private fun isFaceScoreOverThreshold(actualScore: Float) = actualScore > faceDetectorMinScore

    internal enum class Selfie(val index: Int, val value: String) {
        FIRST(INDEX_FIRST, VALUE_FIRST),
        BEST(INDEX_BEST, VALUE_BEST),
        LAST(INDEX_LAST, VALUE_LAST),
    }

    internal companion object {
        const val SELFIES = "SELFIES"
        const val NUM_FILTERED_FRAMES = 3
        const val INDEX_FIRST = 0
        const val INDEX_BEST = 1
        const val INDEX_LAST = 2
        const val VALUE_FIRST = "first"
        const val VALUE_LAST = "last"
        const val VALUE_BEST = "best"
        const val DEFAULT_STAY_IN_FOUND_DURATION = 2000
    }
}
