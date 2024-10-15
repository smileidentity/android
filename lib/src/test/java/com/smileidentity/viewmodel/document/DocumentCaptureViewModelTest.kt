package com.smileidentity.viewmodel.document

import androidx.camera.view.CameraController
import com.google.mlkit.vision.objects.ObjectDetector
import com.smileidentity.SmileID
import com.smileidentity.compose.document.DocumentCaptureSide
import com.ujizin.camposer.state.CameraState
import com.ujizin.camposer.state.ImageCaptureResult
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import java.io.File
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DocumentCaptureViewModelTest {
    private lateinit var subject: DocumentCaptureViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(Dispatchers.Unconfined)
        SmileID.fileSavePath = "."
        val objectDetector: ObjectDetector = mockk()
        subject = DocumentCaptureViewModel(
            "jobId",
            side = DocumentCaptureSide.Front,
            knownAspectRatio = null,
            objectDetector = objectDetector,
            metadata = mutableListOf(),
        )
    }

    @Test
    fun `should acknowledge instructions`() {
        // when
        subject.onInstructionsAcknowledged()

        // then
        assertTrue(subject.uiState.value.acknowledgedInstructions)
    }

    @Test
    fun `should save file from gallery`() {
        // given
        val documentFile = File.createTempFile("documentFront", ".jpg")

        // when
        subject.onPhotoSelectedFromGallery(documentFile)

        // then
        assertEquals(documentFile, subject.uiState.value.documentImageToConfirm)
        assertTrue(subject.uiState.value.acknowledgedInstructions)
    }

    @Test
    fun `should set state when starting capture`() {
        // given
        val cameraState: CameraState = mockk()
        every { cameraState.takePicture(any(File::class), any()) } just Runs

        // when
        subject.captureDocument(cameraState)

        // then
        assertTrue(subject.uiState.value.showCaptureInProgress)
        assertEquals(DocumentDirective.Capturing, subject.uiState.value.directive)
    }

    @Test
    fun `should set state when capture failed`() {
        // given
        val cameraState: CameraState = mockk()
        val slot = slot<(ImageCaptureResult) -> Unit>()
        every { cameraState.takePicture(any(File::class), capture(slot)) } just Runs
        subject.captureDocument(cameraState)
        val expectedError = Exception("")
        val captureResult = ImageCaptureResult.Error(expectedError)

        // when
        slot.captured.invoke(captureResult)

        // then
        assertFalse(subject.uiState.value.showCaptureInProgress)
        assertEquals(expectedError, subject.uiState.value.captureError)
    }

    @Test
    fun `should retry`() {
        // given
        val documentFile = File.createTempFile("documentFront", ".jpg")
        subject.onPhotoSelectedFromGallery(documentFile)

        // when
        subject.onRetry()

        // then
        assertFalse(documentFile.exists())
        assertNull(subject.uiState.value.documentImageToConfirm)
        assertNull(subject.uiState.value.captureError)
        assertFalse(subject.uiState.value.acknowledgedInstructions)
        assertEquals(DocumentDirective.DefaultInstructions, subject.uiState.value.directive)
    }

    @Test
    fun `should update focusing state`() {
        // given
        val focusEvent = CameraController.TAP_TO_FOCUS_STARTED

        // when
        subject.onFocusEvent(focusEvent)

        // then
        assertEquals(DocumentDirective.Focusing, subject.uiState.value.directive)
    }
}
