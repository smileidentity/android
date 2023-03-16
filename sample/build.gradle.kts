@file:Suppress("UnstableApiUsage")

// TODO: KTIJ-19369: should be fixed with Gradle 8.1
//  https://github.com/gradle/gradle/issues/22797
//  https://youtrack.jetbrains.com/issue/KTIJ-19369
@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ktlint)
}

android {
    namespace = "com.smileidentity.sample"
    compileSdk = 33

    defaultConfig {
        applicationId = "com.smileidentity.sample"
        minSdk = 21
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
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

dependencies {
    implementation(project(":ui"))
    implementation(libs.androidx.core)
    implementation(libs.androidx.appcompat)
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
