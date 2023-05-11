package com.smileidentity.datasource

import android.content.ContentValues
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import java.io.File
import java.util.UUID

class FileDataSource {

    // The path to the directory in the external storage where the files are stored.
    private val externalDir = "${Environment.DIRECTORY_DCIM}${File.separator}$RELATIVE_PATH"

    // Generates a file name based on the current timestamp and a random UUID.
    private val currentFileName: String
        get() = "${System.currentTimeMillis()}-${UUID.randomUUID()}"

    // Gets the public external storage directory where the image files are stored.
    private val externalStorage
        get() = Environment.getExternalStoragePublicDirectory(externalDir).apply { mkdirs() }

    // Gets a list of files in the external storage directory, sorted by last modified time in descending order.
    val externalFiles
        get() = externalStorage.listFiles()?.sortedByDescending { it.lastModified() }

    // Gets the last picture taken, if any.
    val lastPicture get() = externalFiles?.firstOrNull()

    /**
     * Creates a new file with the given extension in the external storage directory.
     * @param extension The file extension, e.g. "jpg".
     * @return The newly created file.
     */
    fun getFile(
        extension: String = "jpg",
    ): File = File(externalStorage.path, "$currentFileName.$extension").apply {
        if (parentFile?.exists() == false) parentFile?.mkdirs()
        createNewFile()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    val imageContentValues: ContentValues = getContentValues(JPEG_MIME_TYPE)

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun getContentValues(mimeType: String) = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, currentFileName)
        put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
        put(MediaStore.MediaColumns.RELATIVE_PATH, externalDir)
    }

    companion object {
        private const val JPEG_MIME_TYPE = "image/jpeg"
        private const val RELATIVE_PATH = "SmileID"
    }
}
