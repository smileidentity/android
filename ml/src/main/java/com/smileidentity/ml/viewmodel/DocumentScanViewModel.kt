package com.smileidentity.ml.viewmodel

import android.graphics.Bitmap
import com.smileidentity.ml.detectors.DocumentDetectorAnalyzer

class DocumentScanViewModel(val detector: DocumentDetectorAnalyzer) :
    CameraViewModel<CameraPreviewImage<Bitmap>>()
