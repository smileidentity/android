package com.smileidentity.ui.viewmodel

import androidx.camera.core.ImageProxy
import com.smileidentity.ui.R
import com.smileidentity.ui.core.SmartSelfieResult
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
    private lateinit var subject: SelfieViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(Dispatchers.Unconfined)
        subject = SelfieViewModel()
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
        val callback = { it: SmartSelfieResult -> assertTrue(it is SmartSelfieResult.Success) }
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
        val callback = { it: SmartSelfieResult -> assertTrue(it is SmartSelfieResult.Error) }
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
    fun `analyzeImage should close the proxy when capture is already complete`() {
        // given
        val proxy = mockk<ImageProxy>()
        every { proxy.image } returns mockk(relaxed = true)
        every { proxy.close() } returns Unit
        subject.shouldAnalyzeImages = false

        // when
        subject.analyzeImage(proxy)

        // then
        verify(exactly = 1) { proxy.close() }
        verify(exactly = 1) { proxy.image }
        confirmVerified(proxy)
    }
}
