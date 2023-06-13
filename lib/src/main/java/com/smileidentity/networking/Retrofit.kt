package com.smileidentity.networking

import com.smileidentity.models.JobResult
import com.smileidentity.models.JobType
import com.smileidentity.models.UploadRequest
import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonQualifier
import com.squareup.moshi.JsonReader
import com.squareup.moshi.ToJson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.io.File
import java.lang.reflect.Type

@Suppress("unused")
object JobTypeAdapter {
    @ToJson
    fun toJson(jobType: JobType): Int = jobType.value

    @FromJson
    fun fromJson(value: Int) = JobType.fromValue(value)
}

object UploadRequestConverterFactory : Converter.Factory() {
    override fun requestBodyConverter(
        type: Type,
        parameterAnnotations: Array<out Annotation>,
        methodAnnotations: Array<out Annotation>,
        retrofit: Retrofit,
    ): Converter<*, RequestBody>? {
        if (type != UploadRequest::class.java) {
            return null
        }
        return Converter<UploadRequest, RequestBody> {
            it.zip().asRequestBody("application/zip".toMediaType())
        }
    }
}

@Suppress("unused", "UNUSED_PARAMETER")
object FileAdapter {
    @ToJson
    fun toJson(file: File): String = file.name

    @FromJson
    fun fromJson(fileName: String): File = throw NotImplementedError()
}

@Suppress("unused", "UNUSED_PARAMETER")
object JobResultAdapter {
    @FromJson
    fun fromJson(reader: JsonReader, delegate: JsonAdapter<JobResult.Entry>): JobResult {
        if (reader.peek() == JsonReader.Token.BEGIN_OBJECT) {
            return delegate.fromJson(reader)!!
        }
        return JobResult.Freeform(reader.nextString())
    }

    @ToJson
    fun toJson(result: JobResult): String = throw NotImplementedError()
}

@JsonQualifier
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FUNCTION)
annotation class StringifiedBoolean

@Suppress("unused")
object StringifiedBooleanAdapter {
    @ToJson
    fun toJson(@StringifiedBoolean value: Boolean): String = value.toString()

    @FromJson
    @StringifiedBoolean
    fun fromJson(value: String): Boolean = value.toBoolean()
}
