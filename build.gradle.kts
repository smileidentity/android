plugins {
    // Applied to all sub-modules
    id("org.jlleitschuh.gradle.ktlint") version "11.2.0"

    // Applied depending on sub-module
    id("org.jetbrains.kotlin.jvm") version "1.8.10" apply false
    id("com.android.library") version "7.4.1" apply false
    id("com.android.application") version "7.4.1" apply false
    id("org.jetbrains.kotlin.android") version "1.8.10" apply false
    id("com.google.devtools.ksp") version "1.8.10-1.0.9" apply false
}

tasks.create("clean", Delete::class.java) {
    delete(rootProject.buildDir)
}

ktlint {
    android.set(true)
}

project.gradle.startParameter.excludedTaskNames.add("ktlintGeneratedByKspKotlinSourceSetCheck")
