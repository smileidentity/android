package com.smileidentity.ui.viewmodel

import androidx.camera.core.ImageProxy
import com.smileidentity.ui.R
import com.smileidentity.ui.core.SelfieCaptureResult
import com.ujizin.camposer.state.CameraState
import com.ujizin.camposer.state.ImageCaptureResult
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class SelfieViewModelTest {
    private val subject = SelfieViewModel()

    @Test
    fun `uiState should be initialized with the correct defaults`() {
        val uiState = subject.uiState.value
        assertEquals(R.string.si_selfie_capture_directive_smile, uiState.currentDirective)
        assertEquals(0f, uiState.progress)
    }

    @Test
    fun `takePicture should call callback with Success when ImageCaptureResult is Success`() {
        // given
        val cameraState = mockk<CameraState>()
        val callback = { it: SelfieCaptureResult -> assertTrue(it is SelfieCaptureResult.Success) }
        val slot = slot<(ImageCaptureResult) -> Unit>()
        every { cameraState.takePicture(any<File>(), capture(slot)) } returns Unit

        // when
        subject.takePicture(cameraState, callback)
        slot.captured(ImageCaptureResult.Success(null))

        // then
        verify(exactly = 1) { cameraState.takePicture(any<File>(), any()) }
        confirmVerified(cameraState)
    }

    @Test
    fun `takePicture should call callback with Error when ImageCaptureResult is Error`() {
        // given
        val cameraState = mockk<CameraState>()
        val callback = { it: SelfieCaptureResult -> assertTrue(it is SelfieCaptureResult.Error) }
        val slot = slot<(ImageCaptureResult) -> Unit>()
        every { cameraState.takePicture(any<File>(), capture(slot)) } returns Unit

        // when
        subject.takePicture(cameraState, callback)
        slot.captured(ImageCaptureResult.Error(RuntimeException()))

        // then
        verify(exactly = 1) { cameraState.takePicture(any<File>(), any()) }
        confirmVerified(cameraState)
    }

    @Test
    fun `analyzeImage should call close on the proxy`() {
        // given
        val proxy = mockk<ImageProxy>()
        every { proxy.close() } returns Unit

        // when
        subject.analyzeImage(proxy)

        // then
        verify(exactly = 1) { proxy.close() }
        confirmVerified(proxy)
    }

    @Test
    fun `analyzeImage should update uiState to a valid directive`() {
        // given
        val directives = setOf(
            R.string.si_selfie_capture_directive_smile,
            R.string.si_selfie_capture_directive_capturing,
            R.string.si_selfie_capture_directive_face_too_far,
            R.string.si_selfie_capture_directive_unable_to_detect_face,
        )

        // when
        for (i in 0..100) {
            subject.analyzeImage(mockk(relaxed = true))
        }

        // then
        val uiState = subject.uiState.value
        assertTrue(directives.contains(uiState.currentDirective))
    }
}
