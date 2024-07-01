package com.smileidentity.models.v2

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

@JsonClass(generateAdapter = true)
@Parcelize
data class ActiveLivenessDetails(
    val orderedFaceDirections: List<FaceDirection>,
    val forceFailure: Boolean? = null,
) : Parcelable

enum class FaceDirection {
    Left,
    Right,
    Up,
}
