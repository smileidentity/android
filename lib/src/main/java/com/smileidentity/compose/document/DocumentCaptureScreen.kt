package com.smileidentity.compose.document

import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import com.smileidentity.compose.preview.SmilePreview
import com.smileidentity.models.Document
import com.smileidentity.randomJobId
import com.smileidentity.randomUserId
import com.smileidentity.viewmodel.DocumentViewModel
import com.smileidentity.viewmodel.viewModelFactory
import com.ujizin.camposer.CameraPreview
import com.ujizin.camposer.state.CamSelector
import com.ujizin.camposer.state.ScaleType
import com.ujizin.camposer.state.rememberCamSelector
import com.ujizin.camposer.state.rememberCameraState

@VisibleForTesting
@Composable
internal fun DocumentCaptureScreen(
    userId: String = rememberSaveable { randomUserId() },
    jobId: String = rememberSaveable { randomJobId() },
    enforcedIdType: Document? = null,
    idAspectRatio: Float? = enforcedIdType?.aspectRatio,
    titleText: String,
    subtitleText: String,
    isBackSide: Boolean = false,
    viewModel: DocumentViewModel = viewModel(
        factory = viewModelFactory {
            DocumentViewModel(
                userId = userId,
                jobId = jobId,
                enforcedIdType = enforcedIdType,
                idAspectRatio = idAspectRatio,
            )
        },
    ),
) {
    val cameraState = rememberCameraState()
    val camSelector by rememberCamSelector(CamSelector.Back)
    Box(
        modifier = Modifier.fillMaxSize(),
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
                .fillMaxSize()
                .testTag("document_progress_indicator"),
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.Bottom),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
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
            CaptureDocumentButton { viewModel.takeButtonCaptureDocument(cameraState = cameraState, isBackSide = isBackSide) }
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

@SmilePreview
@Composable
private fun DocumentCaptureScreenPreview() {
    Preview {
        DocumentCaptureScreen(
            titleText = "Front of National ID Card",
            subtitleText = "Make sure all corners are visible and there is no glare",
        )
    }
}
