package com.smileidentity.viewmodel

import com.google.mlkit.vision.face.Face
import com.smileidentity.util.isLookingLeft
import com.smileidentity.util.isLookingRight
import com.smileidentity.util.isLookingUp
import com.smileidentity.viewmodel.SelfieHint.LookLeft
import com.smileidentity.viewmodel.SelfieHint.LookRight
import com.smileidentity.viewmodel.SelfieHint.LookUp

private const val END_LR_ANGLE_MAX = 90f
private const val END_LR_ANGLE_MIN = 27f
private const val END_UP_ANGLE_MAX = 90f
private const val END_UP_ANGLE_MIN = 17f
private const val LIVENESS_STABILITY_TIME_MS = 150L
private const val MIDWAY_LR_ANGLE_MAX = 90f
private const val MIDWAY_LR_ANGLE_MIN = 9f
private const val MIDWAY_UP_ANGLE_MAX = 90f
private const val MIDWAY_UP_ANGLE_MIN = 7f
private const val ORTHOGONAL_ANGLE_BUFFER = 90f
private const val PROGRESS_INCREMENT = 0.5f
private const val FAILURE_THRESHOLD = 3

/**
 * Determines a randomized set of directions for the user to look in
 * We capture two types of tasks: midpoint and end.
 */
internal class ActiveLivenessTask(
    shouldCaptureMidTrack: Boolean = true,
    private val updateProgress: (Float, Float, Float) -> Unit,
) {
    private sealed interface FaceDirection {
        fun getProgress(task: ActiveLivenessTask): Float
        fun updateProgress(task: ActiveLivenessTask)
        fun checkFaceAngle(face: Face): Boolean
    }

    private sealed interface Left : FaceDirection {
        override fun getProgress(task: ActiveLivenessTask) = task.leftProgress
        override fun updateProgress(task: ActiveLivenessTask) =
            task.updateProgress(task.leftProgress, task.rightProgress, task.topProgress)
    }

    private sealed interface Right : FaceDirection {
        override fun getProgress(task: ActiveLivenessTask) = task.rightProgress
        override fun updateProgress(task: ActiveLivenessTask) =
            task.updateProgress(task.leftProgress, task.rightProgress, task.topProgress)
    }

    private sealed interface Up : FaceDirection {
        override fun getProgress(task: ActiveLivenessTask) = task.topProgress
        override fun updateProgress(task: ActiveLivenessTask) =
            task.updateProgress(task.leftProgress, task.rightProgress, task.topProgress)
    }

    private sealed interface Midpoint : FaceDirection
    private sealed class Endpoint(val midpoint: Midpoint) : FaceDirection

    private data object LeftEnd : Left, Endpoint(LeftMid) {
        override fun checkFaceAngle(face: Face) = face.isLookingLeft(
            minAngle = END_LR_ANGLE_MIN,
            maxAngle = END_LR_ANGLE_MAX,
            verticalAngleBuffer = ORTHOGONAL_ANGLE_BUFFER,
        )
    }

    private data object LeftMid : Left, Midpoint {
        override fun checkFaceAngle(face: Face) = face.isLookingLeft(
            minAngle = MIDWAY_LR_ANGLE_MIN,
            maxAngle = MIDWAY_LR_ANGLE_MAX,
            verticalAngleBuffer = ORTHOGONAL_ANGLE_BUFFER,
        )
    }

    private data object RightEnd : Right, Endpoint(RightMid) {
        override fun checkFaceAngle(face: Face) = face.isLookingRight(
            minAngle = END_LR_ANGLE_MIN,
            maxAngle = END_LR_ANGLE_MAX,
            verticalAngleBuffer = ORTHOGONAL_ANGLE_BUFFER,
        )
    }

    private data object RightMid : Right, Midpoint {
        override fun checkFaceAngle(face: Face) = face.isLookingRight(
            minAngle = MIDWAY_LR_ANGLE_MIN,
            maxAngle = MIDWAY_LR_ANGLE_MAX,
            verticalAngleBuffer = ORTHOGONAL_ANGLE_BUFFER,
        )
    }

    private data object UpEnd : Up, Endpoint(UpMid) {
        override fun checkFaceAngle(face: Face) = face.isLookingUp(
            minAngle = END_UP_ANGLE_MIN,
            maxAngle = END_UP_ANGLE_MAX,
            horizontalAngleBuffer = ORTHOGONAL_ANGLE_BUFFER,
        )
    }

    private data object UpMid : Up, Midpoint {
        override fun checkFaceAngle(face: Face) = face.isLookingUp(
            minAngle = MIDWAY_UP_ANGLE_MIN,
            maxAngle = MIDWAY_UP_ANGLE_MAX,
            horizontalAngleBuffer = ORTHOGONAL_ANGLE_BUFFER,
        )
    }

    private var leftProgress = 0f
    private var rightProgress = 0f
    private var topProgress = 0f
    private var consecutiveFailedFrames = 0
    private var currentDirectionIdx = 0
    private var currentDirectionInitiallySatisfiedAt = Long.MAX_VALUE

    private val orderedFaceDirections = listOf(LeftEnd, RightEnd, UpEnd)
        .shuffled()
        .flatMap {
            if (shouldCaptureMidTrack) {
                listOf(it, it.midpoint)
            } else {
                listOf(it)
            }
        }

    /**
     * Determines if conditions are met for the current active liveness task
     *
     * For midpoint images, we capture eagerly. For end images, we only capture if the user has been
     * looking in the correct direction for a certain amount of time. This is to prevent blurriness.
     * We don't do the same for midpoint images because we don't want to interrupt the user mid-turn
     *
     * @param face The face detected in the image
     */
    fun doesFaceMeetCurrentActiveLivenessTask(face: Face): Boolean {
        val currentDirection = orderedFaceDirections[currentDirectionIdx]
        val isCorrect = currentDirection.checkFaceAngle(face)

        updateProgressForDirection(currentDirection, isCorrect)

        if (!isCorrect) {
            resetLivenessStabilityTime()
            return false
        }

        return if (currentDirection is Midpoint) {
            true
        } else {
            checkEndpointStability()
        }
    }

    private fun updateProgressForDirection(direction: FaceDirection, isCorrect: Boolean) {
        if (isCorrect) {
            consecutiveFailedFrames = 0
            when (direction) {
                is Left -> leftProgress = minOf(1f, leftProgress + PROGRESS_INCREMENT)
                is Right -> rightProgress = minOf(1f, rightProgress + PROGRESS_INCREMENT)
                is Up -> topProgress = minOf(1f, topProgress + PROGRESS_INCREMENT)
            }
        } else {
            consecutiveFailedFrames++
            if (consecutiveFailedFrames >= FAILURE_THRESHOLD) {
                when (direction) {
                    is Left -> leftProgress = 0f
                    is Right -> rightProgress = 0f
                    is Up -> topProgress = 0f
                }
                consecutiveFailedFrames = 0
            }
        }
        direction.updateProgress(task = this)
    }

    private fun checkEndpointStability(): Boolean {
        if (currentDirectionInitiallySatisfiedAt > System.currentTimeMillis()) {
            currentDirectionInitiallySatisfiedAt = System.currentTimeMillis()
        }
        val elapsedTimeMs = System.currentTimeMillis() - currentDirectionInitiallySatisfiedAt
        val hasBeenLongEnough = elapsedTimeMs > LIVENESS_STABILITY_TIME_MS
        if (hasBeenLongEnough) {
            resetLivenessStabilityTime()
        }
        return hasBeenLongEnough
    }

    /**
     * Marks the current direction as satisfied and moves to the next direction.
     *
     * @return true if there are more directions to satisfy, false otherwise
     */
    fun markCurrentDirectionSatisfied(): Boolean {
        when (orderedFaceDirections[currentDirectionIdx]) {
            is Left -> leftProgress = 0f
            is Right -> rightProgress = 0f
            is Up -> topProgress = 0f
        }
        currentDirectionIdx++
        return currentDirectionIdx < orderedFaceDirections.size
    }

    val isFinished
        get() = currentDirectionIdx >= orderedFaceDirections.size

    /**
     * Resets the current direction and the time it was initially satisfied.
     */
    fun restart() {
        currentDirectionIdx = 0
        consecutiveFailedFrames = 0
        updateProgress(0f, 0f, 0f)
        resetLivenessStabilityTime()
    }

    /**
     * Converts the current direction to a selfie hint.
     */
    val selfieHint
        get() = when (orderedFaceDirections[currentDirectionIdx]) {
            is Left -> LookLeft
            is Right -> LookRight
            is Up -> LookUp
        }

    /**
     * Resets the time the current direction was initially satisfied.
     */
    private fun resetLivenessStabilityTime() {
        currentDirectionInitiallySatisfiedAt = Long.MAX_VALUE
    }
}
