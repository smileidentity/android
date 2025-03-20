package com.smileidentity.models.v2.metadata

enum class MetadataKey(val key: String) {
    ActiveLivenessType("active_liveness_type"),
    ActiveLivenessVersion("active_liveness_version"),
    CameraName("camera_name"),
    ClientIP("client_ip"),
    DeviceModel("device_model"),
    DeviceOS("device_os"),
    DocumentBackCaptureRetries("document_back_capture_retries"),
    DocumentBackCaptureDuration("document_back_capture_duration_ms"),
    DocumentBackImageOrigin("document_back_image_origin"),
    DocumentFrontCaptureRetries("document_front_capture_retries"),
    DocumentFrontCaptureDuration("document_front_capture_duration_ms"),
    DocumentFrontImageOrigin("document_front_image_origin"),
    Fingerprint("fingerprint"),
    NetworkConnection("network_connection"),
    SelfieCaptureDuration("selfie_capture_duration_ms"),
    SelfieImageOrigin("selfie_image_origin"),
    Sdk("sdk"),
    SdkVersion("sdk_version"),
}
