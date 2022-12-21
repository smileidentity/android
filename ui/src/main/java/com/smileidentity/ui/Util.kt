package com.smileidentity.ui

import android.content.Context
import android.widget.Toast

internal fun Context.toast(message: String, length: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, length).show()
}
