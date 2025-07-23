plugins {
    alias(libs.plugins.smileid.android.library)
}

android {
    namespace = "com.smileidentity.ml"
}

dependencies {
    implementation(project(":camera"))
}
