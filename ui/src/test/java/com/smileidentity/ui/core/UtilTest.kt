package com.smileidentity.ui.core

import org.junit.Assert.assertTrue
import org.junit.Test

class UtilTest {

    @Test
    fun `should add timestamp to filename`() {
        val file = createLivenessFile()
        // name is si_liveness_{timestamp}_{random_indentifier}.jpg
        val stringTokens = file.name.split("_")
        val timestamp = stringTokens[stringTokens.size - 2]
        val timestampLong = timestamp.toLong()
        assertTrue(
            timestampLong in (
                System.currentTimeMillis() - 1000
                )..(System.currentTimeMillis()),
        )
    }
}
