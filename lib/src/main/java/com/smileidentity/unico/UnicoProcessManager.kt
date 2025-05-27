package com.smileidentity.unico

import android.content.Context
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import timber.log.Timber

class UnicoProcessManager(private val context: Context) {

    companion object {
        private const val TAG = "UnicoProcessManager"
        private const val BASE_URL = "https://api.cadastro.uat.unico.app"
        private const val CREATE_PROCESS_ENDPOINT = "/client/v1/process"

        // DUI Types
        const val DUI_TYPE_US_SSN = "DUI_TYPE_US_SSN"
        const val DUI_TYPE_BR_CPF = "DUI_TYPE_BR_CPF"
        const val DUI_TYPE_PASSPORT = "DUI_TYPE_PASSPORT"

        // Flow Types
        const val FLOW_ID_LIVE_TRUST = "idlivetrust"
        const val FLOW_ID_VERIFICATION = "idverification"

        // Purpose Types
        const val PURPOSE_CREDIT_PROCESS = "creditprocess"
        const val PURPOSE_ONBOARDING = "onboarding"
        const val PURPOSE_VERIFICATION = "verification"
    }

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val tokenManager = OAuth2TokenManager(context)

    suspend fun createProcess(
        callbackUri: String,
        flow: String = FLOW_ID_LIVE_TRUST,
        duiType: String,
        duiValue: String,
        friendlyName: String,
        purpose: String = PURPOSE_CREDIT_PROCESS,
    ): CreateProcessResponse = withContext(Dispatchers.IO) {
        try {
            val tokenResponse = tokenManager.getAccessToken()

            val requestBody = JSONObject().apply {
                put("callbackUri", callbackUri)
                put("flow", flow)
                put(
                    "person",
                    JSONObject().apply {
                        put("duiType", duiType)
                        put("duiValue", duiValue)
                        put("person.friendlyName", friendlyName)
                    },
                )
                put("purpose", purpose)
                put(
                    "flowConfig",
                    JSONObject().apply {
                        put(
                            "biometryCapture",
                            JSONObject().apply {
                                put("enabledBackCamera", true)
                            },
                        )
                    },
                )
            }

            Timber.d("Creating process with request: $requestBody")

            val request = Request.Builder()
                .url("$BASE_URL$CREATE_PROCESS_ENDPOINT")
                .post(requestBody.toString().toRequestBody("application/json".toMediaTypeOrNull()))
                .addHeader("Authorization", "Bearer ${tokenResponse.accessToken}")
                .addHeader("Content-Type", "application/json")
                .build()

            httpClient.newCall(request).execute().use { response ->
                val responseBody = response.body?.string() ?: throw Exception("Empty response")
                Timber.d("Response code: ${response.code}")
                Timber.d("Response body: $responseBody")

                if (!response.isSuccessful) {
                    throw Exception("API call failed with code: ${response.code}, body: $responseBody")
                }

                val jsonResponse = JSONObject(responseBody)
                val processObject = jsonResponse.getJSONObject("process")

                CreateProcessResponse(
                    processId = processObject.getString("id"),
                    status = processObject.optString("state", "PROCESS_STATE_CREATED"),
                    webLink = processObject.optString("userRedirectUrl"),
                    createdAt = processObject.optString("createdAt"),
                )
            }
        } catch (e: Exception) {
            Timber.e("Error creating process", e)
            throw e
        }
    }

    suspend fun getProcess(processId: String): JSONObject = withContext(Dispatchers.IO) {
        try {
            val tokenResponse = tokenManager.getAccessToken()

            val request = Request.Builder()
                .url("$BASE_URL$CREATE_PROCESS_ENDPOINT/$processId")
                .get()
                .addHeader("Authorization", "Bearer ${tokenResponse.accessToken}")
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw Exception("Failed to get process: ${response.code}")
                }

                val responseBody = response.body?.string() ?: throw Exception("Empty response")
                JSONObject(responseBody)
            }
        } catch (e: Exception) {
            Timber.e("Error getting process", e)
            throw e
        }
    }
}
