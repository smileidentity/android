package com.smileidentity.networking

import com.smileidentity.models.JobType
import com.squareup.moshi.Moshi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class JobTypeAdapterTest {
    private val adapter = Moshi.Builder()
        .add(JobTypeAdapter)
        .build()
        .adapter(JobType::class.java)!!

    @Test
    fun `toJson should return the correct value`() {
        // given
        for (value in JobType.values()) {
            // when
            val result = adapter.toJson(value)

            // then
            assertTrue(result == value.value.toString())
        }
    }

    @Test
    fun `fromJson should return the correct value`() {
        // given
        for (value in JobType.values()) {
            // when
            val result = adapter.fromJson(value.value.toString())

            // then
            assertTrue(result == value)
        }
    }

    @Test
    fun `should default to Unknown`() {
        // given
        val value = "999"

        // when
        val result = adapter.fromJson(value)

        // then
        assertEquals(JobType.Unknown, result)
    }
}
