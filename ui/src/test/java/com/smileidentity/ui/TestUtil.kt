package com.smileidentity.ui

import com.smileidentity.ui.core.createSmileTempFile
import com.smileidentity.ui.core.postProcessImage
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic

fun setupPostProcessMocks() {
    mockkStatic(::createSmileTempFile)
    every { createSmileTempFile(any()) } returns mockk(relaxed = true)

    mockkStatic(::postProcessImage)
    every { postProcessImage(any(), any(), any(), any()) } returns mockk(relaxed = true)
}
