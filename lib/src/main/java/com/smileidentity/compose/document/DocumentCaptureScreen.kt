package com.smileidentity.compose.document

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smileidentity.R
import com.smileidentity.compose.preview.Preview
import com.smileidentity.compose.preview.SmilePreviews
import com.smileidentity.models.Document
import com.smileidentity.util.randomJobId
import com.smileidentity.util.randomUserId
import com.smileidentity.viewmodel.DocumentViewModel
import com.smileidentity.viewmodel.viewModelFactory
import com.ujizin.camposer.CameraPreview
import com.ujizin.camposer.state.CamSelector
import com.ujizin.camposer.state.ScaleType
import com.ujizin.camposer.state.rememberCamSelector
import com.ujizin.camposer.state.rememberCameraState
import java.io.File

@Composable
internal fun DocumentCaptureScreen(
    idType: Document,
    titleText: String,
    subtitleText: String,
    modifier: Modifier = Modifier,
    idAspectRatio: Float? = idType.aspectRatio,
    userId: String = rememberSaveable { randomUserId() },
    jobId: String = rememberSaveable { randomJobId() },
    isBackSide: Boolean = false,
    bypassSelfieCaptureWithFile: File? = null,
    viewModel: DocumentViewModel = viewModel(
        factory = viewModelFactory {
            DocumentViewModel(
                userId = userId,
                jobId = jobId,
                idType = idType,
                idAspectRatio = idAspectRatio,
                selfieFile = bypassSelfieCaptureWithFile,
            )
        },
    ),
) {
    val cameraState = rememberCameraState()
    val camSelector by rememberCamSelector(CamSelector.Back)
    Column(modifier.fillMaxSize()) {
        Box(
            modifier = Modifier.fillMaxSize().weight(1f),
        ) {
            CameraPreview(
                cameraState = cameraState,
                camSelector = camSelector,
                scaleType = ScaleType.FillCenter,
                modifier = Modifier
                    .testTag("document_camera_preview")
                    .fillMaxSize()
                    .clipToBounds(),
            )
            DocumentShapedBoundingBox(
                aspectRatio = idAspectRatio,
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .consumeWindowInsets(WindowInsets.safeDrawing)
                    .fillMaxSize()
                    .testTag("document_progress_indicator"),
            )
        }
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.Bottom),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(16.dp)
                .wrapContentSize(),
        ) {
            Text(
                text = titleText,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.secondary,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = subtitleText,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.secondary,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            CaptureDocumentButton {
                viewModel.takeButtonCaptureDocument(
                    cameraState = cameraState,
                    hasBackSide = isBackSide,
                )
            }
        }
    }
}

@Composable
private fun CaptureDocumentButton(
    onCaptureClicked: () -> Unit,
) {
    Image(
        painter = painterResource(id = R.drawable.si_camera_capture),
        contentDescription = "smile_camera_capture",
        modifier = Modifier
            .size(70.dp)
            .clickable { onCaptureClicked.invoke() },
    )
}

@SmilePreviews
@Composable
private fun DocumentCaptureScreenPreview() {
    Preview {
        DocumentCaptureScreen(
            titleText = "Front of National ID Card",
            subtitleText = "Make sure all corners are visible and there is no glare",
            idType = Document("KE", "ID_CARD"),
        )
    }
}
