@file:Suppress("UnstableApiUsage")

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ktlint)
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
        kotlinCompilerExtensionVersion = libs.versions.androidx.compose.compiler.get()
    }
}

dependencies {
    api(project(":networking"))
    implementation(libs.androidx.core)

    // Logging
    implementation(libs.timber)

    // Sentry (crash reporting, tracing, breadcrumbs)
    implementation(platform(libs.sentry.bom))
    implementation(libs.sentry)
    // TODO: Integrate these more directly so that we get automatic breadcrumbs
    // implementation(libs.sentry.android.core")
    // implementation(libs.sentry.compose.android")
    // implementation(libs.sentry.android.fragment")
    // implementation(libs.sentry.android.timber")
    // implementation(libs.sentry.android.okhttp")

    // ViewModel and utilities for Compose
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Jetpack Compose version is defined by BOM ("Bill-of-Materials")
    // Latest BOM version: https://developer.android.com/jetpack/compose/setup#bom-version-mapping
    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    androidTestImplementation(composeBom)

    // Material Design 3 components
    implementation(libs.androidx.compose.material3)
    // Jetpack Compose UI
    implementation(libs.androidx.compose.ui)
    // Android Studio Preview support
    implementation(libs.androidx.compose.ui.tooling.preview)
    // Android Studio Preview support
    debugImplementation(libs.androidx.compose.ui.tooling)
    // Test rules
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    // UI Tests (Needed for createAndroidComposeRule, but not createComposeRule)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    // Permissions Compose component
    implementation(libs.accompanist.permissions)
    // CameraX Compose component
    implementation(libs.camposer)

    // Unbundled model -- will be dynamically downloaded via Google Play Services
    implementation(libs.play.services.mlkit.face.detection)

    testImplementation(libs.junit)
    testImplementation(libs.coroutines.test)

    testImplementation(libs.mockk)
    androidTestImplementation(libs.mockk.android)

    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.androidx.test.rules)
    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(libs.androidx.test.espresso)
    androidTestImplementation(libs.androidx.test.fragment)
}
