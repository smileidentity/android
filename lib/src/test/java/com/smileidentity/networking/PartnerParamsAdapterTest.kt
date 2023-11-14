package com.smileidentity.networking

import com.smileidentity.models.JobType
import com.smileidentity.models.PartnerParams
import com.squareup.moshi.Moshi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PartnerParamsAdapterTest {
    private val adapter = Moshi.Builder()
        .add(JobTypeAdapter)
        .add(PartnerParamsAdapter)
        .build()
        .adapter(PartnerParams::class.java)!!

    @Test
    fun `extras should be serialized to top level of json object`() {
        // given
        val partnerParams = PartnerParams(
            jobType = JobType.SmartSelfieEnrollment,
            jobId = "jobId",
            userId = "userId",
            extras = mapOf("extra1" to "value1", "extra2" to "value2"),
        )
        val kepMap = mapOf(
            "jobId" to "job_id",
            "userId" to "user_id",
            "jobType" to "job_type",
        )

        // when
        val jsonString = adapter.toJson(partnerParams)

        // then
        assertFalse(jsonString.contains("\"extras\":{"))
        assertTrue(jsonString.contains("\"extra1\":\"value1\""))
        assertTrue(jsonString.contains("\"extra2\":\"value2\""))
        // Since the adapter has to manually decode the other keys, check that they are all present
        // in case new ones get added later
        val nonExtras = PartnerParams::class.java.declaredFields
            // re: the $stable field - https://developer.android.com/reference/kotlin/androidx/compose/runtime/internal/StabilityInferred
            .filterNot { setOf("extras", "\$stable", "CREATOR").contains(it.name) }
        for (it in nonExtras) {
            val expectedKey = kepMap[it.name]!!
            assertTrue(jsonString.contains(expectedKey))
        }
    }

    @Test
    fun `extras should be deserialized from top level of json object`() {
        // given
        // language=json
        val jsonString = """{
            "job_id": "jobId", 
            "user_id": "userId", 
            "job_type": "4", 
            "extra1": "value1", 
            "extra2": "value2"
        }"""

        // when
        val partnerParams = adapter.fromJson(jsonString)!!

        // then
        assertEquals("jobId", partnerParams.jobId)
        assertEquals("userId", partnerParams.userId)
        assertEquals(JobType.SmartSelfieEnrollment, partnerParams.jobType)
        assertEquals("value1", partnerParams.extras["extra1"])
        assertEquals("value2", partnerParams.extras["extra2"])
    }
}
