package com.smileidentity

import com.android.build.api.dsl.ApplicationExtension
import com.smileidentity.ext.SmileIDAppExtension
import com.smileidentity.ext.libs
import com.smileidentity.plugins.AndroidAppConfigConvention
import com.smileidentity.plugins.AndroidHiltConventionPlugin
import com.smileidentity.plugins.AndroidRoomConventionPlugin
import com.smileidentity.plugins.TestingConvention
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.the

@Suppress("unused")
internal class SmileIDAppPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {

            with(pluginManager) {
                apply("com.android.application")
            }

            extensions.create(
                /* name = */
                "smileidApp",
                /* type = */
                SmileIDAppExtension::class.java,
                /* ...constructionArguments = */
                target,
                the<ApplicationExtension>(),
            )


            extensions.configure<SmileIDAppExtension> {
                uses(AndroidAppConfigConvention)
                uses(AndroidHiltConventionPlugin)
                uses(AndroidRoomConventionPlugin)
                uses(TestingConvention)
            }

            dependencies {
                add("implementation", libs.findLibrary("timber").get())
            }
        }
    }
}
