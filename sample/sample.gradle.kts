plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.moshix)
    alias(libs.plugins.parcelize)
}

android {
    namespace = "com.smileidentity.sample"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.smileidentity.sample"
        minSdk = 21
        targetSdk = 34
        versionCode = findProperty("VERSION_CODE")?.toString()?.toInt() ?: 1
        // Include the SDK version in the app version name
        versionName = "1.6_" + project(":lib").version.toString()

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
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "_debug"
        }
        release {
            isMinifyEnabled = false
            isDebuggable = false
            if (uploadKeystoreFile.exists()) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
        compileOptions {
            // https://kotlinlang.org/docs/opt-in-requirements.html#module-wide-opt-in
            // This is to provide us a blanket-allow us to use APIs annotated with @SmileIDOptIn
            // without having to add the opt-in annotation to every usage. The annotation's purpose
            // is primarily for consumers of the SDK to use, not for us.
            freeCompilerArgs += "-opt-in=com.smileidentity.SmileIDOptIn"
        }
    }

    buildFeatures {
        buildConfig = true
    }

    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }

    lint {
        // Required for lib lints to be enforced
        checkDependencies = true
        enable += "ComposeM2Api"
        error += "ComposeM2Api"
    }
}

composeCompiler {
    enableStrongSkippingMode = true
    reportsDestination = layout.buildDirectory.dir("compose_compiler")
    metricsDestination = layout.buildDirectory.dir("compose_compiler")
}

val checkSmileConfigFileTaskName = "checkSmileConfigFile"
tasks.register(checkSmileConfigFileTaskName) {
    doLast {
        val configFile = file("src/main/assets/smile_config.json")
        // It is okay if the Smile Config doesn't exist -- it will be prompted for upon startup
        if (configFile.exists() && configFile.readText().isBlank()) {
            throw IllegalArgumentException("Empty smile_config.json file in src/main/assets!")
        }
    }
}

tasks.named("assemble") {
    dependsOn(checkSmileConfigFileTaskName)
}

dependencies {
    implementation(project(":lib"))
    implementation(libs.kotlin.immutable.collections)
    implementation(libs.androidx.core)
    implementation(libs.datastore)
    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.fragment)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.timber)

    // Debug Helpers
    debugImplementation(libs.chucker)
    releaseImplementation(libs.chucker.noop)

    debugImplementation(libs.leakcanary)
    releaseImplementation(libs.leakcanary.noop)

    // Utilities for Compose
    implementation(libs.androidx.lifecycle.runtime.compose)

    // Jetpack Compose version is defined by BOM ("Bill-of-Materials")
    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    lintChecks(libs.compose.lint.checks)

    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview) // Android Studio Preview support
    debugImplementation(libs.androidx.compose.ui.tooling) // Android Studio Preview support
    debugImplementation(libs.androidx.compose.ui.test.manifest) // UI Tests

    implementation(libs.androidx.navigation.compose)

    implementation(libs.mlkit.code.scanner)

    testImplementation(libs.junit)
    testImplementation(libs.coroutines.test)

    androidTestImplementation(composeBom)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4) // UI Tests
    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(libs.androidx.test.espresso)
}
