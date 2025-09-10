plugins {
    alias(libs.plugins.smileid.android.library)
    alias(libs.plugins.maven.publish)
}
val groupId = "com.smileidentity"
val artifactId = "android-attestation"
project.version = findProperty("VERSION_NAME") as? String
    ?: file("../lib/VERSION").readText().trim()
android {
    namespace = "com.smileidentity.attestation"
    buildFeatures {
        buildConfig = true
    }
    buildTypes {
        all {
            buildConfigField("String", "VERSION_NAME", "\"${version}\"")
        }
    }
}
mavenPublishing {
    publishToMavenCentral(automaticRelease = true)
    signAllPublications()
    coordinates(groupId, artifactId, version.toString())
    pom {
        name = "Smile ID Android Attestation"
        description =
            "Internal attestation support library used by the Smile ID Android SDK. Not for direct integration."
        url = "https://github.com/smileidentity/android"
        licenses {
            license {
                name = "Smile ID Terms of Use"
                url = "https://usesmileid.com/terms-and-conditions"
                distribution = "repo"
            }
            license {
                name = "The MIT License"
                url = "https://opensource.org/licenses/MIT"
                distribution = "repo"
            }
        }
        scm {
            url = "https://github.com/smileidentity/android"
            connection = "scm:git:git://github.com/smileidentity/android.git"
            developerConnection = "scm:git:ssh://github.com/smileidentity/android.git"
        }
        developers {
            developer {
                id = "vanshg"
                name = "Vansh Gandhi"
                email = "vansh@usesmileid.com"
                url = "https://github.com/vanshg"
                organization = "Smile ID"
                organizationUrl = "https://usesmileid.com"
            }
            developer {
                id = "JNdhlovu"
                name = "Japhet Ndhlovu"
                email = "japhet@usesmileid.com"
                url = "https://github.com/jndhlovu"
                organization = "Smile ID"
                organizationUrl = "https://usesmileid.com"
            }
            developer {
                id = "jumaallan"
                name = "Juma Allan"
                email = "juma@usesmileid.com"
                url = "https://github.com/jumaallan"
                organization = "Smile ID"
                organizationUrl = "https://usesmileid.com"
            }
        }
    }
}

dependencies {
    implementation(libs.play.integrity)
    implementation(libs.coroutines.core)
    implementation(libs.smileid.security)
    testImplementation(libs.mockk)
}
