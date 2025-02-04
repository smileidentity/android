package com.smileidentity.networking

import com.smileidentity.SmileID
import com.smileidentity.models.ConsentInformation
import com.smileidentity.models.EnhancedKycAsyncResponse
import com.smileidentity.models.EnhancedKycRequest
import com.smileidentity.models.JobType.EnhancedKyc
import com.smileidentity.models.PartnerParams
import org.junit.Test

class EnhancedKycTest {
    @Test
    fun shouldDecodeEnhancedKycAsyncResponseJson() {
        // given
        val json = """{"success": true}"""

        // when
        val response = SmileID.moshi.adapter(EnhancedKycAsyncResponse::class.java).fromJson(json)

        // then
        assert(response!!.success)
    }

    @Test
    fun shouldIncludeCallbackUrlForEnhancedKycAsync() {
        // given
        val request = EnhancedKycRequest(
            country = "country",
            idType = "idType",
            idNumber = "idNumber",
            callbackUrl = "callbackUrl",
            partnerId = "partnerId",
            signature = "signature",
            partnerParams = PartnerParams(EnhancedKyc),
            consentInformation = ConsentInformation(
                consentGrantedDate = "somedate",
                personalDetailsConsentGranted = true,
                contactInfoConsentGranted = true,
                documentInfoConsentGranted = true,
            ),
        )

        // when
        val json = SmileID.moshi.adapter(EnhancedKycRequest::class.java).toJson(request)

        // then
        assert(json.contains("callback_url"))
    }
}
