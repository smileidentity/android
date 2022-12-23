package com.smileidentity.sample

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes

internal fun Context.toast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

internal fun Context.toast(@StringRes message: Int) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}
