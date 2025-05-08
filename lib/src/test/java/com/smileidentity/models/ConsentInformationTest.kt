package com.smileidentity.models

import com.smileidentity.SmileID
import org.junit.Test

class ConsentInformationTest {
    @Test
    fun testConsentInformationJsonStructure() {
        // given
        val consentInformation = ConsentInformation(
            consented = ConsentedInformation(
                consentGrantedDate = "2025-04-01T15:16:03.246Z",
                personalDetails = true,
                contactInformation = true,
                documentInformation = true,
            ),
        )

        // when
        val json = SmileID.moshi.adapter(ConsentInformation::class.java).toJson(consentInformation)

        // then
        println("\n=============================================")
        println("Serialized JSON: $json")
        println("=============================================\n")

        // Simply check that the JSON contains the expected nested structure
        assert(
            json.contains("\"consented\":{"),
        ) { "JSON should contain 'consented' key but was: $json" }
        assert(json.contains("\"consent_granted_date\":\"2025-04-01T15:16:03.246Z\"")) {
            "JSON missing consent_granted_date"
        }
        assert(json.contains("\"personal_details\":true")) { "JSON missing personal_details" }
        assert(json.contains("\"contact_information\":true")) { "JSON missing contact_information" }
        assert(
            json.contains("\"document_information\":true"),
        ) { "JSON missing document_information" }
    }
}
