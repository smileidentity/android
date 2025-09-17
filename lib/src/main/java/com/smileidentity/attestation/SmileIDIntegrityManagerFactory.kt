package com.smileidentity.attestation

import android.content.Context
import com.google.android.play.core.integrity.IntegrityManagerFactory
import com.google.android.play.core.integrity.StandardIntegrityManager

/**
 * Factory for creating a [StandardIntegrityManager]. This is used to allow for easier testing.
 *
 */
interface SmileIDIntegrityManagerFactory {
    fun create(): StandardIntegrityManager
}

class SmileIDStandardIntegrityManagerFactory(private val context: Context) :
    SmileIDIntegrityManagerFactory {
    override fun create(): StandardIntegrityManager =
        IntegrityManagerFactory.createStandard(context)
}
