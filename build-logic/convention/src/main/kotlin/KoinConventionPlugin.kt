import com.smileidentity.buildlogic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class KoinConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {

            dependencies {
                "implementation"(platform(libs.findLibrary("koin.bom").get()))
                "implementation"(libs.findLibrary("koin.android").get())
                "implementation"(libs.findLibrary("koin.compose").get())
                "implementation"(libs.findLibrary("koin.coroutine").get())

                "androidTestImplementation"(libs.findLibrary("koin.test").get())

                "testImplementation"(libs.findLibrary("koin.junit4").get())
            }
        }
    }
}

