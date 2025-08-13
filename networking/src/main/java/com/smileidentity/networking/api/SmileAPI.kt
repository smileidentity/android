package com.smileidentity.networking.api

import com.smileidentity.networking.models.AuthenticationRequest
import com.smileidentity.networking.models.AuthenticationResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface SmileAPI {

    /**
     * Returns a signature and timestamp that can be used to authenticate future requests. This is
     * necessary only when using the authToken and *not* using the API key.
     */
    @POST("/v1/auth_smile")
    suspend fun authenticate(@Body request: AuthenticationRequest): AuthenticationResponse
}
