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

    override val metadataName: String = ""

    private val packageManager = context.packageManager
    private val packageName = context.packageName

    init {
        forceUpdate()
    }

    private fun getBuildBrand(): String = Build.BRAND

    private fun getBuildDevice(): String = Build.DEVICE
    private fun getBuildFingerprint(): String = Build.FINGERPRINT

    private fun getBuildHardware(): String = Build.HARDWARE

    private fun getBuildProduct(): String = Build.PRODUCT

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

    private fun getPackageName(): String = packageName

    private fun getCertificateSha256Digests(): List<String> {
        return try {
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
            signatures.mapNotNull { signature ->
                signature?.toByteArray()?.let { cert ->
                    val digest = md.digest(cert)
                    Base64.encodeToString(
                        digest,
                        Base64.NO_PADDING or Base64.NO_WRAP or Base64.URL_SAFE,
                    )
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override fun forceUpdate() {
        val buildBrand = getBuildBrand()
        buildBrand.isNotEmpty().let {
            metadata.updateOrAddBy(Metadatum.BuildBrand(buildBrand)) {
                it.name ==
                    MetadataKey.BuildBrand.key
            }
        }
        val buildDevice = getBuildDevice()
        buildDevice.isNotEmpty().let {
            metadata.updateOrAddBy(Metadatum.BuildDevice(buildDevice)) {
                it.name ==
                    MetadataKey.BuildDevice.key
            }
        }
        val buildFingerprint = getBuildFingerprint()
        buildFingerprint.isNotEmpty().let {
            metadata.updateOrAddBy(Metadatum.BuildFingerprint(buildFingerprint)) {
                it.name ==
                    MetadataKey.BuildFingerprint.key
            }
        }
        val buildHardware = getBuildHardware()
        buildHardware.isNotEmpty().let {
            metadata.updateOrAddBy(Metadatum.BuildHardware(buildHardware)) {
                it.name ==
                    MetadataKey.BuildHardware.key
            }
        }
        val buildProduct = getBuildProduct()
        buildProduct.isNotEmpty().let {
            metadata.updateOrAddBy(Metadatum.BuildProduct(buildProduct)) {
                it.name ==
                    MetadataKey.BuildProduct.key
            }
        }
        val buildSource = getBuildSource()
        buildSource.isNotEmpty().let {
            metadata.updateOrAddBy(Metadatum.BuildSource(buildSource)) {
                it.name ==
                    MetadataKey.BuildSource.key
            }
        }
        val packageName = getPackageName()
        packageName.isNotEmpty().let {
            metadata.updateOrAddBy(Metadatum.PackageName(packageName)) {
                it.name ==
                    MetadataKey.PackageName.key
            }
        }
        val certificateDigests = getCertificateSha256Digests()
        if (certificateDigests.isNotEmpty()) {
            metadata.updateOrAddBy(
                Metadatum.CertificateDigest(
                    Value.ArrayValue(
                        certificateDigests.map { Value.StringValue(it) },
                    ).list,
                ),
            ) { it.name == MetadataKey.CertificateDigest.key }
        }
    }
}
