package com.smileidentity.viewmodel

import com.google.mlkit.vision.face.Face
import com.smileidentity.util.isLookingLeft
import com.smileidentity.util.isLookingRight
import com.smileidentity.util.isLookingUp
import com.smileidentity.viewmodel.SelfieHint.LookLeft
import com.smileidentity.viewmodel.SelfieHint.LookRight
import com.smileidentity.viewmodel.SelfieHint.LookUp

private const val ORTHOGONAL_ANGLE_BUFFER = Float.MAX_VALUE
private const val MIDWAY_ANGLE_MIN = 10f
private const val MIDWAY_ANGLE_MAX = Float.MAX_VALUE
private const val END_ANGLE_MIN = 30f
private const val END_ANGLE_MAX = Float.MAX_VALUE

/**
 * Determines a randomized set of directions for the user to look in
 * We capture two types of tasks: midpoint and end.
 */
internal class ActiveLivenessTask(
    private val endpointStabilityTimeMs: Long,
    shouldCaptureMidTrack: Boolean = true,
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

    // private val orderedFaceDirections = FaceDirection.entries.shuffled()
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
            is LeftMid -> face.isLookingLeft(
                minAngle = MIDWAY_ANGLE_MIN,
                maxAngle = MIDWAY_ANGLE_MAX,
                verticalAngleBuffer = ORTHOGONAL_ANGLE_BUFFER,
            )

            is LeftEnd -> face.isLookingLeft(
                minAngle = END_ANGLE_MIN,
                maxAngle = END_ANGLE_MAX,
                verticalAngleBuffer = ORTHOGONAL_ANGLE_BUFFER,
            )

            is RightMid -> face.isLookingRight(
                minAngle = MIDWAY_ANGLE_MIN,
                maxAngle = MIDWAY_ANGLE_MAX,
                verticalAngleBuffer = ORTHOGONAL_ANGLE_BUFFER,
            )

            is RightEnd -> face.isLookingRight(
                minAngle = END_ANGLE_MIN,
                maxAngle = END_ANGLE_MAX,
                verticalAngleBuffer = ORTHOGONAL_ANGLE_BUFFER,
            )

            is UpMid -> face.isLookingUp(
                minAngle = MIDWAY_ANGLE_MIN,
                maxAngle = MIDWAY_ANGLE_MAX,
                horizontalAngleBuffer = ORTHOGONAL_ANGLE_BUFFER,
            )

            is UpEnd -> face.isLookingUp(
                minAngle = END_ANGLE_MIN,
                maxAngle = END_ANGLE_MAX,
                horizontalAngleBuffer = ORTHOGONAL_ANGLE_BUFFER,
            )
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
        val hasBeenLongEnough = elapsedTimeMs > endpointStabilityTimeMs
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
