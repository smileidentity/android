package com.smileidentity.viewmodel

import android.os.Build
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smileidentity.R
import com.smileidentity.datasource.FileDataSource
import com.ujizin.camposer.state.CameraState
import com.ujizin.camposer.state.ImageCaptureResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File

data class DocumentUiState(
    val progress: Float = 0f,
    val documentImage: File? = null,
    @StringRes val errorMessage: Int? = null,
)

class DocumentViewModel(
    private val fileDataSource: FileDataSource = FileDataSource(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(DocumentUiState())
    val uiState = _uiState.asStateFlow()

    // Captures a document image using the camera and saves it to the device's external storage.
    internal fun captureDocument(cameraState: CameraState) = with(cameraState) {
        viewModelScope.launch {
            when {
                // If the device is running Android 10 (API level 29) or higher, use the new CameraX API
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> takePicture(
                    fileDataSource.imageContentValues,
                    onResult = ::onImageResult,
                )
                // Otherwise, use the deprecated Camera API
                else -> takePicture(
                    fileDataSource.getFile("jpg"),
                    ::onImageResult,
                )
            }
        }
    }

    // Callback function that is called when the camera captures an image.
    private fun onImageResult(imageResult: ImageCaptureResult) {
        when (imageResult) {
            is ImageCaptureResult.Error -> onError(imageResult.throwable)
            is ImageCaptureResult.Success -> captureSuccess()
        }
    }

    // Handles an error that occurred during the image capture operation.
    private fun onError(throwable: Throwable?) {
        Timber.e(throwable, "Error capturing document")
        _uiState.update { it.copy(errorMessage = R.string.si_doc_v_capture_error_subtitle) }
    }

    // Handles the case where the image capture was successful.
    private fun captureSuccess() {
        _uiState.update { it.copy(documentImage = fileDataSource.lastPicture) }
    }
}
