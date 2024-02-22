package com.smileidentity.networking

import com.squareup.moshi.Moshi
import java.io.File
import okhttp3.MediaType.Companion.toMediaType
import org.junit.Assert.assertEquals
import org.junit.Test

class FileAdapterTest {
    private val adapter = Moshi.Builder()
        .add(FileAdapter)
        .build()
        .adapter(File::class.java)!!

    @Test
    fun `file should be serialized to string`() {
        // given
        val file = File("path/to/file")

        // when
        val jsonString = adapter.toJson(file)

        // then
        assertEquals("\"file\"", jsonString)
    }

    @Test
    fun `file should be converted to MultipartBody Part`() {
        // given
        val file = File("path/to/file.jpg")

        // when
        val part = file.asFormDataPart("partName", mediaType = "image/jpeg")

        // then
        val headers = part.headers
        requireNotNull(headers)
        assert("name=\"partName\"" in headers["Content-Disposition"]!!)
        assert("filename=\"file.jpg\"" in headers["Content-Disposition"]!!)
        assert("image/jpeg".toMediaType() == part.body.contentType())
    }

    @Test
    fun `files should be converted to MultipartBody Parts`() {
        // given
        val files = listOf(
            File("path/to/file1.jpg"),
            File("path/to/file2.jpg"),
            File("path/to/file3.jpg"),
        )

        // when
        val parts = files.asFormDataParts("partName", mediaType = "image/jpeg")

        // then
        assertEquals(3, parts.size)
        parts.forEach { part ->
            val headers = part.headers
            requireNotNull(headers)
            assert("name=\"partName\"" in headers["Content-Disposition"]!!)
            assert("filename=" in headers["Content-Disposition"]!!)
            assert("image/jpeg".toMediaType() == part.body.contentType())
        }
    }
}
