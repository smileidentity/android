// TODO: KTIJ-19369: should be fixed with Gradle 8.1
//  https://github.com/gradle/gradle/issues/22797
//  https://youtrack.jetbrains.com/issue/KTIJ-19369
@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    // Applied to all sub-modules
    alias(libs.plugins.ktlint)

    // Applied depending on sub-module
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.ksp) apply false
}

tasks.create("clean", Delete::class.java) {
    delete(rootProject.buildDir)
}

ktlint {
    android.set(true)
}
