plugins {
    alias(libs.plugins.smileid.android.library)
    alias(libs.plugins.smileid.android.library.jacoco)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.smileidentity.navigation"
}

ksp {
    arg("compose-destinations.mode", "destinations")
    arg("compose-destinations.moduleName", "navigation")
    arg("compose-destinations.htmlMermaidGraph", "$rootDir//navigation-docs")
    arg("compose-destinations.mermaidGraph", "$rootDir/navigation-docs")
}

dependencies {
    implementation(project(":camera"))
    implementation(project(":ml"))
    implementation(project(":ui"))

    implementation(libs.destinations)
    implementation(libs.destinations.bottom.sheet)
    ksp(libs.destinations.ksp)
}
