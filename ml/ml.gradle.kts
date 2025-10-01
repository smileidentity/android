plugins {
    alias(libs.plugins.smileid.android.library)
    alias(libs.plugins.smileid.android.library.jacoco)
    alias(libs.plugins.smileid.android.library.koin)
}

android {
    namespace = "com.smileidentity.ml"
}

dependencies {
    implementation(project(":camera"))

    implementation(libs.mediapipe)
    implementation(libs.mlkit.text.recognition)
    implementation(libs.litert)
    implementation(libs.litert.metadata)
    implementation(libs.litert.support)
    implementation(libs.litert.gpu)
}
