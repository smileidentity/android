package com.smileidentity.compose

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ProcessingScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun shouldDisplayProcessingInProgressScreen() {
        // when
        composeTestRule.setContent {
            ProcessingScreen(
                processingState = ProcessingState.InProgress,
                inProgressTitle = "",
                inProgressSubtitle = "",
                inProgressIcon = ColorPainter(Color.Blue),
                successTitle = "",
                successSubtitle = "",
                successIcon = ColorPainter(Color.Blue),
                errorTitle = "",
                errorSubtitle = "",
                errorIcon = ColorPainter(Color.Blue),
                continueButtonText = "",
                onContinue = {},
                retryButtonText = "",
                onRetry = {},
                closeButtonText = "",
                onClose = {},
            )
        }

        // then
        composeTestRule.onNodeWithTag("processing_screen_in_progress").assertExists()
    }

    @Test
    fun shouldDisplayProcessingSuccessScreen() {
        // when
        composeTestRule.setContent {
            ProcessingScreen(
                processingState = ProcessingState.Success,
                inProgressTitle = "",
                inProgressSubtitle = "",
                inProgressIcon = ColorPainter(Color.Blue),
                successTitle = "",
                successSubtitle = "",
                successIcon = ColorPainter(Color.Blue),
                errorTitle = "",
                errorSubtitle = "",
                errorIcon = ColorPainter(Color.Blue),
                continueButtonText = "",
                onContinue = {},
                retryButtonText = "",
                onRetry = {},
                closeButtonText = "",
                onClose = {},
            )
        }

        // then
        composeTestRule.onNodeWithTag("processing_screen_success").assertExists()
    }

    @Test
    fun shouldDisplayProcessingErrorScreen() {
        // when
        composeTestRule.setContent {
            ProcessingScreen(
                processingState = ProcessingState.Error,
                inProgressTitle = "",
                inProgressSubtitle = "",
                inProgressIcon = ColorPainter(Color.Blue),
                successTitle = "",
                successSubtitle = "",
                successIcon = ColorPainter(Color.Blue),
                errorTitle = "",
                errorSubtitle = "",
                errorIcon = ColorPainter(Color.Blue),
                continueButtonText = "",
                onContinue = {},
                retryButtonText = "",
                onRetry = {},
                closeButtonText = "",
                onClose = {},
            )
        }

        // then
        composeTestRule.onNodeWithTag("processing_screen_error").assertExists()
    }

    @Test
    fun shouldInvokeOnContinueCallbackOnContinueButtonClick() {
        // given
        var callbackInvoked = false
        val onContinue = { callbackInvoked = true }
        val continueButtonText = "Continue"

        // when
        composeTestRule.setContent {
            ProcessingSuccessScreen(
                title = "",
                subtitle = "",
                icon = ColorPainter(Color.Blue),
                continueButtonText = continueButtonText,
                onContinue = onContinue,
            )
        }
        composeTestRule.onNodeWithText(continueButtonText).performClick()

        // then
        assertTrue(callbackInvoked)
    }

    @Test
    fun shouldInvokeOnRetryCallbackOnRetryButtonClick() {
        // given
        var callbackInvoked = false
        val onRetry = { callbackInvoked = true }
        val retryButtonText = "Retry"

        // when
        composeTestRule.setContent {
            ProcessingErrorScreen(
                title = "",
                subtitle = "",
                icon = ColorPainter(Color.Blue),
                retryButtonText = retryButtonText,
                onRetry = onRetry,
                closeButtonText = "",
                onClose = {},
            )
        }
        composeTestRule.onNodeWithText(retryButtonText).performClick()

        // then
        assertTrue(callbackInvoked)
    }

    @Test
    fun shouldInvokeOnCloseCallbackOnCloseButtonClick() {
        // given
        var callbackInvoked = false
        val onClose = { callbackInvoked = true }
        val closeButtonText = "Close"

        // when
        composeTestRule.setContent {
            ProcessingErrorScreen(
                title = "",
                subtitle = "",
                icon = ColorPainter(Color.Blue),
                retryButtonText = "",
                onRetry = {},
                closeButtonText = closeButtonText,
                onClose = onClose,
            )
        }
        composeTestRule.onNodeWithText(closeButtonText).performClick()

        // then
        assertTrue(callbackInvoked)
    }
}
