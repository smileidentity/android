package com.smileidentity.networking

import com.smileidentity.SmileID
import com.smileidentity.SmileID.moshi
import com.smileidentity.models.ImageType
import com.smileidentity.models.UploadImageInfo
import com.smileidentity.models.UploadRequest
import okio.ByteString.Companion.encode
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

fun calculateSignature(timestamp: String): String {
    val apiKey = SmileID.apiKey ?: throw IllegalStateException("API key not set")
    val hashContent = timestamp + SmileID.config.partnerId + "sid_request"
    return hashContent.encode().hmacSha256(apiKey.encode()).base64()
}

fun UploadRequest.zip(): File {
    val zipFile = File.createTempFile("upload", ".zip")
    val zipOutputStream = ZipOutputStream(BufferedOutputStream(FileOutputStream(zipFile)))

    // Write info.json
    zipOutputStream.putNextEntry(ZipEntry("info.json"))
    zipOutputStream.write(moshi.adapter(UploadRequest::class.java).toJson(this).toByteArray())
    zipOutputStream.closeEntry()

    // Write images
    for (imageInfo in images) {
        zipOutputStream.putNextEntry(ZipEntry(imageInfo.image.name))
        imageInfo.image.inputStream().use { it.copyTo(zipOutputStream) }
        zipOutputStream.closeEntry()
    }

    zipOutputStream.close()
    zipFile.deleteOnExit()
    return zipFile
}

fun File.asSelfieImage() = UploadImageInfo(
    imageTypeId = ImageType.SelfieJpgFile,
    image = this,
)

fun File.asLivenessImage() = UploadImageInfo(
    imageTypeId = ImageType.LivenessJpgFile,
    image = this,
)

fun File.asDocumentImage() = UploadImageInfo(
    imageTypeId = ImageType.IdCardJpgFile,
    image = this,
)
