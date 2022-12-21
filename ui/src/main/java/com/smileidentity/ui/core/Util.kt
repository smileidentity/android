package com.smileidentity.ui.core

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes

internal fun Context.toast(@StringRes message: Int) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}
