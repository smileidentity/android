@file:Suppress("unused")

package com.smileidentity.models

import android.os.Parcelable
import com.smileidentity.util.getCurrentIsoTimestamp
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.File
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

/**
 * This class represents security_info.json
 */
@JsonClass(generateAdapter = true)
data class SecurityInfoRequest(
    @Json(name = "timestamp") val timestamp: String,
    @Json(name = "mac") val mac: String,
)

/**
 * This class represents info.json
 */
@JsonClass(generateAdapter = true)
data class UploadRequest(
    @Json(name = "images") val images: List<UploadImageInfo>,
    @Json(name = "id_info") val idInfo: IdInfo? = null,
    @Json(name = "consent_information") val consentInformation: ConsentInformation? = null,
)

@JsonClass(generateAdapter = true)
data class UploadImageInfo(
    @Json(name = "image_type_id") val imageTypeId: ImageType,
    @Json(name = "file_name") val image: File,
)

/**
 * @param country The 2 letter country code of the user's ID (ISO 3166-1 alpha-2 format)
 * @param idType The ID type from the list [here](https://docs.usesmileid.com/supported-id-types/for-individuals-kyc/backed-by-id-authority)
 * @param idNumber The ID number of the user's ID
 * @param firstName The first name of the user
 * @param middleName The middle name of the user
 * @param lastName The last name of the user
 * @param dob The date of birth of the user in the **ID type specific format**
 * @param entered Whether to submit the verification to the ID authority or not. For Biometric KYC
 * jobs, this should be set to true
 */
@Serializable
@Parcelize
@JsonClass(generateAdapter = true)
data class IdInfo(
    @Json(name = "country") val country: String,
    @Json(name = "id_type") val idType: String? = null,
    @Json(name = "id_number") val idNumber: String? = null,
    @Json(name = "first_name") val firstName: String? = null,
    @Json(name = "middle_name") val middleName: String? = null,
    @Json(name = "last_name") val lastName: String? = null,
    @Json(name = "dob") val dob: String? = null,
    @Json(name = "bank_code") val bankCode: String? = null,
    @Json(name = "entered") val entered: Boolean? = null,
) : Parcelable

/**
 * Class representing user consent information submitted with verification jobs.
 * As of version 10.6.2, the structure was updated to match API requirements by nesting
 * consent fields under a "consented" object. This class provides backward compatibility
 * with previous SDK versions while maintaining the new API-compatible JSON structure.
 *
 * Preferred usage (current API format):
 * ```
 * val consentInfo = ConsentInformation(
 *     consented = ConsentedInformation(
 *         consentGrantedDate = getCurrentIsoTimestamp(),
 *         personalDetails = true,
 *         contactInformation = true,
 *         documentInformation = true
 *     )
 * )
 * ```
 *
 * For backward compatibility, you can also use the secondary constructor with the old property names:
 * ```
 * // Direct construction with legacy property names (will be converted to the new structure internally)
 * val consentInfo = ConsentInformation(
 *     consentGrantedDate = getCurrentIsoTimestamp(),
 *     personalDetailsConsentGranted = true,
 *     contactInfoConsentGranted = true,
 *     documentInfoConsentGranted = true
 * )
 * ```
 *
 * Or the legacy factory method:
 * ```
 * // Legacy factory method (will be converted to the new structure internally)
 * val consentInfo = ConsentInformation.createLegacy(
 *     consentGrantedDate = getCurrentIsoTimestamp(),
 *     personalDetailsConsentGranted = true,
 *     contactInfoConsentGranted = true,
 *     documentInfoConsentGranted = true
 * )
 * ```
 *
 * All three approaches will produce identical API-compatible JSON output with the nested structure.
 *
 * @property consented The nested consent information object containing all consent fields
 */
@Serializable
@Parcelize
@JsonClass(generateAdapter = true)
data class ConsentInformation(
    @Json(name = "consented") val consented: ConsentedInformation,
) : Parcelable {

    /**
     * Secondary constructor to support direct creation with legacy properties.
     * This constructor creates the object with the new nested structure
     * but accepts parameters in the old format for backward compatibility.
     *
     * @param consentGrantedDate The timestamp of when consent was granted
     * @param personalDetailsConsentGranted Whether consent for personal details was granted
     * @param contactInfoConsentGranted Whether consent for contact information was granted
     * @param documentInfoConsentGranted Whether consent for document information was granted
     */
    @Deprecated(
        message = "Use primary constructor with ConsentedInformation instead",
        replaceWith = ReplaceWith(
            """ConsentInformation(
                ConsentedInformation(
                    consentGrantedDate,
                    personalDetailsConsentGranted,
                    contactInfoConsentGranted,
                    documentInfoConsentGranted
                )
            )""",
        ),
    )
    constructor(
        consentGrantedDate: String = getCurrentIsoTimestamp(),
        personalDetailsConsentGranted: Boolean = false,
        contactInfoConsentGranted: Boolean = false,
        documentInfoConsentGranted: Boolean = false,
    ) : this(
        consented = ConsentedInformation(
            consentGrantedDate = consentGrantedDate,
            personalDetails = personalDetailsConsentGranted,
            contactInformation = contactInfoConsentGranted,
            documentInformation = documentInfoConsentGranted,
        ),
    )
    // Backward compatibility with previous versions - delegated properties that
    // map to the nested structure

    /**
     * Access the consent granted date from the nested structure.
     *
     * @return The consent granted date
     * @deprecated Use [consented.consentGrantedDate] instead
     */
    @get:Deprecated(
        message = "Use consented.consentGrantedDate instead",
        replaceWith = ReplaceWith("consented.consentGrantedDate"),
    )
    val consentGrantedDate: String
        get() = consented.consentGrantedDate

    /**
     * Access whether consent for personal details was granted from the nested structure.
     *
     * @return Whether consent for personal details was granted
     * @deprecated Use [consented.personalDetails] instead
     */
    @get:Deprecated(
        message = "Use consented.personalDetails instead",
        replaceWith = ReplaceWith("consented.personalDetails"),
    )
    val personalDetailsConsentGranted: Boolean
        get() = consented.personalDetails

    /**
     * Access whether consent for contact information was granted from the nested structure.
     *
     * @return Whether consent for contact information was granted
     * @deprecated Use [consented.contactInformation] instead
     */
    @get:Deprecated(
        message = "Use consented.contactInformation instead",
        replaceWith = ReplaceWith("consented.contactInformation"),
    )
    val contactInfoConsentGranted: Boolean
        get() = consented.contactInformation

    /**
     * Access whether consent for document information was granted from the nested structure.
     *
     * @return Whether consent for document information was granted
     * @deprecated Use [consented.documentInformation] instead
     */
    @get:Deprecated(
        message = "Use consented.documentInformation instead",
        replaceWith = ReplaceWith("consented.documentInformation"),
    )
    val documentInfoConsentGranted: Boolean
        get() = consented.documentInformation

    /**
     * Contains factory methods for backward compatibility with older SDK versions.
     */
    companion object {
        /**
         * Creates a ConsentInformation object using the legacy flat structure.
         * Internally converts to the new nested structure required by the API.
         *
         * @param consentGrantedDate The timestamp of when consent was granted
         * @param personalDetailsConsentGranted Whether consent for personal details was granted
         * @param contactInfoConsentGranted Whether consent for contact information was granted
         * @param documentInfoConsentGranted Whether consent for document information was granted
         * @return A ConsentInformation object with the properly nested structure
         * @deprecated Use the primary constructor with a [ConsentedInformation] object
         */
        @JvmStatic
        @Deprecated(
            message = "Use primary constructor with ConsentedInformation instead",
            replaceWith = ReplaceWith(
                """ConsentInformation(
                    ConsentedInformation(
                        consentGrantedDate,
                        personalDetailsConsentGranted,
                        contactInfoConsentGranted,
                        documentInfoConsentGranted
                    )
                )""",
            ),
        )
        fun createLegacy(
            consentGrantedDate: String = getCurrentIsoTimestamp(),
            personalDetailsConsentGranted: Boolean = false,
            contactInfoConsentGranted: Boolean = false,
            documentInfoConsentGranted: Boolean = false,
        ): ConsentInformation {
            return ConsentInformation(
                consented = ConsentedInformation(
                    consentGrantedDate = consentGrantedDate,
                    personalDetails = personalDetailsConsentGranted,
                    contactInformation = contactInfoConsentGranted,
                    documentInformation = documentInfoConsentGranted,
                ),
            )
        }
    }
}

/**
 * Represents the detailed consent information nested within [ConsentInformation].
 * This class follows the API's expected structure for consent data.
 *
 * @property consentGrantedDate The ISO timestamp of when consent was granted
 * @property personalDetails Whether consent for personal details was granted
 * @property contactInformation Whether consent for contact information was granted
 * @property documentInformation Whether consent for document information was granted
 */
@Serializable
@Parcelize
@JsonClass(generateAdapter = true)
data class ConsentedInformation(
    @Json(name = "consent_granted_date") val consentGrantedDate: String = getCurrentIsoTimestamp(),
    @Json(name = "personal_details") val personalDetails: Boolean,
    @Json(name = "contact_information") val contactInformation: Boolean,
    @Json(name = "document_information") val documentInformation: Boolean,
) : Parcelable

enum class ImageType {
    @Json(name = "0")
    SelfieJpgFile,

    @Json(name = "1")
    IdCardJpgFile,

    @Json(name = "2")
    SelfieJpgBase64,

    @Json(name = "3")
    IdCardJpgBase64,

    @Json(name = "4")
    LivenessJpgFile,

    @Json(name = "5")
    IdCardRearJpgFile,

    @Json(name = "6")
    LivenessJpgBase64,

    @Json(name = "7")
    IdCardRearJpgBase64,
}
