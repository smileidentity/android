plugins {
    // Applied to all sub-modules
    alias(libs.plugins.ktlint)

    // Applied depending on sub-module
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.maven.publish) apply false
    alias(libs.plugins.moshix) apply false
    alias(libs.plugins.parcelize) apply false
}

tasks.create("clean", Delete::class.java) {
    delete(rootProject.layout.buildDirectory)
}

ktlint {
    android = true
}
