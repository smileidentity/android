package com.smileidentity.attestation

import android.content.Context
import com.google.android.gms.tasks.Task
import com.google.android.play.core.integrity.IntegrityManagerFactory
import com.google.android.play.core.integrity.StandardIntegrityManager
import com.google.android.play.core.integrity.StandardIntegrityManager.PrepareIntegrityTokenRequest
import com.google.android.play.core.integrity.StandardIntegrityManager.StandardIntegrityTokenProvider
import com.google.android.play.core.integrity.StandardIntegrityManager.StandardIntegrityTokenRequest
import com.smileidentity.security.arkana.ArkanaKeys
import timber.log.Timber

interface SmileIDIntegrityManager {
    /**
     * Prepare the integrity token. This warms up the integrity token generation, it's recommended
     * to call it as soon as possible if you know you will need an integrity token.
     *
     * Needs to be called before calling [requestToken].
     */
    suspend fun warmUpTokenProvider(): Result<Unit>

    /**
     * Requests an Integrity token.
     *
     * @param requestIdentifier A string to be hashed to generate a request identifier.
     * Can be null.
     *
     */
    suspend fun requestToken(
        requestIdentifier: String? = null
    ): Result<String>
}

class SmileIDStandardRequestIntegrityManager(
    context: Context
): SmileIDIntegrityManager {
    private val standardIntegrityManager = IntegrityManagerFactory.createStandard(context)
    private var integrityTokenProvider:
        StandardIntegrityTokenProvider? = null

    override suspend fun warmUpTokenProvider() = runCatching {
        if (integrityTokenProvider != null) {
            return Result.success(Unit)
        }

        val finishedTask: Task<StandardIntegrityTokenProvider> = standardIntegrityManager
            .prepareIntegrityToken(
                PrepareIntegrityTokenRequest.builder()
                    .setCloudProjectNumber(0L)
                    .build()
            ).awaitTask()

        finishedTask.toResult()
            .onSuccess {
                Timber.i("Integrity - Successfully prepared integrity token")
                integrityTokenProvider = it
            }
            .getOrThrow()
    }
        .map {}
        .recoverCatching {
            Timber.w(it, "Integrity - Failed to prepare integrity token")
            throw it
    }

    override suspend fun requestToken(
        requestIdentifier: String?
    ): Result<String> = request(requestIdentifier)

    private suspend fun request(
        requestHash: String?,
    ): Result<String> = runCatching {
        val finishedTask = requireNotNull(
            value = integrityTokenProvider,
            lazyMessage = { "Integrity token provider is not initialized. Call warmUpTokenProvider() first." }
        ).request(
            StandardIntegrityTokenRequest.builder()
                .setRequestHash(requestHash)
                .build()
        ).awaitTask()

        finishedTask.toResult().getOrThrow()
    }.map { it.token() }
        .recoverCatching {
            Timber.w(it, "Integrity - Failed to request integrity token")
            throw it
        }
}
