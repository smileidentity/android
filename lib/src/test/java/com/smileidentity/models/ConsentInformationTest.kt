package com.smileidentity.models

import com.smileidentity.SmileID
import org.junit.Assert.assertEquals
import org.junit.Test

class ConsentInformationTest {

    @Test
    fun testSecondaryConstructor() {
        // given - using the secondary constructor with legacy property names
        val consentInfo = ConsentInformation(
            consentGrantedDate = "2025-04-01T15:16:03.246Z",
            personalDetailsConsentGranted = true,
            contactInfoConsentGranted = true,
            documentInfoConsentGranted = true,
        )

        // when
        val json = SmileID.moshi.adapter(ConsentInformation::class.java).toJson(consentInfo)

        // then - verify it still produces the correct nested structure
        assert(json.contains("\"consented\":{")) { "JSON should contain 'consented' key" }
        assert(json.contains("\"consent_granted_date\":\"2025-04-01T15:16:03.246Z\""))
        assert(json.contains("\"personal_details\":true"))
        assert(json.contains("\"contact_information\":true"))
        assert(json.contains("\"document_information\":true"))

        // and check properties are accessible through both APIs
        assertEquals("2025-04-01T15:16:03.246Z", consentInfo.consentGrantedDate)
        assertEquals(true, consentInfo.personalDetailsConsentGranted)
        assertEquals(true, consentInfo.contactInfoConsentGranted)
        assertEquals(true, consentInfo.documentInfoConsentGranted)

        assertEquals("2025-04-01T15:16:03.246Z", consentInfo.consented.consentGrantedDate)
        assertEquals(true, consentInfo.consented.personalDetails)
        assertEquals(true, consentInfo.consented.contactInformation)
        assertEquals(true, consentInfo.consented.documentInformation)
    }

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

    @Test
    fun testBackwardCompatibilityProperties() {
        // given
        val consentInformation = ConsentInformation(
            consented = ConsentedInformation(
                consentGrantedDate = "2025-04-01T15:16:03.246Z",
                personalDetails = true,
                contactInformation = true,
                documentInformation = true,
            ),
        )

        // then - verify backward compatibility properties match the nested values
        assertEquals("2025-04-01T15:16:03.246Z", consentInformation.consentGrantedDate)
        assertEquals(true, consentInformation.personalDetailsConsentGranted)
        assertEquals(true, consentInformation.contactInfoConsentGranted)
        assertEquals(true, consentInformation.documentInfoConsentGranted)
    }

    @Test
    fun testLegacyFactoryMethod() {
        // given
        val legacyConsentInfo = ConsentInformation.createLegacy(
            consentGrantedDate = "2025-04-01T15:16:03.246Z",
            personalDetailsConsentGranted = true,
            contactInfoConsentGranted = true,
            documentInfoConsentGranted = true,
        )

        // when
        val json = SmileID.moshi.adapter(ConsentInformation::class.java).toJson(legacyConsentInfo)

        // then - verify we still get the same nested structure
        assert(
            json.contains("\"consented\":{"),
        ) { "JSON should contain 'consented' key but was: $json" }
        assert(json.contains("\"consent_granted_date\":\"2025-04-01T15:16:03.246Z\""))
        assert(json.contains("\"personal_details\":true"))
        assert(json.contains("\"contact_information\":true"))
        assert(json.contains("\"document_information\":true"))

        // and verify the properties are accessible through both old and new paths
        assertEquals("2025-04-01T15:16:03.246Z", legacyConsentInfo.consentGrantedDate)
        assertEquals("2025-04-01T15:16:03.246Z", legacyConsentInfo.consented.consentGrantedDate)
        assertEquals(true, legacyConsentInfo.personalDetailsConsentGranted)
        assertEquals(true, legacyConsentInfo.consented.personalDetails)
    }

    @Test
    fun testJsonDeserialization() {
        // given - a JSON with the nested structure
        val json = """
            {
              "consented": {
                "consent_granted_date": "2025-04-01T15:16:03.246Z",
                "personal_details": true,
                "contact_information": true,
                "document_information": true
              }
            }
        """.trimIndent()

        // when
        val consentInfo = SmileID.moshi.adapter(ConsentInformation::class.java).fromJson(json)

        // then - verify we can still access via old properties
        assertEquals("2025-04-01T15:16:03.246Z", consentInfo?.consentGrantedDate)
        assertEquals(true, consentInfo?.personalDetailsConsentGranted)
        assertEquals(true, consentInfo?.contactInfoConsentGranted)
        assertEquals(true, consentInfo?.documentInfoConsentGranted)
    }
}
