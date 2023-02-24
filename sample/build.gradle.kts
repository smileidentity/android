@file:Suppress("UnstableApiUsage")

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jlleitschuh.gradle.ktlint")
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
        kotlinCompilerExtensionVersion = "1.4.2"
    }

    packagingOptions {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
}

dependencies {
    implementation(project(":ui"))
    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.5.1")
    implementation("androidx.activity:activity-compose:1.6.1")
    implementation("com.google.android.material:material:1.8.0")
    implementation("com.jakewharton.timber:timber:5.0.1")

    // Debug Helpers
    val chuckVersion = "3.5.2"
    debugImplementation("com.github.chuckerteam.chucker:library:$chuckVersion")
    releaseImplementation("com.github.chuckerteam.chucker:library-no-op:$chuckVersion")

    val leakCanaryVersion = "2.10"
    debugImplementation("com.squareup.leakcanary:leakcanary-android:$leakCanaryVersion")
    releaseImplementation("com.squareup.leakcanary:leakcanary-android-release:$leakCanaryVersion")

    // Jetpack Compose version is defined by BOM ("Bill-of-Materials")
    // Latest BOM version: https://developer.android.com/jetpack/compose/setup#bom-version-mapping
    val composeBom = platform("androidx.compose:compose-bom:2023.01.00")
    implementation(composeBom)

    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview") // Android Studio Preview support
    debugImplementation("androidx.compose.ui:ui-tooling") // Android Studio Preview support
    debugImplementation("androidx.compose.ui:ui-test-manifest") // UI Tests

    implementation("androidx.navigation:navigation-compose:2.5.3")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4")

    androidTestImplementation(composeBom)
    androidTestImplementation("androidx.compose.ui:ui-test-junit4") // UI Tests
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
