package com.smileidentity.models.v2.metadata

enum class LivenessType(val value: String) {
    HeadPose("head_pose"),
    Smile("smile"),
}

enum class DocumentImageOriginValue(val value: String) {
    Gallery("gallery"),
    CameraAutoCapture("camera_auto_capture"),
    CameraManualCapture("camera_manual_capture"),
}

enum class SelfieImageOriginValue(val value: String) {
    FrontCamera("front_camera"),
    BackCamera("back_camera"),
}
