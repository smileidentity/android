package com.smileidentity.attestation

import android.content.Context
import com.google.android.gms.tasks.Task
import com.google.android.play.core.integrity.StandardIntegrityManager
import com.google.android.play.core.integrity.StandardIntegrityManager.PrepareIntegrityTokenRequest
import com.google.android.play.core.integrity.StandardIntegrityManager.StandardIntegrityTokenProvider
import com.google.android.play.core.integrity.StandardIntegrityManager.StandardIntegrityTokenRequest
import com.smileidentity.security.arkana.ArkanaKeys
import timber.log.Timber

interface SmileIDIntegrityManager {
    /**
     * Prepare the integrity token.Needs to be called before calling [requestToken].
     */
    suspend fun warmUpTokenProvider(): Result<Unit>

    /**
     * Requests an Integrity token.
     *
     * @param requestHash A string to be hashed to generate a request identifier.
     *
     */
    suspend fun requestToken(requestHash: String): Result<String>
}

class SmileIDStandardRequestIntegrityManager(context: Context) : SmileIDIntegrityManager {

    private val smileIDStandardIntegrityManagerFactory: SmileIDIntegrityManagerFactory =
        SmileIDStandardIntegrityManagerFactory(context)
    private val standardIntegrityManager: StandardIntegrityManager by lazy {
        smileIDStandardIntegrityManagerFactory.create()
    }
    private var integrityTokenProvider:
        StandardIntegrityTokenProvider? = null

    override suspend fun warmUpTokenProvider(): Result<Unit> = runCatching {
        if (integrityTokenProvider != null) {
            return Result.success(Unit)
        }
        val finishedTask: Task<StandardIntegrityTokenProvider> = standardIntegrityManager
            .prepareIntegrityToken(
                PrepareIntegrityTokenRequest.builder()
                    .setCloudProjectNumber(
                        ArkanaKeys.Global.gOOGLE_CLOUD_PROJECT_NUMBER.toLong(),
                    )
                    .build(),
            ).awaitTask()
        finishedTask.toResult()
            .onSuccess {
                Timber.i("Successfully prepared integrity token")
                integrityTokenProvider = it
            }
            .getOrThrow()
    }
        .map {}
        .recoverCatching {
            Timber.w(it, "Failed to prepare integrity token")
            throw it
        }

    override suspend fun requestToken(requestHash: String): Result<String> =
        request(requestHash)

    private suspend fun request(requestHash: String): Result<String> = runCatching {
        val provider = integrityTokenProvider
            ?: throw IllegalStateException("Integrity token provider is not initialized. Call warmUpTokenProvider() first.")

        val finishedTask = provider.request(
            StandardIntegrityTokenRequest.builder()
                .setRequestHash(requestHash)
                .build()
        ).awaitTask()

        finishedTask.toResult().getOrThrow()
    }.map { it.token() }
        .recoverCatching {
            Timber.w(it, "Failed to request integrity token")
            throw it
        }
}
