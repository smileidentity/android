package com.smileidentity.sample

import android.content.Context
import com.google.android.play.core.integrity.IntegrityManagerFactory
import com.google.android.play.core.integrity.StandardIntegrityManager
import timber.log.Timber

class SmileIntegrityManager(private val appContext: Context) {
    private var appIntegrityTokenProvider: StandardIntegrityManager.StandardIntegrityTokenProvider? =
        null

    /**
     * This function is needed in case of standard verdict request by [requestIntegrityVerdictToken].
     * It must be called once at the initialisation because it can take a long time (up to several minutes)
     */
    fun warmUpTokenProvider() {
        val integrityManager = IntegrityManagerFactory.createStandard(appContext)
        integrityManager.prepareIntegrityToken(
            StandardIntegrityManager.PrepareIntegrityTokenRequest.builder()
                .setCloudProjectNumber(BuildConfig.GOOGLE_CLOUD_PROJECT_NUMBER.toLong())
                .build(),
        ).addOnSuccessListener { tokenProvider ->
            appIntegrityTokenProvider = tokenProvider
        }.addOnFailureListener {
            Timber.e(it, "Failed to prepare integrity token")
        }
    }

    /**
     * Standard verdict request for Integrity token
     */
    fun requestIntegrityVerdictToken(onSuccess: (String) -> Unit) {
        if (appIntegrityTokenProvider == null) {
            Timber.e(
                "Integrity token provider is null during a verdict request. This should not be possible",
            )
        } else {
            appIntegrityTokenProvider?.request(
                StandardIntegrityManager.StandardIntegrityTokenRequest.builder()
                    .setRequestHash("aaaas")
                    .build(),
            )
                ?.addOnSuccessListener { response ->
                    onSuccess(response.token())
                }
                ?.addOnFailureListener {
                    Timber.e(it, "Failed to request integrity token")
                }
        }
    }
}
