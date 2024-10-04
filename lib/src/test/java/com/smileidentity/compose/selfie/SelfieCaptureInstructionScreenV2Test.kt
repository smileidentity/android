package com.smileidentity.compose.selfie

import app.cash.paparazzi.DeviceConfig.Companion.PIXEL_5
import app.cash.paparazzi.Paparazzi
import com.airbnb.lottie.LottieTask
import com.smileidentity.compose.selfie.v2.SelfieCaptureInstructionScreenV2
import java.util.concurrent.Executor
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SelfieCaptureInstructionScreenV2Test {

    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = PIXEL_5,
        supportsRtl = true,
    )

    @Before
    fun setup() {
        LottieTask.EXECUTOR = Executor(Runnable::run)
    }

    @Test
    fun testInstructionsScreen() {
        paparazzi.snapshot {
            SelfieCaptureInstructionScreenV2()
        }
    }
}
