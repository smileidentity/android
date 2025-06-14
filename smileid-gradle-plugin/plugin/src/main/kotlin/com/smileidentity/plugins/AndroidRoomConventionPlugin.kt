package com.smileidentity.plugins

import androidx.room.gradle.RoomExtension
import com.google.devtools.ksp.gradle.KspExtension
import com.smileidentity.ext.BaseSmileIDPluginExtension
import com.smileidentity.ext.Convention
import com.smileidentity.ext.libs
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

internal object AndroidRoomConventionPlugin : Convention {

    override fun apply(target: BaseSmileIDPluginExtension<*>) = with(target) {
        with(project) {
            with(pluginManager) {
                apply("androidx.room")
                apply("com.google.devtools.ksp")
            }

            extensions.configure<KspExtension> {
                arg("room.generateKotlin", "true")
            }

            extensions.configure<RoomExtension> {
                // The schemas directory contains a schema file for each version of the Room database.
                // This is required to enable Room auto migrations.
                // See https://developer.android.com/reference/kotlin/androidx/room/AutoMigration.
                schemaDirectory("$projectDir/schemas")
            }

            dependencies {
                add("implementation", libs.findLibrary("room.runtime").get())
                add("implementation", libs.findLibrary("room.ktx").get())
                add("ksp", libs.findLibrary("room.compiler").get())
                add("androidTestImplementation", libs.findLibrary("room.testing").get())
            }
        }
    }
}
