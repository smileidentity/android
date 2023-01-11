package com.smileidentity.ui.viewmodel

import androidx.camera.core.ImageProxy
import com.smileidentity.ui.R
import com.smileidentity.ui.core.SelfieCaptureResult
import com.smileidentity.ui.setupPostProcessMocks
import com.ujizin.camposer.state.CameraState
import com.ujizin.camposer.state.ImageCaptureResult
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

class SelfieViewModelTest {
    private val subject = SelfieViewModel()

    @Before
    fun setup() {
        Dispatchers.setMain(Dispatchers.Unconfined)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `uiState should be initialized with the correct defaults`() {
        val uiState = subject.uiState.value
        assertEquals(R.string.si_selfie_capture_directive_smile, uiState.currentDirective)
        assertEquals(0f, uiState.progress)
        assertEquals(false, uiState.isCapturing)
    }

    @Test
    fun `takePicture should call callback with Success when ImageCaptureResult is Success`() {
        // given
        val cameraState = mockk<CameraState>()
        val callback = { it: SelfieCaptureResult -> assertTrue(it is SelfieCaptureResult.Success) }
        val slots = mutableListOf<(ImageCaptureResult) -> Unit>()
        every { cameraState.takePicture(any<File>(), capture(slots)) } returns Unit
        setupPostProcessMocks()

        // when
        subject.takeButtonInitiatedPictures(cameraState, callback)
        slots.forEach { it(ImageCaptureResult.Success(null)) }

        // then
        verify(exactly = slots.size) { cameraState.takePicture(any<File>(), any()) }
        confirmVerified(cameraState)
    }

    @Test
    fun `takePicture should call callback with Error when ImageCaptureResult is Error`() {
        // given
        val cameraState = mockk<CameraState>()
        val callback = { it: SelfieCaptureResult -> assertTrue(it is SelfieCaptureResult.Error) }
        val slots = mutableListOf<(ImageCaptureResult) -> Unit>()
        every { cameraState.takePicture(any<File>(), capture(slots)) } returns Unit

        // when
        subject.takeButtonInitiatedPictures(cameraState, callback)
        slots.forEach { it(ImageCaptureResult.Error(RuntimeException())) }

        // then
        verify(exactly = slots.size) { cameraState.takePicture(any<File>(), any()) }
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
