package com.smileidentity.networking

import com.smileidentity.SmileID
import com.smileidentity.models.SdkContext
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response

annotation class SmileIDSdkContextParameter

class SmileIDSdkContextInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request: Request = chain.request()
        request.getCustomAnnotation(SmileIDSdkContextParameter::class.java)
            ?: return chain.proceed(request)

        val sdkContext = SmileID.sdkContext ?: SdkContext(
            apiSubmissionOnly = true,
            componentMode = true,
        )
        val moshi = SmileID.moshi
        val adapter = moshi.adapter(SdkContext::class.java)
        val sdkContextJson = adapter.toJson(sdkContext)

        val originalBody = request.body
        val extendedBody = when (originalBody) {
            is okhttp3.MultipartBody -> {
                val builder = okhttp3.MultipartBody.Builder()
                    .setType(originalBody.type)
                for (i in 0 until originalBody.size) {
                    val part = originalBody.part(i)
                    builder.addPart(part)
                }
                builder.addFormDataPart("sdk_context", sdkContextJson)
                builder.build()
            }

            is okhttp3.RequestBody -> {
                val buffer = okio.Buffer()
                originalBody.writeTo(buffer)
                val originalContent = buffer.readUtf8()

                val jsonAdapter = moshi.adapter(Map::class.java)
                val originalMap = if (originalContent.isNotEmpty()) {
                    jsonAdapter.fromJson(originalContent) as? MutableMap<String, Any>
                        ?: mutableMapOf()
                } else {
                    mutableMapOf()
                }
                originalMap["sdk_context"] = sdkContext

                val newJson = jsonAdapter.toJson(originalMap)
                newJson
                    .toRequestBody(originalBody.contentType())
            }

            else -> originalBody
        }

        val newRequest = request.newBuilder()
            .method(request.method, extendedBody)
            .build()
        return chain.proceed(newRequest)
    }
}
