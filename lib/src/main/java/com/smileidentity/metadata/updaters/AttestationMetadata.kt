package com.smileidentity.metadata.updaters

import android.content.Context
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyInfo
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.smileidentity.metadata.models.MetadataKey
import com.smileidentity.metadata.models.Metadatum
import com.smileidentity.metadata.models.Value
import com.smileidentity.metadata.updateOrAddBy
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * A manager that updates metadata with attestation information.
 */
internal class AttestationMetadata(
    context: Context,
    private val metadata: SnapshotStateList<Metadatum>,
) : MetadataInterface {

    override val metadataName: String = ""
    private val keyAlias = "SmileID_Attestation"
    private val keyStoreType = "AndroidKeyStore"
    private var keyPair: KeyPair? = null
    private var supportsHardwareAttestation: Int = -2
    private var certificateChain: List<String> = emptyList()

    override fun onStart(owner: LifecycleOwner) {
        owner.lifecycleScope.launch(Dispatchers.IO) {
            if (keyPair == null) {
                keyPair = generateKeyPair()
            }
            supportsHardwareAttestation = supportsHardwareAttestation()
            certificateChain = getCertificateChain()
            forceUpdate()
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        deleteKeyPair()
    }

    private fun generateKeyPair(): KeyPair? {
        return try {
            if (Build.VERSION.SDK_INT < 24) return null

            val keyStore = KeyStore.getInstance(keyStoreType)
            keyStore.load(null)

            val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                keyAlias,
                KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY,
            )
                .setDigests(KeyProperties.DIGEST_SHA256)
                .setUserAuthenticationRequired(false)
                // dummy challenge since the key is only used for metadata
                .setAttestationChallenge("SmileID".toByteArray())
                .build()
            val keyPairGenerator = KeyPairGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_RSA,
                keyStoreType,
            )
            keyPairGenerator.initialize(keyGenParameterSpec)
            val keyPair = keyPairGenerator.generateKeyPair()
            keyPair
        } catch (e: Exception) {
            null
        }
    }

    private fun deleteKeyPair() {
        try {
            val keyStore = KeyStore.getInstance(keyStoreType)
            keyStore.load(null)
            if (keyStore.containsAlias(keyAlias)) {
                keyStore.deleteEntry(keyAlias)
            }
        } catch (e: Exception) {
            // Ignore cleanup exceptions
        }
    }

    private fun supportsHardwareAttestation(): Int {
        if (keyPair == null || Build.VERSION.SDK_INT < 23) return -2
        return try {
            val keyFactory = KeyFactory.getInstance(
                keyPair!!.private.algorithm,
                "AndroidKeyStore",
            )
            val keyInfo = keyFactory.getKeySpec(keyPair!!.private, KeyInfo::class.java)

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
        }
    }

    private fun getCertificateChain(): List<String> = try {
        val keyStore = KeyStore.getInstance(keyStoreType)
        keyStore.load(null)
        val certs = keyStore.getCertificateChain(keyAlias)
        certs?.map { cert ->
            Base64.encodeToString(
                cert.encoded,
                Base64.NO_WRAP or Base64.NO_PADDING or Base64.URL_SAFE,
            )
        } ?: emptyList()
    } catch (e: Exception) {
        emptyList()
    }

    override fun forceUpdate() {
        metadata.updateOrAddBy(Metadatum.SupportsHardwareAttestation(supportsHardwareAttestation)) {
            it.name == MetadataKey.SupportsHardwareAttestation.key
        }
        if (certificateChain.isNotEmpty()) {
            metadata.updateOrAddBy(
                Metadatum.AttestationCertificateChain(
                    Value.ArrayValue(
                        certificateChain.map { Value.StringValue(it) },
                    ).list,
                ),
            ) { it.name == MetadataKey.AttestationCertificateChain.key }
        }
    }
}
