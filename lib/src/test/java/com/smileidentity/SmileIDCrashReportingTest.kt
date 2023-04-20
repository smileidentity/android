package com.smileidentity

import io.sentry.Hub
import io.sentry.NoOpHub
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SmileIDCrashReportingTest {
    @Before
    fun setUp() {
        SmileIDCrashReporting.disable()
    }

    @Test
    fun shouldBeDisabledByDefault() {
        assertTrue(SmileIDCrashReporting.hub is NoOpHub)
        assertFalse(SmileIDCrashReporting.hub.options.isEnableUncaughtExceptionHandler)
    }

    @Test
    fun shouldBeEnabledOnOptIn() {
        SmileIDCrashReporting.enable()
        assertTrue(SmileIDCrashReporting.hub is Hub)
        assertTrue(SmileIDCrashReporting.hub.options.isEnableUncaughtExceptionHandler)
    }

    @Test
    fun shouldDisableOnOptOut() {
        SmileIDCrashReporting.enable()
        SmileIDCrashReporting.disable()
        assertTrue(SmileIDCrashReporting.hub is NoOpHub)
        assertFalse(SmileIDCrashReporting.hub.options.isEnableUncaughtExceptionHandler)
    }
}
