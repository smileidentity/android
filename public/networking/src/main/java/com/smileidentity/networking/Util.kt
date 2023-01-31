package com.smileidentity.networking

import com.smileidentity.networking.SmileIdentity.moshi
import com.smileidentity.networking.models.ImageType
import com.smileidentity.networking.models.UploadImageInfo
import com.smileidentity.networking.models.UploadRequest
import okio.ByteString.Companion.encode
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

fun calculateSignature(timestamp: String): String {
    val apiKey = SmileIdentity.apiKey ?: throw IllegalStateException("API key not set")
    val hashContent = timestamp + SmileIdentity.config.partnerId + "sid_request"
    return hashContent.encode().hmacSha256(apiKey.encode()).base64()
}

fun UploadRequest.zip(): File {
    val zipFile = File.createTempFile("upload", ".zip")
    val zipOutputStream = ZipOutputStream(BufferedOutputStream(FileOutputStream(zipFile)))
    // TODO: It might be more performant to use Okio
    // val zipOutputStream = ZipOutputStream(zipFile.sink().buffer().outputStream())

    // Write info.json
    zipOutputStream.putNextEntry(ZipEntry("info.json"))
    zipOutputStream.write(moshi.adapter(UploadRequest::class.java).toJson(this).toByteArray())
    zipOutputStream.closeEntry()

    // Write images
    this.images.forEach { imageInfo ->
        zipOutputStream.putNextEntry(ZipEntry(imageInfo.image.name))
        imageInfo.image.inputStream().use { it.copyTo(zipOutputStream) }
        zipOutputStream.closeEntry()
    }

    zipOutputStream.close()
    zipFile.deleteOnExit()
    return zipFile
}

fun File.asSelfieImage() = UploadImageInfo(
    imageTypeId = ImageType.SelfiePngOrJpgFile,
    image = this,
)

fun File.asLivenessImage() = UploadImageInfo(
    imageTypeId = ImageType.LivenessPngOrJpgFile,
    image = this,
)
