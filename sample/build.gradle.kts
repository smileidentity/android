// TODO: Remove DSL_SCOPE_VIOLATION on release of Gradle 8.1
//  https://github.com/gradle/gradle/issues/22797
@file:Suppress("UnstableApiUsage", "DSL_SCOPE_VIOLATION")

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.parcelize)
}

android {
    namespace = "com.smileidentity.sample"
    compileSdk = 33

    defaultConfig {
        applicationId = "com.smileidentity.sample"
        minSdk = 21
        targetSdk = 33
        versionCode = 1
        // Include the SDK version in the app version name
        versionName = "1.0.0_sdk-" + project(":lib").version.toString()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    val uploadKeystoreFile = file("upload.jks")
    signingConfigs {
        if (uploadKeystoreFile.exists()) {
            create("release") {
                val uploadKeystorePassword = findProperty("uploadKeystorePassword") as? String
                storeFile = file("upload.jks")
                keyAlias = "upload"
                storePassword = uploadKeystorePassword
                keyPassword = uploadKeystorePassword
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            isDebuggable = false
            if (uploadKeystoreFile.exists()) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.androidx.compose.compiler.get()
    }

    packagingOptions {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
}

val checkSmileConfigFileTaskName = "checkSmileConfigFile"
tasks.register(checkSmileConfigFileTaskName) {
    doLast {
        val configFile = file("src/main/assets/smile_config.json")
        if (!configFile.exists()) {
            throw IllegalArgumentException("Missing smile_config.json file in src/main/assets!")
        }
    }
}

tasks.named("assemble") {
    dependsOn(checkSmileConfigFileTaskName)
}

dependencies {
    implementation(project(":lib"))
    implementation(libs.androidx.core)
    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.androidx.activity.compose)
    implementation(libs.timber)

    // Debug Helpers
    debugImplementation(libs.chucker)
    releaseImplementation(libs.chucker.noop)

    debugImplementation(libs.leakcanary)
    releaseImplementation(libs.leakcanary.noop)

    // Jetpack Compose version is defined by BOM ("Bill-of-Materials")
    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)

    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview) // Android Studio Preview support
    debugImplementation(libs.androidx.compose.ui.tooling) // Android Studio Preview support
    debugImplementation(libs.androidx.compose.ui.test.manifest) // UI Tests

    implementation(libs.androidx.navigation.compose)
    implementation(libs.material.components)

    testImplementation(libs.junit)
    testImplementation(libs.coroutines.test)

    androidTestImplementation(composeBom)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4) // UI Tests
    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(libs.androidx.test.espresso)
}
