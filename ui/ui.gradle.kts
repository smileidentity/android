plugins {
    alias(libs.plugins.smileid.android.library)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.smileidentity.ui"

    lint {
        enable += "ComposeM2Api"
        error += "ComposeM2Api"
    }
}

composeCompiler {
    reportsDestination = layout.buildDirectory.dir("compose_compiler")
    metricsDestination = layout.buildDirectory.dir("compose_compiler")
}

dependencies {
    implementation(project(":camera"))
    implementation(project(":ml"))

    implementation(libs.androidx.core)

    // todo move the compose stuff to a bundle too, then apply like this
    // implementation(platform(libs.compose.bom))
    // implementation(libs.bundles.compose)
    // Jetpack Compose version is defined by BOM ("Bill-of-Materials")
    // Latest BOM version: https://developer.android.com/jetpack/compose/setup#bom-version-mapping
    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    // Jetpack Compose UI
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.text.google.fonts)
    implementation(libs.androidx.compose.animation)
    // Android Studio Preview support
    implementation(libs.androidx.compose.ui.tooling.preview)
    // Android Studio Preview support
    debugImplementation(libs.androidx.compose.ui.tooling)
    lintChecks(libs.compose.lint.checks)

    testImplementation(libs.junit)

    androidTestImplementation(composeBom)
    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(libs.androidx.test.espresso)
}
