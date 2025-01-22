package com.smileidentity.viewmodel

import com.smileidentity.compose.selfie.viewmodel.OrchestratedSelfieViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
class OrchestratedSelfieViewModelTest {
    private lateinit var subject: OrchestratedSelfieViewModel

    // @Before
    // fun setup() {
    //     Dispatchers.setMain(Dispatchers.Unconfined)
    //     subject = OrchestratedSelfieViewModel(
    //         isEnroll = true,
    //         userId = randomUserId(),
    //         jobId = randomJobId(),
    //         allowNewEnroll = false,
    //         skipApiSubmission = false,
    //         metadata = mutableListOf(),
    //     )
    // }
    //
    // @After
    // fun tearDown() {
    //     Dispatchers.resetMain()
    // }
    //
    // @Test
    // fun `uiState should be initialized with the correct defaults`() {
    //     val uiState = subject.uiState.value
    //     assertEquals(SelfieDirective.InitialInstruction, uiState.directive)
    //     assertEquals(0f, uiState.progress)
    //     assertEquals(null, uiState.selfieToConfirm)
    //     assertEquals(null, uiState.processingState)
    //     assertEquals(
    //         StringResource.ResId(R.string.si_processing_error_subtitle),
    //         uiState.errorMessage,
    //     )
    // }
    //
    // @Test
    // fun `analyzeImage should close the proxy when capture is already complete`() {
    //     // given
    //     val proxy = mockk<ImageProxy>()
    //     every { proxy.image } returns mockk(relaxed = true)
    //     every { proxy.close() } returns Unit
    //     subject.shouldAnalyzeImages = false
    //
    //     // when
    //     subject.analyzeImage(proxy, CamSelector.Back)
    //
    //     // then
    //     verify(exactly = 1) { proxy.close() }
    //     verify(exactly = 1) { proxy.image }
    //     confirmVerified(proxy)
    // }
}
