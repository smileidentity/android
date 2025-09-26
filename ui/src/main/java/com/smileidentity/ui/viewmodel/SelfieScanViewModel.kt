package com.smileidentity.ui.viewmodel

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import com.smileidentity.camera.CameraPreviewImage
import com.smileidentity.ml.scan.AggregateResultListener
import com.smileidentity.ml.scan.AnalyzerLoopErrorListener
import com.smileidentity.ml.scan.IdentityAggregator
import com.smileidentity.ml.scan.IdentityScanFlow
import com.smileidentity.ml.states.IdentityScanState
import timber.log.Timber

class SelfieScanViewModel :
    IdentityScanViewModel<CameraPreviewImage<Bitmap>>(),
    AnalyzerLoopErrorListener,
    AggregateResultListener<IdentityAggregator.InterimResult, IdentityAggregator.FinalResult> {

    internal var identityScanFlow: IdentityScanFlow? = null

    init {
        identityScanFlow = IdentityScanFlow(
            analyzerLoopErrorListener = this,
            aggregateResultListener = this,
        )
    }

    fun startScan(context: Context, lifecycleOwner: LifecycleOwner) {
        identityScanFlow?.startFlow(
            context = context,
            imageStream = getImageStream,
            lifecycleOwner = lifecycleOwner,
            coroutineScope = viewModelScope,
            parameters = IdentityScanState.ScanType.SELFIE,
            onError = {
                Timber.d("juuuuuma here VM Identity Scan Flow error $it")
            },
        )
    }

    override fun onAnalyzerFailure(t: Throwable): Boolean {
        Timber.d("juuuuuma here VM 1 $t")
        return false
    }

    override fun onResultFailure(t: Throwable): Boolean {
        Timber.d("juuuuuma here VM 2 $t")
        return false
    }

    override suspend fun onResult(result: IdentityAggregator.FinalResult) {
        Timber.d("juuuuuma here VM 3 $result")
    }

    override suspend fun onInterimResult(result: IdentityAggregator.InterimResult) {
        Timber.d("juuuuuma here VM 4 $result")
    }

    override suspend fun onReset() {
        Timber.d("juuuuuma here VM 5")
    }

//    class SelfieScanViewModelFactory(val identityScanFlow: IdentityScanFlow) :
//        ViewModelProvider.Factory {
//        @Suppress("UNCHECKED_CAST")
//        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T =
//            SelfieScanViewModel(identityScanFlow = identityScanFlow) as T
//    }
}
