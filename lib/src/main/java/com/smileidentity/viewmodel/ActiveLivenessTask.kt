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

/**
 * Determines a randomized set of directions for the user to look in
 * We capture two types of tasks: midpoint and end.
 */
internal class ActiveLivenessTask(
    shouldCaptureMidTrack: Boolean = true,
    private val updateProgress: (Float, Float, Float) -> Unit,
) {
    private sealed interface FaceDirection
    private sealed interface Left : FaceDirection
    private sealed interface Right : FaceDirection
    private sealed interface Up : FaceDirection
    private sealed interface Midpoint : FaceDirection
    private sealed class Endpoint(val midpoint: Midpoint) : FaceDirection
    private data object LeftEnd : Left, Endpoint(LeftMid)
    private data object LeftMid : Left, Midpoint
    private data object RightEnd : Right, Endpoint(RightMid)
    private data object RightMid : Right, Midpoint
    private data object UpEnd : Up, Endpoint(UpMid)
    private data object UpMid : Up, Midpoint

    private var leftProgress = 0F
    private var rightProgress = 0F
    private var topProgress = 0F

    private val orderedFaceDirections = listOf(LeftEnd, RightEnd, UpEnd)
        .shuffled()
        .flatMap {
            if (shouldCaptureMidTrack) {
                listOf(it, it.midpoint)
            } else {
                listOf(it)
            }
        }
    private var currentDirectionIdx = 0
    private var currentDirectionInitiallySatisfiedAt = Long.MAX_VALUE

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
        val isLookingRightDirection = when (orderedFaceDirections[currentDirectionIdx]) {
            is LeftMid -> {
                val isCorrect = face.isLookingLeft(
                    minAngle = MIDWAY_LR_ANGLE_MIN,
                    maxAngle = MIDWAY_LR_ANGLE_MAX,
                    verticalAngleBuffer = ORTHOGONAL_ANGLE_BUFFER,
                )
                if (isCorrect) {
                    leftProgress = minOf(1F, leftProgress + 0.5F)
                    updateProgress(leftProgress, 0F, 0F)
                } else {
                    leftProgress = 0F
                    updateProgress(leftProgress, 0F, 0F)
                }
                isCorrect
            }

            is LeftEnd -> {
                val isCorrect = face.isLookingLeft(
                    minAngle = END_LR_ANGLE_MIN,
                    maxAngle = END_LR_ANGLE_MAX,
                    verticalAngleBuffer = ORTHOGONAL_ANGLE_BUFFER,
                )
                if (isCorrect) {
                    leftProgress = minOf(1F, leftProgress + 0.5F)
                    updateProgress(leftProgress, 0F, 0F)
                } else {
                    leftProgress = 0F
                    updateProgress(leftProgress, 0F, 0F)
                }
                isCorrect
            }

            is RightMid -> {
                val isCorrect = face.isLookingRight(
                    minAngle = MIDWAY_LR_ANGLE_MIN,
                    maxAngle = MIDWAY_LR_ANGLE_MAX,
                    verticalAngleBuffer = ORTHOGONAL_ANGLE_BUFFER,
                )
                if (isCorrect) {
                    rightProgress = minOf(1F, rightProgress + 0.5F)
                    updateProgress(0F, rightProgress, 0F)
                } else {
                    rightProgress = 0F
                    updateProgress(0F, rightProgress, 0F)
                }
                isCorrect
            }

            is RightEnd -> {
                val isCorrect = face.isLookingRight(
                    minAngle = END_LR_ANGLE_MIN,
                    maxAngle = END_LR_ANGLE_MAX,
                    verticalAngleBuffer = ORTHOGONAL_ANGLE_BUFFER,
                )
                if (isCorrect) {
                    rightProgress = minOf(1F, rightProgress + 0.5F)
                    updateProgress(0F, rightProgress, 0F)
                } else {
                    rightProgress = 0F
                    updateProgress(0F, rightProgress, 0F)
                }
                isCorrect
            }

            is UpMid -> {
                val isCorrect = face.isLookingUp(
                    minAngle = MIDWAY_UP_ANGLE_MIN,
                    maxAngle = MIDWAY_UP_ANGLE_MAX,
                    horizontalAngleBuffer = ORTHOGONAL_ANGLE_BUFFER,
                )
                if (isCorrect) {
                    topProgress = minOf(1F, topProgress + 0.5F)
                    updateProgress(0F, 0F, topProgress)
                } else {
                    topProgress = 0F
                    updateProgress(0F, 0F, topProgress)
                }
                isCorrect
            }

            is UpEnd -> {
                val isCorrect = face.isLookingUp(
                    minAngle = END_UP_ANGLE_MIN,
                    maxAngle = END_UP_ANGLE_MAX,
                    horizontalAngleBuffer = ORTHOGONAL_ANGLE_BUFFER,
                )
                if (isCorrect) {
                    topProgress = minOf(1F, topProgress + 0.5F)
                    updateProgress(0F, 0F, topProgress)
                } else {
                    topProgress = 0F
                    updateProgress(0F, 0F, topProgress)
                }
                isCorrect
            }
        }
        if (!isLookingRightDirection) {
            resetLivenessStabilityTime()
            return false
        }
        if (orderedFaceDirections[currentDirectionIdx] is Midpoint) {
            return true
        }
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
        currentDirectionIdx += 1
        return currentDirectionIdx < orderedFaceDirections.size
    }

    val isFinished
        get() = currentDirectionIdx >= orderedFaceDirections.size

    /**
     * Resets the current direction and the time it was initially satisfied.
     */
    fun restart() {
        currentDirectionIdx = 0
        updateProgress(0F, 0F, 0F)
        resetLivenessStabilityTime()
    }

    /**
     * Converts the current direction to a selfie hint.
     */
    val selfieHint
        get() = when (orderedFaceDirections[currentDirectionIdx]) {
            is LeftMid -> LookLeft
            is LeftEnd -> LookLeft
            is RightMid -> LookRight
            is RightEnd -> LookRight
            is UpMid -> LookUp
            is UpEnd -> LookUp
        }

    /**
     * Resets the time the current direction was initially satisfied.
     */
    private fun resetLivenessStabilityTime() {
        currentDirectionInitiallySatisfiedAt = Long.MAX_VALUE
    }
}
