plugins {
    alias(libs.plugins.smileid.android.library)
    alias(libs.plugins.smileid.android.library.jacoco)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.smileidentity.navigation"

    buildFeatures {
        compose = true
    }
}

ksp {
    arg("compose-destinations.mode", "destinations")
    arg("compose-destinations.moduleName", "navigation")
    arg("compose-destinations.htmlMermaidGraph", "$rootDir//navigation-docs")
    arg("compose-destinations.mermaidGraph", "$rootDir/navigation-docs")
}

dependencies {
    implementation(project(":camera"))
    implementation(project(":ml"))
    implementation(project(":ui"))

    implementation(libs.destinations)
    implementation(libs.destinations.bottom.sheet)
    ksp(libs.destinations.ksp)

    val composeBom = platform(libs.androidx.compose.bom)
    androidTestImplementation(composeBom)
    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(libs.androidx.test.espresso)
    androidTestImplementation(libs.bundles.androidx.compose.ui.test)
}
