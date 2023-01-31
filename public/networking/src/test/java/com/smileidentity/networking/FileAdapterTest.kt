package com.smileidentity.networking

import com.squareup.moshi.Moshi
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File

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
}
