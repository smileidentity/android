package com.smileidentity.ui

import com.smileidentity.ui.core.createSmileTempFile
import com.smileidentity.ui.core.postProcessImageFile
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic

fun setupPostProcessMocks() {
    mockkStatic(::createSmileTempFile)
    every { createSmileTempFile(any(), any()) } returns mockk(relaxed = true)

    mockkStatic(::postProcessImageFile)
    every { postProcessImageFile(any(), any(), any(), any()) } returns mockk(relaxed = true)
}
