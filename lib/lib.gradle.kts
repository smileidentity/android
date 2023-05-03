@file:Suppress("UnstableApiUsage")

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.maven.publish)
    alias(libs.plugins.moshix)
    alias(libs.plugins.parcelize)
}

val groupId = "com.smileidentity"
val artifactId = "android-sdk"
project.version = findProperty("VERSION_NAME") as? String ?: file("VERSION").readText().trim()

android {
    namespace = groupId
    resourcePrefix = "si_"
    compileSdk = 33

    defaultConfig {
        minSdk = 21

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        all {
            val sentryDsn = findProperty("SENTRY_DSN")
                ?: throw IllegalArgumentException("Please set the SENTRY_DSN gradle property")
            buildConfigField("String", "SENTRY_DSN", "\"$sentryDsn\"")
            buildConfigField("String", "VERSION_NAME", "\"${version}\"")
        }

        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android.txt"),
                "proguard-rules.pro",
            )
        }
    }

    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1,LICENSE*}"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
        moduleName = "${groupId}_$artifactId"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.androidx.compose.compiler.get()
    }
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()
    coordinates(groupId, artifactId, project.version.toString())
    pom {
        name.set("Smile ID Android SDK")
        description.set("The Official Smile ID Android SDK")
        url.set("https://docs.smileidentity.com/mobile/android")
        licenses {
            license {
                name.set("Smile ID Terms of Use")
                url.set("https://smileidentity.com/terms-and-conditions")
                distribution.set("repo")
            }
        }
        scm {
            url.set("https://github.com/smileidentity/android")
            connection.set("scm:git:git://github.com/smileidentity/android.git")
            developerConnection.set("scm:git:ssh://github.com/smileidentity/android.git")
        }
        developers {
            developer {
                id.set("vanshg")
                name.set("Vansh Gandhi")
                email.set("vansh@smileidentity.com")
                url.set("https://github.com/vanshg")
                organization.set("Smile ID")
                organizationUrl.set("https://smileidentity.com")
            }
            developer {
                id.set("JNdhlovu")
                name.set("Japhet Ndhlovu")
                email.set("japhet@smileidentity.com")
                url.set("https://github.com/jndhlovu")
                organization.set("Smile ID")
                organizationUrl.set("https://smileidentity.com")
            }
        }
    }
}

dependencies {
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.moshi)
    implementation(libs.okhttp.logging.interceptor)

    // Moshi is exposed in public SmileID interface, hence "api" vs "implementation"
    api(libs.moshi)
    implementation(libs.moshi.adapters)
    implementation(libs.moshi.adapters.lazy)

    implementation(libs.androidx.core)
    implementation(libs.androidx.fragment)

    // Logging
    implementation(libs.timber)

    // Sentry (crash reporting, tracing, breadcrumbs)
    implementation(platform(libs.sentry.bom))
    implementation(libs.sentry)

    // ViewModel and utilities for Compose
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)

    // Jetpack Compose version is defined by BOM ("Bill-of-Materials")
    // Latest BOM version: https://developer.android.com/jetpack/compose/setup#bom-version-mapping
    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    androidTestImplementation(composeBom)

    // Material Design 3 components
    implementation(libs.androidx.compose.material3)
    // Jetpack Compose UI
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.text.google.fonts)
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
    testImplementation(libs.okhttp.mockwebserver)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.mockk)
    androidTestImplementation(libs.mockk.android)

    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.androidx.test.rules)
    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(libs.androidx.test.espresso)
    androidTestImplementation(libs.androidx.test.fragment)
}
