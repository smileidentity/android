package com.smileidentity.camera.state

import androidx.camera.view.PreviewView

/**
 * Camera implementation mode.
 *
 * @param value internal implementation mode from cameraX
 * @see PreviewView.ImplementationMode
 * */
enum class ImplementationMode(internal val value: PreviewView.ImplementationMode) {
    Compatible(PreviewView.ImplementationMode.COMPATIBLE),
    Performance(PreviewView.ImplementationMode.PERFORMANCE),
}
