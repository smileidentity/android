package com.smileidentity.ml.viewmodel

import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smileidentity.ml.detectors.FaceDetectorAnalyzer
import kotlinx.coroutines.launch

class FaceScanViewModel(val detector: FaceDetectorAnalyzer) : ViewModel() {

    @OptIn(ExperimentalGetImage::class)
    fun analyze(imageProxy: ImageProxy) = viewModelScope.launch {
        detector.detect(imageProxy.toBitmap())
        imageProxy.close()
    }
}
