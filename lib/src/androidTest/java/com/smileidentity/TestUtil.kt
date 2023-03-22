package com.smileidentity

import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import kotlin.time.Duration

internal fun ComposeContentTestRule.waitUntilExists(matcher: SemanticsMatcher, timeout: Duration) {
    waitUntil(timeout.inWholeMilliseconds) {
        onAllNodes(matcher).fetchSemanticsNodes().isNotEmpty()
    }
}
