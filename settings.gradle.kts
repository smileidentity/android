@file:Suppress("UnstableApiUsage")

rootProject.name = "SmileID"
include("lib", "sample")
rootProject.children.forEach { it.buildFileName = "${it.name}.gradle.kts" }

pluginManagement {
    repositories {
        google()
        mavenCentral()
        // gradlePluginPortal is last for faster builds
        // see: https://developer.android.com/studio/build/optimize-your-build#gradle_plugin_portal
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
    repositories {
        google()
        mavenCentral()
    }
}
