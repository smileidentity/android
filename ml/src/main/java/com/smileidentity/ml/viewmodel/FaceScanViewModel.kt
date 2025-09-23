package com.smileidentity.ml.viewmodel

import android.graphics.Bitmap
import com.smileidentity.ml.detectors.FaceDetectorAnalyzer

class FaceScanViewModel(val detector: FaceDetectorAnalyzer) :
    CameraViewModel<CameraPreviewImage<Bitmap>>()
