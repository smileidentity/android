package com.smileidentity.compose.document

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.smileidentity.compose.preview.Preview
import com.smileidentity.compose.preview.SmilePreviews
import java.io.File

/**
 * SmileDocumentCapture
 *
 * @param documentType Document type of the document being capture
 * @param onResult Callback to be invoked when the document capture is successful
 * @param onError Callback to be invoked when the document capture has an error
 */
@Composable
fun SmileDocumentCapture(
    onResult: () -> File,
    onError: (Throwable) -> Unit,
    modifier: Modifier = Modifier,
) {
    Text("Document Capture")
}

@SmilePreviews
@Composable
private fun SmileDocumentCapturePreview() {
    Preview {
        SmileDocumentCapture(
            onResult = { File.createTempFile("", "") },
            onError = {}
        )
    }
}
