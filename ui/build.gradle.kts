@file:Suppress("UnstableApiUsage")

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("org.jlleitschuh.gradle.ktlint")
}

android {
    namespace = "com.smileidentity.ui"
    resourcePrefix = "si_"
    compileSdk = 33

    defaultConfig {
        minSdk = 21

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        all {
            val sentryDsn = findProperty("SENTRY_DSN") ?: ""
            buildConfigField("String", "SENTRY_DSN", "\"$sentryDsn\"")
            buildConfigField("String", "VERSION_NAME", "\"${project.version}\"")
        }

        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    packagingOptions {
        resources.excludes += "META-INF/*"
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
}

dependencies {
    api(project(":networking"))
    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1")

    // Logging
    implementation("com.jakewharton.timber:timber:5.0.1")

    // Sentry (crash reporting, tracing, breadcrumbs)
    implementation(platform("io.sentry:sentry-bom:6.13.0"))
    implementation("io.sentry:sentry")
    // TODO: Integrate these more directly so that we get automatic breadcrumbs
    // implementation("io.sentry:sentry-android-core")
    // implementation("io.sentry:sentry-compose-android")
    // implementation("io.sentry:sentry-android-fragment")
    // implementation("io.sentry:sentry-android-timber")
    // implementation("io.sentry:sentry-android-okhttp")

    // ViewModel and utilities for Compose
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.5.1")

    // Jetpack Compose version is defined by BOM ("Bill-of-Materials")
    // Latest BOM version: https://developer.android.com/jetpack/compose/setup#bom-version-mapping
    val composeBom = platform("androidx.compose:compose-bom:2023.01.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    // Material Design 3 components
    implementation("androidx.compose.material3:material3")
    // Jetpack Compose UI
    implementation("androidx.compose.ui:ui")
    // Android Studio Preview support
    implementation("androidx.compose.ui:ui-tooling-preview")
    // Android Studio Preview support
    debugImplementation("androidx.compose.ui:ui-tooling")
    // Test rules
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    // UI Tests (Needed for createAndroidComposeRule, but not createComposeRule)
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    // Permissions Compose component
    implementation("com.google.accompanist:accompanist-permissions:0.28.0")
    // CameraX Compose component
    implementation("io.github.ujizin:camposer:0.1.1")
    implementation("androidx.camera:camera-camera2")

    // Unbundled model -- will be dynamically downloaded via Google Play Services
    implementation("com.google.android.gms:play-services-mlkit-face-detection:17.1.0")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4")

    val mockkVersion = "1.13.3"
    testImplementation("io.mockk:mockk:$mockkVersion")
    androidTestImplementation("io.mockk:mockk-android:$mockkVersion")

    androidTestImplementation("androidx.test:core-ktx:1.5.0")
    androidTestImplementation("androidx.test:rules:1.5.0")
    androidTestImplementation("androidx.test.ext:junit-ktx:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.fragment:fragment-testing:1.5.5")
}
