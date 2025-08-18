package com.smileidentity.metadata.updaters

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.os.Build
import android.util.Base64
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.smileidentity.metadata.models.MetadataKey
import com.smileidentity.metadata.models.Metadatum
import com.smileidentity.metadata.models.Value
import com.smileidentity.metadata.updateOrAddBy
import java.security.MessageDigest

/**
 * A manager that updates metadata with build information.
 */
internal class BuildInfoMetadata(
    context: Context,
    private val metadata: SnapshotStateList<Metadatum>,
) : MetadataInterface {

    override val metadataName: String = MetadataKey.BuildInfo.key

    private val packageManager = context.packageManager
    private val packageName = context.packageName

    init {
        forceUpdate()
    }

    private fun getCertificateSha256Digests(): List<String> {
        val packageInfo: PackageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageManager.getPackageInfo(
                packageName,
                PackageManager.GET_SIGNING_CERTIFICATES,
            )
        } else {
            packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
        }

        val signatures: Array<out Signature?>? = if (Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.P
        ) {
            packageInfo.signingInfo?.apkContentsSigners
        } else {
            packageInfo.signatures
        }

        if (signatures == null) return emptyList()

        val md = MessageDigest.getInstance("SHA-256")
        return signatures.mapNotNull { signature ->
            signature?.toByteArray()?.let { cert ->
                val digest = md.digest(cert)
                Base64.encodeToString(
                    digest,
                    Base64.NO_PADDING or Base64.NO_WRAP or Base64.URL_SAFE,
                )
            }
        }
    }

    private fun getBuildSource(): String {
        val buildSource = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                // Android 11 (API 30) and above
                packageManager.getInstallSourceInfo(packageName).installingPackageName
            } else {
                // Below Android 11
                packageManager.getInstallerPackageName(packageName)
            } ?: "unknown"
        } catch (e: Exception) {
            "unknown"
        }
        return buildSource
    }

    private fun getBuildInfo(): Map<String, Value> {
        val buildSource = getBuildSource()
        val certificateDigests = getCertificateSha256Digests()
        return mapOf(
            "brand" to Value.StringValue(Build.BRAND),
            "device" to Value.StringValue(Build.DEVICE),
            "fingerprint" to Value.StringValue(Build.FINGERPRINT),
            "hardware" to Value.StringValue(Build.HARDWARE),
            "product" to Value.StringValue(Build.PRODUCT),
            "build_source" to Value.StringValue(buildSource),
            "package_name" to Value.StringValue(packageName),
            "certificate_digest" to Value.ArrayValue(
                certificateDigests.map { Value.StringValue(it) },
            ),
        )
    }

    override fun forceUpdate() {
        metadata.updateOrAddBy(Metadatum.BuildInfo(getBuildInfo())) { it.name == metadataName }
    }
}
