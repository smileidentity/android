package com.smileidentity.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.platform.testTag
import com.smileidentity.SmileIDCrashReporting
import io.sentry.ISpan
import io.sentry.SpanOptions
import timber.log.Timber

private const val OP_PARENT_COMPOSITION = "ui.compose.composition"
private const val OP_COMPOSE = "ui.compose"

private const val OP_PARENT_RENDER = "ui.compose.rendering"
private const val OP_RENDER = "ui.render"

@Immutable
private class ImmutableHolder<T>(var item: T)

private fun getRootSpan(): ISpan? {
    var rootSpan: ISpan? = null
    SmileIDCrashReporting.hub.configureScope {
        Timber.v("Configuring scope for root span: ${it.transaction}")
        rootSpan = it.transaction
    }
    return rootSpan
}

private val localSentryCompositionParentSpan = compositionLocalOf {
    ImmutableHolder(
        getRootSpan()
            ?.startChild(
                OP_PARENT_COMPOSITION,
                "Jetpack Compose Initial Composition",
                SpanOptions().apply {
                    isTrimStart = true
                    isTrimEnd = true
                    isIdle = true
                },
            ),
    )
}

private val localSentryRenderingParentSpan = compositionLocalOf {
    ImmutableHolder(
        getRootSpan()
            ?.startChild(
                OP_PARENT_RENDER,
                "Jetpack Compose Initial Render",
                SpanOptions().apply {
                    isTrimStart = true
                    isTrimEnd = true
                    isIdle = true
                },
            ),
    )
}

@Composable
fun SmileSentryTraced(
    tag: String,
    modifier: Modifier = Modifier,
    enableUserInteractionTracing: Boolean = true,
    content: @Composable BoxScope.() -> Unit,
) {
    val parentCompositionSpan = localSentryCompositionParentSpan.current
    val parentRenderingSpan = localSentryRenderingParentSpan.current
    val compositionSpan = parentCompositionSpan.item?.startChild(OP_COMPOSE, tag)
    val firstRendered = remember { ImmutableHolder(false) }

    val baseModifier = if (enableUserInteractionTracing) modifier.testTag(tag) else modifier

    Box(
        modifier = baseModifier
            .drawWithContent {
                val renderSpan = if (!firstRendered.item) {
                    parentRenderingSpan.item?.startChild(
                        OP_RENDER,
                        tag,
                    )
                } else {
                    null
                }
                drawContent()
                firstRendered.item = true
                renderSpan?.finish()
            },
    ) {
        content()
    }
    compositionSpan?.finish()
}
