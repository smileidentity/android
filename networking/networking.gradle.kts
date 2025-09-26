plugins {
    alias(libs.plugins.smileid.android.library)
    alias(libs.plugins.smileid.android.library.jacoco)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.smileid.android.library.koin)
}

android {
    namespace = "com.smileidentity.networking"
}

dependencies {
    // OkHttp is exposed in public SmileID interface (initialize), hence "api" vs "implementation"
    api(libs.okhttp)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.scalars)
    implementation(libs.retrofit.converter.kotlinx.serialization)
    implementation(libs.okhttp.logging.interceptor)

    implementation(libs.kotlinx.serialization.json)

    testImplementation(libs.okhttp.mockwebserver)
    testImplementation(libs.mockk)
}
