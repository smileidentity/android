package com.smileidentity.networking

import com.smileidentity.SmileID
import com.smileidentity.models.ActionResult
import org.junit.Assert.assertEquals
import org.junit.Test

class ActionResultTest {
    private val adapter = SmileID.moshi.adapter(ActionResult::class.java)

    @Test
    fun `should default to Unknown`() {
        // given
        val json = "Gibberish"

        // when
        val jobResult = adapter.fromJsonValue(json) as ActionResult

        // then
        assertEquals(ActionResult.Unknown, jobResult)
    }
}
