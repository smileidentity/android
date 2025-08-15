plugins {
    alias(libs.plugins.smileid.android.library)
}

android {
    namespace = "com.smileidentity.attestation"
}

dependencies {
    implementation(libs.play.integrity)
    implementation(libs.coroutines.core)
    implementation(libs.smileid.security)
    testImplementation(libs.mockk)
}
