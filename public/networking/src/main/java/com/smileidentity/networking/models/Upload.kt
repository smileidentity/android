@file:Suppress("unused")

package com.smileidentity.networking.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.File

@JsonClass(generateAdapter = true)
data class UploadRequest(
    @Json(name = "images") val images: List<UploadImageInfo>,
    @Json(name = "package_information") val packageInfo: UploadPackageInfo = UploadPackageInfo(),
)

@JsonClass(generateAdapter = true)
data class UploadImageInfo(
    @Json(name = "image_type_id") val imageTypeId: ImageType,
    @Json(name = "file_name") val image: File,
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
    // TODO: Grab the version once build.gradle is updated with versioning
    @Json(name = "sid_sdk_version") val version: String = "2.0.0",
    @Json(name = "sid_sdk_type") val type: String = "Android",
    @Json(name = "sid_sdk_ux_version") val uxVersion: String = "1.0",
)
