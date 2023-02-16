package com.smileidentity.networking

import com.smileidentity.networking.models.JobResult
import com.smileidentity.networking.models.JobType
import com.smileidentity.networking.models.PartnerParams
import com.smileidentity.networking.models.UploadRequest
import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonQualifier
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.ToJson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.io.File
import java.lang.reflect.Type

object JobTypeAdapter {
    @ToJson
    fun toJson(jobType: JobType): Int = jobType.value

    @FromJson
    fun fromJson(value: Int): JobType? = JobType.fromValue(value)
}

@Suppress("unused")
object PartnerParamsAdapter {
    @ToJson
    fun toJson(
        writer: JsonWriter,
        partnerParams: PartnerParams,
        mapDelegate: JsonAdapter<Map<String, Any>>,
        jobTypeDelegate: JsonAdapter<JobType>,
    ) {
        val map = partnerParams.extras + mapOf(
            "job_id" to partnerParams.jobId,
            "user_id" to partnerParams.userId,
            "job_type" to jobTypeDelegate.toJsonValue(partnerParams.jobType) as Long,
        )
        mapDelegate.toJson(writer, map)
    }

    @FromJson
    fun fromJson(
        jsonReader: JsonReader,
        mapDelegate: JsonAdapter<Map<String, String>>,
        jobTypeDelegate: JsonAdapter<JobType>,
    ): PartnerParams {
        val paramsJson = mapDelegate.fromJson(jsonReader) ?: mapOf()
        return PartnerParams(
            jobType = jobTypeDelegate.fromJsonValue(paramsJson["job_type"]),
            jobId = paramsJson["job_id"]!!,
            userId = paramsJson["user_id"]!!,
            extras = paramsJson - listOf("job_id", "user_id", "job_type"),
        )
    }
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
