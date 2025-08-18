package com.smileidentity.networking.api

import com.smileidentity.networking.models.AuthenticationRequest
import com.smileidentity.networking.models.AuthenticationResponse
import com.smileidentity.networking.models.ProductsConfigRequest
import com.smileidentity.networking.models.ProductsConfigResponse
import com.smileidentity.networking.models.ServicesResponse
import com.smileidentity.networking.models.ValidDocumentsResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface SmileAPI {

    /**
     * Returns a signature and timestamp that can be used to authenticate future requests. This is
     * necessary only when using the authToken and *not* using the API key.
     */
    @POST("/v1/auth_smile")
    suspend fun authenticate(@Body request: AuthenticationRequest): AuthenticationResponse

    /**
     * Returns supported products and metadata
     */
    @GET("/v1/services")
    suspend fun getServices(): ServicesResponse

    /**
     * Returns the ID types that are enabled for authenticated partner and which of those require
     * consent
     */
    @POST("/v1/products_config")
    suspend fun getProductsConfig(@Body request: ProductsConfigRequest): ProductsConfigResponse

    /**
     * Returns Global DocV supported products and metadata
     */
    @POST("/v1/valid_documents")
    suspend fun getValidDocuments(@Body request: ProductsConfigRequest): ValidDocumentsResponse
}
