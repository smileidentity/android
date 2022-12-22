package com.smileidentity.ui.core

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes
import com.ujizin.camposer.state.CamSelector
import com.ujizin.camposer.state.CameraState

internal fun Context.toast(@StringRes message: Int) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}

internal val CameraState.hasMultipleCameras
    get() = hasCamera(CamSelector.Front) && hasCamera(CamSelector.Back)
