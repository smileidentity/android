package com.smileidentity.sample.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import com.smileidentity.R
import com.smileidentity.models.JobType
import com.smileidentity.sample.compose.components.SearchableInputFieldItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

fun Context.toast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}

fun Context.toast(@StringRes message: Int) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}

fun getCurrentIsoTimestamp(): String {
    val pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
    val sdf = SimpleDateFormat(pattern, Locale.US)
    sdf.timeZone = TimeZone.getTimeZone("UTC")
    return sdf.format(Date())
}

// From https://stackoverflow.com/a/70510760
fun Context.isInternetAvailable(): Boolean {
    var result = false
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val networkCapabilities = connectivityManager.activeNetwork ?: return false
        val actNw =
            connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false
        result = when {
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    } else {
        connectivityManager.run {
            @Suppress("DEPRECATION")
            connectivityManager.activeNetworkInfo?.run {
                result = when (type) {
                    ConnectivityManager.TYPE_WIFI -> true
                    ConnectivityManager.TYPE_MOBILE -> true
                    ConnectivityManager.TYPE_ETHERNET -> true
                    else -> false
                }
            }
        }
    }
    return result
}

fun SnackbarHostState.showSnackbar(
    scope: CoroutineScope,
    message: String,
    actionLabel: String? = null,
    action: (() -> Unit)? = null,
) {
    scope.launch {
        val result = showSnackbar(message, actionLabel, withDismissAction = true)
        if (result == SnackbarResult.ActionPerformed) {
            action?.invoke()
        }
    }
}

fun jobResultMessageBuilder(jobName: String, didSubmitJob: Boolean): String =
    jobResultMessageBuilder(
        jobName,
        didSubmitJob,
        jobComplete = false,
        jobSuccess = false,
        code = null,
        resultCode = null,
        resultText = null,
    )

/**
 * Builds a display message for a job result. Since each job type has a different response type,
 * we need the individual fields rather than the overarching response type. Not all job types return
 * all fields, hence their nullability
 */
fun jobResultMessageBuilder(
    jobName: String,
    didSubmitJob: Boolean? = true,
    jobComplete: Boolean?,
    jobSuccess: Boolean?,
    code: String?,
    resultCode: String?,
    resultText: String?,
    suffix: String? = null,
): String {
    val message = StringBuilder("$jobName ")
    if (didSubmitJob == true) {
        if (jobComplete == true) {
            if (jobSuccess == true) {
                message.append("completed successfully")
            } else {
                message.append("completed unsuccessfully")
            }
            val parenthesesText = listOf(
                "resultText=$resultText",
                "code=$code",
                "resultCode=$resultCode",
            ).filterNot { it.contains("null") }.joinToString(", ")
            if (parenthesesText.isNotEmpty()) {
                message.append(" ($parenthesesText)")
            }
        } else {
            message.append("still pending")
        }
    } else {
        message.append("was saved offline and will need to be submitted later ")
    }
    suffix?.let { message.append(" $it") }
    return message.toString()
}

@Suppress("ktlint:standard:max-line-length")
val JobType.label: Int
    @StringRes
    get() = when (this) {
        JobType.SmartSelfieEnrollment -> R.string.si_smart_selfie_enrollment_product_name
        JobType.SmartSelfieAuthentication -> R.string.si_smart_selfie_authentication_product_name
        JobType.EnhancedKyc -> com.smileidentity.sample.R.string.enhanced_kyc_product_name
        JobType.BiometricKyc -> R.string.si_biometric_kyc_product_name
        JobType.DocumentVerification -> R.string.si_doc_v_product_name
        JobType.BVN -> R.string.si_bvn_product_name
        JobType.EnhancedDocumentVerification -> R.string.si_enhanced_docv_product_name
        JobType.Unknown -> -1
    }

val countryDetails = mapOf(
    "AO" to SearchableInputFieldItem("AO", "Angola", "🇦🇴"),
    "BF" to SearchableInputFieldItem("BF", "Burkina Faso", "🇧🇫"),
    "BI" to SearchableInputFieldItem("BI", "Burundi", "🇧🇮"),
    "BJ" to SearchableInputFieldItem("BJ", "Benin", "🇧🇯"),
    "BW" to SearchableInputFieldItem("BW", "Botswana", "🇧🇼"),
    "CD" to SearchableInputFieldItem("CD", "Congo (DRC)", "🇨🇩"),
    "CF" to SearchableInputFieldItem("CF", "Central African Republic", "🇨🇫"),
    "CG" to SearchableInputFieldItem("CG", "Congo", "🇨🇬"),
    "CI" to SearchableInputFieldItem("CI", "Côte d'Ivoire", "🇨🇮"),
    "CM" to SearchableInputFieldItem("CM", "Cameroon", "🇨🇲"),
    "CV" to SearchableInputFieldItem("CV", "Cabo Verde", "🇨🇻"),
    "DJ" to SearchableInputFieldItem("DJ", "Djibouti", "🇩🇯"),
    "EG" to SearchableInputFieldItem("EG", "Egypt", "🇪🇬"),
    "EH" to SearchableInputFieldItem("EH", "Western Sahara", "🇪🇭"),
    "ER" to SearchableInputFieldItem("ER", "Eritrea", "🇪🇷"),
    "ET" to SearchableInputFieldItem("ET", "Ethiopia", "🇪🇹"),
    "GA" to SearchableInputFieldItem("GA", "Gabon", "🇬🇦"),
    "GH" to SearchableInputFieldItem("GH", "Ghana", "🇬🇭"),
    "GM" to SearchableInputFieldItem("GM", "Gambia", "🇬🇲"),
    "GN" to SearchableInputFieldItem("GN", "Guinea", "🇬🇳"),
    "GQ" to SearchableInputFieldItem("GQ", "Equatorial Guinea", "🇬🇶"),
    "GW" to SearchableInputFieldItem("GW", "Guinea-Bissau", "🇬🇼"),
    "KE" to SearchableInputFieldItem("KE", "Kenya", "🇰🇪"),
    "KM" to SearchableInputFieldItem("KM", "Comoros", "🇰🇲"),
    "LR" to SearchableInputFieldItem("LR", "Liberia", "🇱🇷"),
    "LS" to SearchableInputFieldItem("LS", "Lesotho", "🇱🇸"),
    "LY" to SearchableInputFieldItem("LY", "Libya", "🇱🇾"),
    "MA" to SearchableInputFieldItem("MA", "Morocco", "🇲🇦"),
    "MG" to SearchableInputFieldItem("MG", "Madagascar", "🇲🇬"),
    "ML" to SearchableInputFieldItem("ML", "Mali", "🇲🇱"),
    "MR" to SearchableInputFieldItem("MR", "Mauritania", "🇲🇷"),
    "MU" to SearchableInputFieldItem("MU", "Mauritius", "🇲🇺"),
    "MW" to SearchableInputFieldItem("MW", "Malawi", "🇲🇼"),
    "MZ" to SearchableInputFieldItem("MZ", "Mozambique", "🇲🇿"),
    "NA" to SearchableInputFieldItem("NA", "Namibia", "🇳🇦"),
    "NE" to SearchableInputFieldItem("NE", "Niger", "🇳🇪"),
    "NG" to SearchableInputFieldItem("NG", "Nigeria", "🇳🇬"),
    "RW" to SearchableInputFieldItem("RW", "Rwanda", "🇷🇼"),
    "SC" to SearchableInputFieldItem("SC", "Seychelles", "🇸🇨"),
    "SD" to SearchableInputFieldItem("SD", "Sudan", "🇸🇩"),
    "SL" to SearchableInputFieldItem("SL", "Sierra Leone", "🇸🇱"),
    "SN" to SearchableInputFieldItem("SN", "Senegal", "🇸🇳"),
    "SO" to SearchableInputFieldItem("SO", "Somalia", "🇸🇴"),
    "SS" to SearchableInputFieldItem("SS", "South Sudan", "🇸🇸"),
    "ST" to SearchableInputFieldItem("ST", "São Tomé and Príncipe", "🇸🇹"),
    "SZ" to SearchableInputFieldItem("SZ", "Eswatini", "🇸🇿"),
    "TD" to SearchableInputFieldItem("TD", "Chad", "🇹🇩"),
    "TG" to SearchableInputFieldItem("TG", "Togo", "🇹🇬"),
    "TN" to SearchableInputFieldItem("TN", "Tunisia", "🇹🇳"),
    "TZ" to SearchableInputFieldItem("TZ", "Tanzania", "🇹🇿"),
    "UG" to SearchableInputFieldItem("UG", "Uganda", "🇺🇬"),
    "ZA" to SearchableInputFieldItem("ZA", "South Africa", "🇿🇦"),
    "ZM" to SearchableInputFieldItem("ZM", "Zambia", "🇿🇲"),
    "ZW" to SearchableInputFieldItem("ZW", "Zimbabwe", "🇿🇼"),
)
