package com.smileidentity.viewmodel

import com.smileidentity.SmileID
import com.smileidentity.models.AuthenticationResponse
import com.smileidentity.models.BvnToptModeResponse
import com.smileidentity.models.BvnToptResponse
import com.smileidentity.models.Config
import com.smileidentity.models.JobType
import com.smileidentity.models.PartnerParams
import com.smileidentity.models.SubmitBvnToptResponse
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BvnConsentViewModelTest {
    private lateinit var subject: BvnConsentViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(Dispatchers.Unconfined)
        subject = BvnConsentViewModel()
        SmileID.config = Config(
            partnerId = "partnerId",
            authToken = "authToken",
            prodBaseUrl = "prodBaseUrl",
            sandboxBaseUrl = "sandboxBaseUrl",
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `submitUserBvn should move BvnConsentScreens to ChooseOtpDeliveryScreen`() = runTest {
        // given
        SmileID.api = mockk()
        coEvery { SmileID.api.authenticate(any()) } returns AuthenticationResponse(
            success = true,
            signature = "signature",
            timestamp = "timestamp",
            partnerParams = PartnerParams(jobType = JobType.BVN),
        )

        coEvery { SmileID.api.requestBvnOtpMode(any()) } returns BvnToptResponse(
            message = "message",
            modes = listOf(),
            sessionId = "sessionId",
            signature = "signature",
            success = true,
            timestamp = "timestamp",
        )

        // when
        subject.submitUserBvn()

        // then
        Assert.assertEquals(
            BvnConsentScreens.ChooseOtpDeliveryScreen,
            subject.uiState.value.bvnConsentScreens,
        )
    }

    @Test
    fun `requestBvnOtp should move BvnConsentScreens to ShowVerifyOtpScreen`() = runTest {
        // given
        SmileID.api = mockk()
        coEvery { SmileID.api.authenticate(any()) } returns AuthenticationResponse(
            success = true,
            signature = "signature",
            timestamp = "timestamp",
            partnerParams = PartnerParams(jobType = JobType.BVN),
        )

        coEvery { SmileID.api.requestBvnOtp(any()) } returns BvnToptModeResponse(
            message = "message",
            signature = "signature",
            success = true,
            timestamp = "timestamp",
        )

        // when
        subject.requestBvnOtp()

        // then
        Assert.assertEquals(
            BvnConsentScreens.ShowVerifyOtpScreen,
            subject.uiState.value.bvnConsentScreens,
        )
    }

    @Test
    fun `submitBvnOtp should return success if the BVN and OTP is correct`() = runTest {
        // given
        SmileID.api = mockk()
        coEvery { SmileID.api.authenticate(any()) } returns AuthenticationResponse(
            success = true,
            signature = "signature",
            timestamp = "timestamp",
            partnerParams = PartnerParams(jobType = JobType.BVN),
        )

        coEvery { SmileID.api.submitBvnOtp(any()) } returns SubmitBvnToptResponse(
            message = "message",
            signature = "signature",
            success = true,
            timestamp = "timestamp",
        )

        // when
        subject.submitBvnOtp()

        // then
        Assert.assertEquals(true, subject.uiState.value.showSuccess)
    }
}