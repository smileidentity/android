@file:Suppress("unused")

package com.smileidentity.models

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.File
import kotlinx.parcelize.Parcelize

/**
 * This class represents info.json
 */
@JsonClass(generateAdapter = true)
data class UploadRequest(
    @Json(name = "images") val images: List<UploadImageInfo>,
    @Json(name = "id_info") val idInfo: IdInfo? = null,
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
