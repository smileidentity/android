package com.smileidentity.ui.core

import org.junit.Assert.assertTrue
import org.junit.Test

class UtilTest {

    @Test
    fun `should include timestamp in filename`() {
        // when
        val file = createLivenessFile("test")
        // name is si_liveness_{timestamp}_{random_identifier}.jpg
        val stringTokens = file.name.split("_")
        val timestamp = stringTokens[stringTokens.size - 2].toLong()

        // then
        assertTrue(
            timestamp in (System.currentTimeMillis() - 1000)..(System.currentTimeMillis()),
        )
    }
}
