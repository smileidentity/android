package com.smileidentity.metadata.updaters

import android.content.Context
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyInfo
import android.security.keystore.KeyProperties
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.LifecycleOwner
import com.smileidentity.metadata.models.MetadataKey
import com.smileidentity.metadata.models.Metadatum
import com.smileidentity.metadata.updateOrAddBy
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.KeyStore
import kotlinx.coroutines.launch

/**
 * A manager that updates metadata with attestation information.
 */
internal class AttestationMetadata(
    context: Context,
    private val metadata: SnapshotStateList<Metadatum>,
) : MetadataInterface {

    override val metadataName: String = MetadataKey.SupportsHardwareAttestation.key
    private var supportsHardwareAttestation: Int = -2

    override fun onStart(owner: LifecycleOwner) {
        kotlinx.coroutines.MainScope().launch {
            supportsHardwareAttestation = supportsHardwareAttestation()
            forceUpdate()
        }
    }

    private fun supportsHardwareAttestation(): Int {
        return try {
            if (Build.VERSION.SDK_INT < 23) return -2

            val keyAlias = "SmileID_Attestation"
            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)

            val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                keyAlias,
                KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY,
            )
                .setDigests(KeyProperties.DIGEST_SHA256)
                .setUserAuthenticationRequired(false)
                .build()
            val keyPairGenerator = KeyPairGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_RSA,
                "AndroidKeyStore",
            )
            keyPairGenerator.initialize(keyGenParameterSpec)
            val keyPair = keyPairGenerator.generateKeyPair()
            val keyFactory = KeyFactory.getInstance(
                keyPair.private.algorithm,
                "AndroidKeyStore",
            )
            val keyInfo = keyFactory.getKeySpec(keyPair.private, KeyInfo::class.java)

            val securityLevel = if (Build.VERSION.SDK_INT >= 31) {
                keyInfo.securityLevel
            } else {
                if (keyInfo.isInsideSecureHardware) {
                    1 // equivalent to SECURITY_LEVEL_TRUSTED_ENVIRONMENT
                } else {
                    -2 // equivalent to SECURITY_LEVEL_UNKNOWN
                }
            }
            securityLevel
        } catch (e: Exception) {
            -2 // equivalent to SECURITY_LEVEL_UNKNOWN
        } finally {
            try {
                val keyAlias = "SmileID_Attestation"
                val keyStore = KeyStore.getInstance("AndroidKeyStore")
                keyStore.load(null)
                if (keyStore.containsAlias(keyAlias)) {
                    keyStore.deleteEntry(keyAlias)
                }
            } catch (e: Exception) {
                // Ignore cleanup exceptions
            }
        }
    }

    override fun forceUpdate() {
        metadata.updateOrAddBy(Metadatum.SupportsHardwareAttestation(supportsHardwareAttestation)) {
            it.name ==
                metadataName
        }
    }
}
