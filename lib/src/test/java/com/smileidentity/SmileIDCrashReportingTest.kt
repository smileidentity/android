package com.smileidentity

import com.smileidentity.models.Config
import io.sentry.NoOpScopes
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

class SmileIDCrashReportingTest {
    @Before
    fun setUp() {
        SmileIDCrashReporting.disable()
    }

    @Test
    fun shouldBeDisabledByDefault() {
        assertTrue(SmileIDCrashReporting.scopes is NoOpScopes)
        assertFalse(SmileIDCrashReporting.scopes.options.isEnableUncaughtExceptionHandler)
    }

    @Test
    fun shouldBeEnabledOnOptIn() {
        SmileIDCrashReporting.enable()
        assertFalse(SmileIDCrashReporting.scopes is NoOpScopes)
        assertTrue(SmileIDCrashReporting.scopes.options.isEnableUncaughtExceptionHandler)
    }

    @Test
    fun shouldDisableOnOptOut() {
        SmileIDCrashReporting.enable()
        SmileIDCrashReporting.disable()
        assertTrue(SmileIDCrashReporting.scopes is NoOpScopes)
        assertFalse(SmileIDCrashReporting.scopes.options.isEnableUncaughtExceptionHandler)
    }

    @Test
    fun shouldSetUser() {
        // given
        val expectedPartnerId = "000"
        SmileID.config = Config(expectedPartnerId, "", "", "")

        // when
        var actualPartnerId: String? = null
        SmileIDCrashReporting.enable()
        SmileIDCrashReporting.scopes.withScope {
            actualPartnerId = it.user?.id
        }

        // then
        assertEquals(expectedPartnerId, actualPartnerId)
    }
}
