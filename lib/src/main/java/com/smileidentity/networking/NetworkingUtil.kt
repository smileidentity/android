package com.smileidentity.networking

import com.smileidentity.SmileID
import com.smileidentity.SmileID.moshi
import com.smileidentity.models.ImageType
import com.smileidentity.models.SecurityInfo
import com.smileidentity.models.UploadImageInfo
import com.smileidentity.models.UploadRequest
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.util.zip.ZipEntry
import java.util.zip.ZipException
import java.util.zip.ZipOutputStream
import okhttp3.Request
import okio.ByteString.Companion.encode
import retrofit2.Invocation
import timber.log.Timber

fun <T : Annotation> Request.getCustomAnnotation(annotationClass: Class<T>): T? =
    this.tag(Invocation::class.java)?.method()?.getAnnotation(annotationClass)

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
    val zipOutputStream = try {
        ZipOutputStream(BufferedOutputStream(FileOutputStream(zipFile)))
    } catch (e: FileNotFoundException) {
        Timber.w(e, "Error creating zip file")
        // We save our Image captures inside a temp file. If the device is low on storage, then
        // the OS may silently delete the temp file. This will cause a FileNotFoundException
        // when we try to read the file. We throw a more user-friendly error message in this
        // case so that it can be handled by the app.
        // NB! It is also possible (albeit unlikely) that the user deleted the file
        throw IOException(
            "Your device is running low on storage. Please free up space and try again",
            e,
        )
    }

    // Write info.json
    zipOutputStream.putNextEntry(ZipEntry("info.json"))
    val infoJson = moshi.adapter(UploadRequest::class.java).toJson(uploadRequest)
    zipOutputStream.write(infoJson.toByteArray())
    zipOutputStream.closeEntry()

    // Write security_info.json
    zipOutputStream.putNextEntry(ZipEntry("security_info.json"))
    val securityInfoJson = moshi.adapter(UploadRequest::class.java).toJson(uploadRequest)
    zipOutputStream.write(securityInfoJson.toByteArray())
    zipOutputStream.closeEntry()

    // Write images
    uploadRequest.images.forEach { imageInfo ->
        zipOutputStream.putNextEntry(ZipEntry(imageInfo.image.name))
        try {
            imageInfo.image.inputStream().use { it.copyTo(zipOutputStream) }
        } catch (e: FileNotFoundException) {
            Timber.w(e, "Error reading image file ${imageInfo.image.name}")
            // We save our Image captures inside a temp file. If the device is low on storage, then
            // the OS may silently delete the temp file. This will cause a FileNotFoundException
            // when we try to read the file. We throw a more user-friendly error message in this
            // case so that it can be handled by the app.
            // NB! It is also possible (albeit unlikely) that the user deleted the file
            throw IOException(
                "Your device is running low on storage. Please free up space and try again",
                e,
            )
        }
        zipOutputStream.closeEntry()
    }

    zipOutputStream.close()
    zipFile.deleteOnExit()
    return zipFile
}

/**
 * This function takes an [UploadRequest] and returns a new [UploadRequest] with duplicate images
 * copied to a new File with a new name. This is necessary because we can't have duplicate entries
 * in a zip file.
 *
 * If we don't do this, then we will crash when attempting to include the file in the Zip with a
 * [ZipException] stating that the entry already exists
 */
private fun deDupedUploadRequest(uploadRequest: UploadRequest) = uploadRequest.copy(
    images = uploadRequest.images
        .groupBy { it.image.name }
        .flatMap { (fileName, images) ->
            if (images.size > 1) {
                images.mapIndexed { index, imageInfo ->
                    val fileNameWithoutExtension = fileName.substringBeforeLast(".")
                    val fileNameExtension = fileName.substringAfterLast(".")
                    val newFileName = "$fileNameWithoutExtension-$index.$fileNameExtension"
                    val newFile = File(SmileID.fileSavePath, newFileName)
                    imageInfo.image.copyTo(newFile, overwrite = true)
                    imageInfo.copy(image = newFile)
                }
            } else {
                images
            }
        },
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
