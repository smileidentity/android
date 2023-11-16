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
import java.util.zip.ZipException
import java.util.zip.ZipOutputStream

fun calculateSignature(timestamp: String): String {
    val apiKey = SmileID.apiKey ?: throw IllegalStateException(
        """API key not set. If using the authToken from smile_config.json, ensure you have set the 
        |signature/timestamp properties on the request from the values returned by 
        |SmileID.authenticate.signature/timestamp
        """.trimMargin().replace("\n", ""),
    )
    val hashContent = timestamp + SmileID.config.partnerId + "sid_request"
    return hashContent.encode().hmacSha256(apiKey.encode()).base64()
}

fun UploadRequest.zip(): File {
    val uploadRequest = deDupedUploadRequest(this)
    val zipFile = File.createTempFile("upload", ".zip")
    val zipOutputStream = ZipOutputStream(BufferedOutputStream(FileOutputStream(zipFile)))

    // Write info.json
    zipOutputStream.putNextEntry(ZipEntry("info.json"))
    zipOutputStream.write(moshi.adapter(UploadRequest::class.java).toJson(uploadRequest).toByteArray())
    zipOutputStream.closeEntry()

    // Write images
    uploadRequest.images.forEach { imageInfo ->
        zipOutputStream.putNextEntry(ZipEntry(imageInfo.image.name))
        imageInfo.image.inputStream().use { it.copyTo(zipOutputStream) }
        zipOutputStream.closeEntry()
    }

    zipOutputStream.close()
    zipFile.deleteOnExit()
    return zipFile
}

/**
 * This function takes an [UploadRequest] and returns a new [UploadRequest] with duplicate images
 * removed. This is necessary because we can't have duplicate entries in a zip file. There is no
 * valid use-case where we would want to upload the same image twice. The only scenario where that
 * could happen is if the user selects the same image for both the front and back of a document.
 *
 * If we don't do this, then we will crash when attempting to include the file in the Zip with a
 * [ZipException] stating that the entry already exists
 */
private fun deDupedUploadRequest(uploadRequest: UploadRequest) = uploadRequest.copy(
    images = uploadRequest.images.distinctBy { it.image.name },
)

fun File.asSelfieImage() = UploadImageInfo(
    imageTypeId = ImageType.SelfieJpgFile,
    image = this,
)

fun File.asLivenessImage() = UploadImageInfo(
    imageTypeId = ImageType.LivenessJpgFile,
    image = this,
)

fun File.asDocumentFrontImage() = UploadImageInfo(
    imageTypeId = ImageType.IdCardJpgFile,
    image = this,
)

fun File.asDocumentBackImage() = UploadImageInfo(
    imageTypeId = ImageType.IdCardRearJpgFile,
    image = this,
)
