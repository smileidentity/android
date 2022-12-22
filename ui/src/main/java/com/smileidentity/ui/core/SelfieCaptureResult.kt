package com.smileidentity.ui.core

sealed class SelfieCaptureResult {
    data class Success(val selfieFile: String) : SelfieCaptureResult()
    // TODO: Do we want to explicitly disambiguate all error cases as part of the sealed hierarchy?
    //  (e.g. PermissionDenied, NetworkError, IOError, etc.)
    data class Error(val throwable: Throwable) : SelfieCaptureResult()
}

// Using a Functional (SAM) interface for best Java interoperability (otherwise typealias would do)
fun interface SelfieCaptureResultCallback {
    fun onResult(result: SelfieCaptureResult)
}
