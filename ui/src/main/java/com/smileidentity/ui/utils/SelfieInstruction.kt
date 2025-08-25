package com.smileidentity.ui.utils

enum class SelfieInstruction(
    val title: String,
    val description: String
) {
    GOOD_LIGHT(
        title = "Good Light",
        description = "Make sure you are in a well lit environment where your face is clear and visible."
    ),
    FACE_CAMERA(
        title = "Face Camera",
        description = "Keep your face centred and look straight into the camera."
    ),
    REMOVE_OBSTRUCTIONS(
        title = "Remove Obstructions",
        description = "Remove any unnecessary glasses, hats, or any items that may hide your face."
    )
}
