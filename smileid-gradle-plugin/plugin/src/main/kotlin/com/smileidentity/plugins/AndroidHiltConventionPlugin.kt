package com.smileidentity.plugins

import com.smileidentity.ext.BaseSmileIDPluginExtension
import com.smileidentity.ext.Convention
import com.smileidentity.ext.libs
import org.gradle.kotlin.dsl.dependencies

internal object AndroidHiltConventionPlugin : Convention {

    override fun apply(target: BaseSmileIDPluginExtension<*>) = with(target) {
        with(project) {
            with(pluginManager) {
                apply("com.google.devtools.ksp")
                apply("dagger.hilt.android.plugin")
            }

            dependencies {
                add("implementation", libs.findLibrary("hilt.core").get())
                add("implementation", libs.findLibrary("hilt.android").get())
                "ksp"(libs.findLibrary("hilt.compiler").get())
                add("implementation", libs.findLibrary("hilt.compose").get())
                add("androidTestImplementation", libs.findLibrary("hilt.android.testing").get())
                add("testImplementation", libs.findLibrary("hilt.android.testing").get())
            }
        }
    }
}
