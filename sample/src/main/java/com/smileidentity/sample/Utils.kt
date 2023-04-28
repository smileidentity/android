package com.smileidentity.sample

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
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
