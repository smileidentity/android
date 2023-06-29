package com.smileidentity.util

import com.smileidentity.SmileID
import org.junit.Assert.assertTrue
import org.junit.Test

class UtilTest {

    @Test
    fun `should include timestamp in filename`() {
        // when
        SmileID.fileSavePath = "."
        val file = createLivenessFile()
        // name is si_liveness_{timestamp}.jpg
        val stringTokens = file.name.replace(".jpg", "").split("_")
        val timestamp = stringTokens[stringTokens.size - 1].toLong()

        // then
        assertTrue(
            timestamp in (System.currentTimeMillis() - 1000)..(System.currentTimeMillis()),
        )
    }
}
