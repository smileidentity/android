package com.smileidentity.plugins

import com.smileidentity.ext.BaseSmileIDPluginExtension
import com.smileidentity.ext.Convention
import org.gradle.api.tasks.testing.logging.TestExceptionFormat

internal object TestingConvention : Convention {

    @Suppress("UnstableApiUsage")
    override fun apply(extension: BaseSmileIDPluginExtension<*>) {
        with(extension.androidExtension) {
            testOptions {
                unitTests {
                    all { test ->
                        test.testLogging.showStackTraces = true
                        test.testLogging.exceptionFormat = TestExceptionFormat.FULL

                        /* Let unit tests run in multiple parallel forked processes.
                         * Docs here: https://docs.gradle.org/current/userguide/performance.html#parallel_test_execution */
                        val processors = Runtime.getRuntime().availableProcessors()
                        val forks = (processors / 2).coerceAtLeast(1)
                        test.maxParallelForks = forks
                    }
                    isIncludeAndroidResources = true
                    isReturnDefaultValues = true
                }
                animationsDisabled = true
            }
        }
    }
}
