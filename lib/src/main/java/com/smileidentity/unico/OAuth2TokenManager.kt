package com.smileidentity.unico

import android.content.Context
import android.util.Base64
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import java.io.BufferedReader
import java.io.InputStreamReader
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.spec.PKCS8EncodedKeySpec
import java.util.Date
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import timber.log.Timber

class OAuth2TokenManager(private val context: Context) {

    companion object {
        private const val TAG = "OAuth2TokenManager"
        private const val ISSUER = "smileid_uat@a41e6c5f-6c5e-4406-953b-035e828c925e.iam.acesso.io"
        private const val AUDIENCE = "https://identityhomolog.acesso.io"
        private const val TOKEN_URL = "https://identityhomolog.acesso.io/oauth2/token"
        private const val SCOPE = "*"
        private const val TOKEN_EXPIRY_SECONDS = 3600 // 1 hour
    }

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    /**
     * Loads the private key from assets and exchanges JWT for OAuth2 access token
     * @param privateKeyFileName The name of the private key file in assets folder
     * @return TokenResponse containing the access token details
     */
    suspend fun getAccessToken(privateKeyFileName: String = "private.key.pem"): TokenResponse =
        withContext(
            Dispatchers.IO,
        ) {
            try {
                // Load RSA private key from assets
                val privateKey = loadPrivateKey(privateKeyFileName)

                val jwtToken = generateJWT(privateKey)
                Timber.d("Generated JWT: $jwtToken")

                val tokenResponse = exchangeJWTForAccessToken(jwtToken)
                Timber.d("Access Token obtained successfully")
                Timber.d("  token_type: ${tokenResponse.tokenType}")
                Timber.d("  access_token: ${tokenResponse.accessToken}")
                Timber.d("  expires_in: ${tokenResponse.expiresIn}")

                tokenResponse
            } catch (e: Exception) {
                Timber.e("Error obtaining access token", e)
                throw e
            }
        }

    /**
     * Loads private key from assets folder
     */
    private fun loadPrivateKey(fileName: String): PrivateKey {
        val keyContent = context.assets.open(fileName).use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                reader.readText()
            }
        }

        // Remove PEM headers and decode
        val privateKeyPEM = keyContent
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replace("-----BEGIN RSA PRIVATE KEY-----", "")
            .replace("-----END RSA PRIVATE KEY-----", "")
            .replace("\\s".toRegex(), "")

        val keyBytes = Base64.decode(privateKeyPEM, Base64.DEFAULT)
        val keySpec = PKCS8EncodedKeySpec(keyBytes)
        val keyFactory = KeyFactory.getInstance("RSA")

        return keyFactory.generatePrivate(keySpec)
    }

    /**
     * Generates a signed JWT token
     */
    private fun generateJWT(privateKey: PrivateKey): String {
        val now = System.currentTimeMillis()
        val expiry = now + (TOKEN_EXPIRY_SECONDS * 1000)

        return Jwts.builder()
            .setIssuer(ISSUER)
            .setAudience(AUDIENCE)
            .claim("scope", SCOPE)
            .setIssuedAt(Date(now))
            .setExpiration(Date(expiry))
            .signWith(privateKey, SignatureAlgorithm.RS256)
            .compact()
    }

    /**
     * Exchanges JWT for OAuth2 access token
     */
    private fun exchangeJWTForAccessToken(jwtToken: String): TokenResponse {
        val formBody = FormBody.Builder()
            .add("grant_type", "urn:ietf:params:oauth:grant-type:jwt-bearer")
            .add("assertion", jwtToken)
            .build()

        val request = Request.Builder()
            .url(TOKEN_URL)
            .post(formBody)
            .addHeader("Content-Type", "application/x-www-form-urlencoded")
            .build()

        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw Exception("Unexpected response code: ${response.code}")
            }

            val responseBody = response.body?.string() ?: throw Exception("Empty response body")
            val jsonResponse = JSONObject(responseBody)

            return TokenResponse(
                tokenType = jsonResponse.getString("token_type"),
                accessToken = jsonResponse.getString("access_token"),
                expiresIn = jsonResponse.getInt("expires_in"),
            )
        }
    }
}
