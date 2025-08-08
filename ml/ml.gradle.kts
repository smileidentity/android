plugins {
    alias(libs.plugins.smileid.android.library)
    alias(libs.plugins.smileid.android.library.jacoco)
}

android {
    namespace = "com.smileidentity.ml"
}

dependencies {
    implementation(project(":camera"))
}
