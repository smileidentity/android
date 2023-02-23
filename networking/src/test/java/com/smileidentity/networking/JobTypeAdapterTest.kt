package com.smileidentity.networking

import com.smileidentity.networking.models.JobType
import com.squareup.moshi.Moshi
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
}
