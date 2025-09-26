plugins {
    alias(libs.plugins.smileid.android.library)
    alias(libs.plugins.smileid.android.library.koin)
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
