package com.smileidentity.plugins

import com.smileidentity.ext.BaseSmileIDPluginExtension
import com.smileidentity.ext.Convention
import com.smileidentity.ext.SmileIDAppExtension

internal object AndroidAppConfigConvention : Convention {

    override fun apply(extension: BaseSmileIDPluginExtension<*>) = with(extension) {
        require(this is SmileIDAppExtension) {
            "AndroidAppConfigConvention can only be applied to modules that include smileid-android-app"
        }

        with(androidExtension) {
            defaultConfig {
                compileSdk = 35
                minSdk = 21
                targetSdk = 35
            }

            buildTypes {
                debug {
                    isDebuggable = true
                }

                release {
                    isMinifyEnabled = true
                    proguardFiles(
                        "proguard-rules.pro",
                        getDefaultProguardFile("proguard-android.txt"),
                    )
                }
            }

            packaging {
                resources.excludes.add("META-INF/*")
            }
        }
    }
}
