package com.smileidentity.networking

import com.smileidentity.networking.models.JobResult
import com.smileidentity.networking.models.JobType
import com.squareup.moshi.Moshi
import org.junit.Assert.assertEquals
import org.junit.Test

class JobResultAdapterTest {
    private val adapter = Moshi.Builder()
        .add(PartnerParamsAdapter)
        .add(JobResultAdapter)
        .add(StringifiedBooleanAdapter)
        .build()
        .adapter(JobResult::class.java)!!

    @Test
    fun `should parse freeform string`() {
        // given
        val json = "No zip uploaded yet"

        // when
        val jobResult: JobResult.Freeform = adapter.fromJsonValue(json) as JobResult.Freeform

        // then
        assertEquals("No zip uploaded yet", jobResult.result)
    }

    @Test
    fun `should parse job result entry`() {
        // given
        // language=json
        val json = """{
            "Source": "Source",
            "Actions": {},
            "ResultCode":  1,
            "ResultText": "ResultText",
            "ResultType": "ResultType",
            "SmileJobID": "SmileJobID",
            "JSONVersion": "JSONVersion",
            "PartnerParams": {
                 "job_id": "jobId",
                 "user_id": "userId",
                 "job_type": "4"
            },
            "ConfidenceValue":  "99.99",
            "IsFinalResult": "true",
            "IsMachineResult": "false"
        }"""

        // when
        val jobResult: JobResult.Entry = adapter.fromJson(json) as JobResult.Entry

        // then
        assertEquals("Source", jobResult.source)
        assertEquals(1, jobResult.resultCode)
        assertEquals("ResultText", jobResult.resultText)
        assertEquals("ResultType", jobResult.resultType)
        assertEquals("SmileJobID", jobResult.smileJobId)
        assertEquals("jobId", jobResult.partnerParams.jobId)
        assertEquals("userId", jobResult.partnerParams.userId)
        assertEquals(JobType.SmartSelfieEnrollment, jobResult.partnerParams.jobType)
        assertEquals(99.99, jobResult.confidence, 0.0)
        assertEquals(true, jobResult.isFinalResult)
        assertEquals(false, jobResult.isMachineResult)
    }
}
