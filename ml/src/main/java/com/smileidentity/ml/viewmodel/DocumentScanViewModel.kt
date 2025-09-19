package com.smileidentity.ml.viewmodel

import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smileidentity.ml.detectors.DocumentDetectorAnalyzer
import kotlinx.coroutines.launch
import timber.log.Timber

class DocumentScanViewModel(val detector: DocumentDetectorAnalyzer) : ViewModel() {

    @OptIn(ExperimentalGetImage::class)
    fun analyze(imageProxy: ImageProxy) = viewModelScope.launch {
        val results = detector.detect(imageProxy.toBitmap())
        Timber.d("Juuuuuuuuuuuma ${imageProxy.format}")
        Timber.d("Juuuuuuuuuuuma ${results.detections()}")
    }
}
