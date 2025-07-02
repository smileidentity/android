package com.smileidentity.util

import com.google.mlkit.vision.face.Face
import kotlin.math.abs

/**
 * Determines whether the face is looking left, within some thresholds
 *
 * @param minAngle The minimum angle the face should be looking left
 * @param maxAngle The maximum angle the face should be looking left
 * @param verticalAngleBuffer The buffer for the vertical angle
 */
internal fun Face.isLookingLeft(
    minAngle: Float,
    maxAngle: Float,
    verticalAngleBuffer: Float,
): Boolean = headEulerAngleY in minAngle..maxAngle && abs(headEulerAngleX) < verticalAngleBuffer

/**
 * Determines whether the face is looking right, within some thresholds
 *
 * @param minAngle The minimum angle the face should be looking right
 * @param maxAngle The maximum angle the face should be looking right
 * @param verticalAngleBuffer The buffer for the vertical angle
 */
internal fun Face.isLookingRight(
    minAngle: Float,
    maxAngle: Float,
    verticalAngleBuffer: Float,
): Boolean = headEulerAngleY in -maxAngle..-minAngle && abs(headEulerAngleX) < verticalAngleBuffer

/**
 * Determines whether the face is looking up, within some thresholds
 *
 * @param minAngle The minimum angle the face should be looking up
 * @param maxAngle The maximum angle the face should be looking up
 * @param horizontalAngleBuffer The buffer for the horizontal angle
 */
internal fun Face.isLookingUp(
    minAngle: Float,
    maxAngle: Float,
    horizontalAngleBuffer: Float,
): Boolean = headEulerAngleX in minAngle..maxAngle && abs(headEulerAngleY) < horizontalAngleBuffer
