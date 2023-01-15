package com.smileidentity.networking

import com.smileidentity.networking.models.SmileIdentityResponse
import com.smileidentity.networking.models.UploadRequest
import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonQualifier
import com.squareup.moshi.ToJson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Callback
import retrofit2.Converter
import retrofit2.Response
import retrofit2.Retrofit
import java.io.File
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

class SmileIdentityCall<T>(private val call: Call<T>) : Call<T> by call {
    fun enqueue(callback: (SmileIdentityResponse<T>) -> Unit) {
        call.enqueue(object : Callback<T> {
            override fun onResponse(call: Call<T>, response: Response<T>) {
                if (response.isSuccessful) {
                    try {
                        callback(SmileIdentityResponse.Success(response.body()!!))
                    } catch (e: Exception) {
                        // Most likely a JSON deserialization error
                        e.printStackTrace()
                        callback(SmileIdentityResponse.Failure(e))
                    }
                } else {
                    val rawBody = response.errorBody()?.string() ?: "Unknown error"
                    val defaultError = SmileIdentityResponse.ServerError(response.code(), rawBody)
                    try {
                        val parsedBody = SmileIdentity.moshi
                            .adapter(SmileIdentityResponse.ServerError::class.java)
                            .nullSafe()
                            .lenient()
                            .fromJson(rawBody)
                        callback(parsedBody ?: defaultError)
                    } catch (e: Exception) {
                        // Most likely a JSON deserialization error. We have 2 errors now:
                        // ServerError and e. The more relevant one to the consumer will be the
                        // ServerError, but we lose track of e, then. Let's leave some form of
                        // breadcrumbs for this error by at least printing a stack trace
                        e.printStackTrace()
                        callback(defaultError)
                    }
                }
            }

            override fun onFailure(call: Call<T>, t: Throwable) {
                t.printStackTrace()
                callback(SmileIdentityResponse.Failure(t))
            }
        })
    }
}

class SmileIdentityCallAdapter<T>(private val responseType: Type): CallAdapter<T, SmileIdentityCall<T>> {
    override fun responseType() = responseType
    override fun adapt(call: Call<T>) = SmileIdentityCall(call)
}

object SmileIdentityCallAdapterFactory : CallAdapter.Factory() {
    override fun get(
        returnType: Type,
        annotations: Array<Annotation>,
        retrofit: Retrofit
    ): CallAdapter<*, *>? {
        return if (getRawType(returnType) == SmileIdentityCall::class.java) {
            val responseType = getParameterUpperBound(0, returnType as ParameterizedType)
            SmileIdentityCallAdapter<Any>(responseType)
        } else {
            null
        }
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
