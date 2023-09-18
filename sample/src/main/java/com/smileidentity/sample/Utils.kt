package com.smileidentity.sample

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import com.smileidentity.models.JobType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

fun Context.toast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}

fun Context.toast(@StringRes message: Int) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
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

/**
 * Builds a display message for a job result. Since each job type has a different response type,
 * we need the individual fields rather than the overarching response type. Not all job types return
 * all fields, hence their nullability
 */
fun jobResultMessageBuilder(
    jobName: String,
    jobComplete: Boolean?,
    jobSuccess: Boolean?,
    code: String?,
    resultCode: String?,
    resultText: String?,
    suffix: String? = null,
): String {
    val message = StringBuilder("$jobName ")
    if (jobComplete == true) {
        if (jobSuccess == true) {
            message.append("completed successfully")
        } else {
            message.append("completed unsuccessfully")
        }
        message.append(" (resultText=$resultText, code=$code, resultCode=$resultCode)")
    } else {
        message.append("still pending")
    }
    suffix?.let { message.append(" $it") }
    return message.toString()
}

val JobType.label: Int
    @StringRes
    get() = when (this) {
        JobType.SmartSelfieEnrollment -> com.smileidentity.R.string.si_smart_selfie_enrollment_product_name // ktlint-disable max-line-length
        JobType.SmartSelfieAuthentication -> com.smileidentity.R.string.si_smart_selfie_authentication_product_name // ktlint-disable max-line-length
        JobType.EnhancedKyc -> R.string.enhanced_kyc_product_name
        JobType.BiometricKyc -> com.smileidentity.R.string.si_biometric_kyc_product_name
        JobType.DocumentVerification -> com.smileidentity.R.string.si_doc_v_product_name
        JobType.BVN -> com.smileidentity.R.string.si_bvn_product_name
        JobType.Unknown -> -1
    }
