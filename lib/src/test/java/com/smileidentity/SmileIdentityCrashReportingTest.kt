package com.smileidentity

import io.sentry.Hub
import io.sentry.NoOpHub
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SmileIdentityCrashReportingTest {
    @Before
    fun setUp() {
        SmileIdentityCrashReporting.disable()
    }

    @Test
    fun shouldBeDisabledByDefault() {
        assertTrue(SmileIdentityCrashReporting.hub is NoOpHub)
        assertFalse(SmileIdentityCrashReporting.hub.options.isEnableUncaughtExceptionHandler)
    }

    @Test
    fun shouldBeEnabledOnOptIn() {
        SmileIdentityCrashReporting.enable()
        assertTrue(SmileIdentityCrashReporting.hub is Hub)
        assertTrue(SmileIdentityCrashReporting.hub.options.isEnableUncaughtExceptionHandler)
    }

    @Test
    fun shouldDisableOnOptOut() {
        SmileIdentityCrashReporting.enable()
        SmileIdentityCrashReporting.disable()
        assertTrue(SmileIdentityCrashReporting.hub is NoOpHub)
        assertFalse(SmileIdentityCrashReporting.hub.options.isEnableUncaughtExceptionHandler)
    }
}
