plugins {
    `kotlin-dsl`
}

group = "com.smileidentity"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17

    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17)) // Specify the Java version
    }
}

kotlin {
    jvmToolchain(17) // Align Kotlin's JVM target with Java 17
}

dependencies {
    implementation(libs.androidx.room.gradle.plugin)
    compileOnly(libs.android.gradle.tools.plugin)
    compileOnly(libs.kotlin.gradle.plugin)
    compileOnly(libs.ksp.gradle.plugin)
}

tasks {
    validatePlugins {
        enableStricterValidation = true
        failOnWarning = true
    }
}

gradlePlugin {
    plugins {
        create("smileidAppPlugin") {
            id = "smileid.android.app"
            implementationClass = "com.smileidentity.SmileIDAppPlugin"
            description = "Plugin utilized for a SmileID Android App module"
        }
    }
}
