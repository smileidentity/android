@file:Suppress("unused")

package com.smileidentity.models

import com.smileidentity.BuildConfig
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.File

@JsonClass(generateAdapter = true)
data class UploadRequest(
    @Json(name = "images") val images: List<UploadImageInfo>,
    @Json(name = "id_info") val idInfo: IdInfo? = null,
    @Json(name = "package_information") val packageInfo: UploadPackageInfo = UploadPackageInfo(),
)

@JsonClass(generateAdapter = true)
data class UploadImageInfo(
    @Json(name = "image_type_id") val imageTypeId: ImageType,
    @Json(name = "file_name") val image: File,
)

/**
 * @param country The 2 letter country code of the user's ID (ISO 3166-1 alpha-2 format)
 * @param idType The ID type from the list [here](https://docs.smileidentity.com/supported-id-types/for-individuals-kyc/backed-by-id-authority)
 * @param idNumber The ID number of the user's ID
 * @param firstName The first name of the user
 * @param middleName The middle name of the user
 * @param lastName The last name of the user
 * @param dob The date of birth of the user in the **ID type specific format**
 * @param entered Whether to submit the verification to the ID authority or not. For Biometric KYC
 * jobs, this should be set to true
 */
@JsonClass(generateAdapter = true)
data class IdInfo(
    @Json(name = "country") val country: String,
    @Json(name = "id_type") val idType: String,
    @Json(name = "id_number") val idNumber: String? = null,
    @Json(name = "first_name") val firstName: String? = null,
    @Json(name = "middle_name") val middleName: String? = null,
    @Json(name = "last_name") val lastName: String? = null,
    @Json(name = "dob") val dob: String? = null,
    @Json(name = "entered") val entered: Boolean? = null,
)

enum class ImageType {
    @Json(name = "0")
    SelfiePngOrJpgFile,

    @Json(name = "1")
    IdCardPngOrJpgFile,

    @Json(name = "2")
    SelfiePngOrJpgBase64,

    @Json(name = "3")
    IdCardPngOrJpgBase64,

    @Json(name = "4")
    LivenessPngOrJpgFile,

    @Json(name = "5")
    IdCardRearPngOrJpgFile,

    @Json(name = "6")
    LivenessPngOrJpgBase64,

    @Json(name = "7")
    IdCardRearPngOrJpgBase64,
}

@JsonClass(generateAdapter = true)
data class UploadPackageInfo(
    @Json(name = "apiVersion") val apiVersion: ApiVersion = ApiVersion(),
    @Json(name = "version_names") val versionNames: VersionNames = VersionNames(),
)

@JsonClass(generateAdapter = true)
data class ApiVersion(
    @Json(name = "buildNumber") val buildNumber: Int = 2,
    @Json(name = "majorVersion") val majorVersion: Int = 2,
    @Json(name = "minorVersion") val minorVersion: Int = 1,
)

@JsonClass(generateAdapter = true)
data class VersionNames(
    @Json(name = "sid_sdk_version") val version: String = BuildConfig.VERSION_NAME,
    @Json(name = "sid_sdk_type") val type: String = "Android",
    @Json(name = "sid_sdk_ux_version") val uxVersion: String = "1.0",
)
