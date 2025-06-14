package com.smileidentity.ext

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Project

open class BaseSmileIDPluginExtension<T : CommonExtension<*, *, *, *, *, *>>(
    internal val project: Project,
    internal val androidExtension: T,
) {
    // var namespace: String?
    //     get() = androidExtension.namespace
    //     set(value) {
    //         androidExtension.namespace = value
    //     }

    fun <T : Convention> uses(convention: T) = apply { convention.apply(this) }
}

open class SmileIDAppExtension(
    project: Project,
    applicationExtension: ApplicationExtension,
) : BaseSmileIDPluginExtension<ApplicationExtension>(project, applicationExtension)
