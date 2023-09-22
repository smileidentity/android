package com.smileidentity.networking

import com.smileidentity.models.BiometricKycJobResult
import com.smileidentity.models.DocumentVerificationJobResult
import com.smileidentity.models.JobResult
import com.smileidentity.models.JobType
import com.smileidentity.models.PartnerParams
import com.smileidentity.models.SmartSelfieJobResult
import com.smileidentity.models.UploadRequest
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

@Suppress("unused")
object JobTypeAdapter {
    @ToJson
    fun toJson(jobType: JobType): Int = jobType.value

    @FromJson
    fun fromJson(value: Int) = JobType.fromValue(value)
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
            extras = paramsJson - setOf("job_id", "user_id", "job_type"),
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

@Suppress("unused")
object JobResultAdapter {
    @FromJson
    fun fromJson(@Suppress("UNUSED_PARAMETER") result: JobResult): JobResult =
        throw NotImplementedError("Unable to determine JobResult type solely from network response")

    @ToJson
    fun toJson(
        writer: JsonWriter,
        result: JobResult,
        smartSelfieDelegate: JsonAdapter<SmartSelfieJobResult>,
        documentVerificationDelegate: JsonAdapter<DocumentVerificationJobResult>,
        biometricKycDelegate: JsonAdapter<BiometricKycJobResult>,
    ) {
        when (result) {
            is JobResult.Freeform -> writer.value(result.result)
            is SmartSelfieJobResult -> smartSelfieDelegate.toJson(writer, result)
            is DocumentVerificationJobResult -> documentVerificationDelegate.toJson(writer, result)
            is BiometricKycJobResult -> biometricKycDelegate.toJson(writer, result)
            else -> throw NotImplementedError("Unknown JobResult type: $result")
        }
    }
}

@Suppress("unused")
object SmartSelfieJobResultAdapter {
    @FromJson
    fun fromJson(
        reader: JsonReader,
        delegate: JsonAdapter<SmartSelfieJobResult.Entry>,
    ) = if (reader.peek() == JsonReader.Token.BEGIN_OBJECT) {
        delegate.fromJson(reader)!!
    } else {
        JobResult.Freeform(reader.nextString())
    }

    @ToJson
    fun toJson(
        writer: JsonWriter,
        result: SmartSelfieJobResult,
        delegate: JsonAdapter<SmartSelfieJobResult.Entry>,
    ) {
        when (result) {
            is JobResult.Freeform -> writer.value(result.result)
            is SmartSelfieJobResult.Entry -> delegate.toJson(writer, result)
        }
    }
}

@Suppress("unused")
object DocumentVerificationJobResultAdapter {
    @FromJson
    fun fromJson(
        reader: JsonReader,
        delegate: JsonAdapter<DocumentVerificationJobResult.Entry>,
    ) = if (reader.peek() == JsonReader.Token.BEGIN_OBJECT) {
        delegate.fromJson(reader)!!
    } else {
        JobResult.Freeform(reader.nextString())
    }

    @ToJson
    fun toJson(
        writer: JsonWriter,
        result: DocumentVerificationJobResult,
        delegate: JsonAdapter<DocumentVerificationJobResult.Entry>,
    ) {
        when (result) {
            is JobResult.Freeform -> writer.value(result.result)
            is DocumentVerificationJobResult.Entry -> delegate.toJson(writer, result)
        }
    }
}

@Suppress("unused")
object BiometricKycJobResultAdapter {
    @FromJson
    fun fromJson(
        reader: JsonReader,
        delegate: JsonAdapter<BiometricKycJobResult.Entry>,
    ) = if (reader.peek() == JsonReader.Token.BEGIN_OBJECT) {
        delegate.fromJson(reader)!!
    } else {
        JobResult.Freeform(reader.nextString())
    }

    @ToJson
    fun toJson(
        writer: JsonWriter,
        result: BiometricKycJobResult,
        delegate: JsonAdapter<BiometricKycJobResult.Entry>,
    ) {
        when (result) {
            is JobResult.Freeform -> writer.value(result.result)
            is BiometricKycJobResult.Entry -> delegate.toJson(writer, result)
        }
    }
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
