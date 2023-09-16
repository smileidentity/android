package com.smileidentity.networking

import com.smileidentity.models.BiometricKycJobResult
import com.smileidentity.models.DocumentVerificationJobResult
import com.smileidentity.models.JobResult
import com.smileidentity.models.JobType
import com.smileidentity.models.SmartSelfieJobResult
import com.squareup.moshi.Moshi
import org.junit.Assert.assertEquals
import org.junit.Test

class JobResultAdapterTest {
    private val moshi = Moshi.Builder()
        .add(SmartSelfieJobResultAdapter)
        .add(DocumentVerificationJobResultAdapter)
        .add(BiometricKycJobResultAdapter)
        .add(JobTypeAdapter)
        .add(PartnerParamsAdapter)
        .add(JobResultAdapter)
        .add(StringifiedBooleanAdapter)
        .build()

    @Test
    fun `should parse freeform string for SmartSelfie`() {
        // given
        val json = "No zip uploaded yet"

        // when
        val jobResult = moshi
            .adapter(SmartSelfieJobResult::class.java)
            .fromJsonValue(json)

        // then
        assertEquals(JobResult.Freeform("No zip uploaded yet"), jobResult)
    }

    @Test
    fun `should parse smart selfie job result entry`() {
        // given
        // language=json
        val json = """{
            "Source": "Source",
            "Actions": {},
            "ResultCode":  "1",
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
        val jobResult = moshi.adapter(SmartSelfieJobResult::class.java)
            .fromJson(json) as SmartSelfieJobResult.Entry

        // then
        assertEquals("1", jobResult.resultCode)
        assertEquals("ResultText", jobResult.resultText)
        assertEquals("SmileJobID", jobResult.smileJobId)
        assertEquals("jobId", jobResult.partnerParams.jobId)
        assertEquals("userId", jobResult.partnerParams.userId)
        assertEquals(JobType.SmartSelfieEnrollment, jobResult.partnerParams.jobType)
        assertEquals(99.99, jobResult.confidence!!, 0.0)
    }

    @Test
    fun `should parse freeform string for BiometricKyc`() {
        // given
        val json = "No zip uploaded yet"

        // when
        val jobResult = moshi
            .adapter(BiometricKycJobResult::class.java)
            .fromJsonValue(json)

        // then
        assertEquals(JobResult.Freeform("No zip uploaded yet"), jobResult)
    }

    @Test
    fun `should parse Biometric KYC job result entry`() {
        // given
        // language=json
        val json = """
        {
           "JSONVersion":"1.0.0",
           "SmileJobID":"0000056574",
           "PartnerParams":{
              "user_id":"KE_TESTTEST_100",
              "job_id":"KE_TEST_100",
              "job_type":1
           },
           "ResultType":"ID Verification",
           "ResultText":"ID Number Validated",
           "ResultCode":"1012",
           "IsFinalResult":"true",
           "Actions":{
              "Verify_ID_Number":"Verified",
              "Return_Personal_Info":"Returned"
           },
           "Country":"NG",
           "IDType":"DRIVERS_LICENSE",
           "IDNumber":"ABC000000000",
           "ExpirationDate":"2021-08-15",
           "FullName":"Leo Doe Joe",
           "DOB":"2000-09-20",
           "Photo":"<BASE64 STRING>",
           "signature": "...",
           "timestamp":"2021-08-12T17:57:00.614879"
        }
        """.trimIndent()

        // when
        val jobResult = moshi.adapter(BiometricKycJobResult::class.java)
            .fromJson(json) as BiometricKycJobResult.Entry

        // then
        assertEquals("1012", jobResult.resultCode)
        assertEquals("ID Number Validated", jobResult.resultText)
        assertEquals("0000056574", jobResult.smileJobId)
        assertEquals("KE_TEST_100", jobResult.partnerParams.jobId)
        assertEquals("KE_TESTTEST_100", jobResult.partnerParams.userId)
        assertEquals(JobType.BiometricKyc, jobResult.partnerParams.jobType)
        assertEquals("2021-08-15", jobResult.expirationDate)
        assertEquals("2000-09-20", jobResult.dob)
        assertEquals("ABC000000000", jobResult.idNumber)
    }

    @Test
    fun `should parse freeform string for DocumentVerification`() {
        // given
        val json = "No zip uploaded yet"

        // when
        val jobResult = moshi
            .adapter(DocumentVerificationJobResult::class.java)
            .fromJsonValue(json)

        // then
        assertEquals(JobResult.Freeform("No zip uploaded yet"), jobResult)
    }

    @Test
    fun `should parse Document Verification job result entry`() {
        // given
        // language=json
        val json = """
        {
            "DOB": "1989-01-20",
            "FullName": "John Joe Doe",
            "Gender": "M",
            "IDType": "PASSPORT",
            "Actions": {
                "Liveness_Check": "Passed",
                "Register_Selfie": "Approved",
                "Verify_Document": "Passed",
                "Human_Review_Compare": "Passed",
                "Return_Personal_Info": "Returned",
                "Selfie_To_ID_Card_Compare": "Completed",
                "Human_Review_Liveness_Check": "Passed"
            },
            "Country": "NG",
            "Document": "----base64 encoded string----",
            "IDNumber": "B00123456",
            "ResultCode": "0810",
            "ResultText": "Document Verified After Human Review",
            "SmileJobID": "0000000046",
            "PartnerParams": {
                "job_id": "Testing_0036",
                "user_id": "Test_0036",
                "job_type": 6
            },
            "ExpirationDate": "2025-11-26",
            "timestamp": "2021-12-14T20:07:56.829Z",
            "signature": "---signature---"
        }
        """.trimIndent()

        // when
        val jobResult = moshi.adapter(DocumentVerificationJobResult::class.java)
            .fromJson(json) as DocumentVerificationJobResult.Entry

        // then
        assertEquals("0810", jobResult.resultCode)
        assertEquals("Document Verified After Human Review", jobResult.resultText)
        assertEquals("0000000046", jobResult.smileJobId)
        assertEquals("Testing_0036", jobResult.partnerParams.jobId)
        assertEquals("Test_0036", jobResult.partnerParams.userId)
        assertEquals(JobType.DocumentVerification, jobResult.partnerParams.jobType)
        assertEquals("2025-11-26", jobResult.expirationDate)
        assertEquals("1989-01-20", jobResult.dob)
    }
}
