plugins {
    alias(libs.plugins.smileid.android.library)
    alias(libs.plugins.smileid.android.library.jacoco)
}

android {
    namespace = "com.smileidentity.ml"
}

dependencies {
    implementation(project(":camera"))

    // ViewModel and utilities for Compose
    api(libs.androidx.lifecycle.viewmodel.compose)
    api(libs.androidx.lifecycle.runtime.compose)

    implementation(libs.mediapipe)
    implementation(libs.litert)
    implementation(libs.litert.metadata)
    implementation(libs.litert.support)
    implementation(libs.litert.gpu)
}
