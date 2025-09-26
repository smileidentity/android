plugins {
    alias(libs.plugins.smileid.android.library)
    alias(libs.plugins.smileid.android.library.jacoco)
    alias(libs.plugins.smileid.android.library.koin)
}

android {
    namespace = "com.smileidentity.storage"
}
