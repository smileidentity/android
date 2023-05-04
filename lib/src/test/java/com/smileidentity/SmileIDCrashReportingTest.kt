package com.smileidentity

import com.smileidentity.models.Config
import io.sentry.Hub
import io.sentry.NoOpHub
import org.junit.Assert.assertEquals
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

    @Test
    fun shouldSetUser() {
        // given
        val expectedPartnerId = "000"
        SmileID.config = Config(expectedPartnerId, "", "", "")

        // when
        var actualPartnerId: String? = null
        SmileIDCrashReporting.enable()
        SmileIDCrashReporting.hub.withScope {
            actualPartnerId = it.user?.id
        }

        // then
        assertEquals(expectedPartnerId, actualPartnerId)
    }
}
