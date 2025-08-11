package com.smileidentity.navigation.builder

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ramcosta.composedestinations.animations.defaults.DefaultFadingTransitions
import com.ramcosta.composedestinations.generated.navigation.destinations.OrchestratedCaptureScreenDestination
import com.ramcosta.composedestinations.generated.navigation.destinations.OrchestratedInstructionsScreenDestination
import com.ramcosta.composedestinations.generated.navigation.destinations.OrchestratedPreviewScreenDestination
import com.ramcosta.composedestinations.generated.navigation.destinations.OrchestratedProcessingScreenDestination
import com.ramcosta.composedestinations.spec.DestinationSpec
import com.ramcosta.composedestinations.spec.Direction
import com.ramcosta.composedestinations.spec.DirectionNavHostGraphSpec
import com.ramcosta.composedestinations.spec.TypedRoute
import com.smileidentity.navigation.dsl.SmileIDDsl
import com.smileidentity.navigation.navigation.SmileIDNavigationHost

@Suppress("FunctionName")
class SmileID {
    private var instructions = false
    private var capture = false
    private var preview = false

    companion object {
        fun Builder() = SmileID()
    }

    fun setInstructionsScreen(enabled: Boolean) = apply {
        instructions = enabled
    }

    fun captureSelfie(enabled: Boolean) = apply {
        capture = enabled
    }

    fun showPreview(enabled: Boolean) = apply {
        preview = enabled
    }

    @Composable
    fun build(modifier: Modifier = Modifier) {
        val startDestination =
            if (instructions) {
                OrchestratedInstructionsScreenDestination
            } else {
                OrchestratedCaptureScreenDestination
            }
        val destinations = buildList {
            if (instructions) add(OrchestratedInstructionsScreenDestination)
            if (capture) add(OrchestratedCaptureScreenDestination)
            if (preview) add(OrchestratedPreviewScreenDestination)
            add(OrchestratedProcessingScreenDestination)
        }

        buildSmileIDNavigationHost(
            modifier = modifier,
            startDestination = startDestination,
            destinations = destinations,
        )
    }
}

@SmileIDDsl
class SmileIDNavigationScope {
    var instructions: Boolean = false
    var capture: Boolean = false
    var preview: Boolean = false
}

@Composable
inline fun SmileID(modifier: Modifier = Modifier, init: SmileIDNavigationScope.() -> Unit) {
    val scope = SmileIDNavigationScope().apply(init)
    val startDestination =
        if (scope.instructions) {
            OrchestratedInstructionsScreenDestination
        } else {
            OrchestratedCaptureScreenDestination
        }
    val destinations = buildList {
        if (scope.instructions) add(OrchestratedInstructionsScreenDestination)
        if (scope.capture) add(OrchestratedCaptureScreenDestination)
        if (scope.preview) add(OrchestratedPreviewScreenDestination)
        add(OrchestratedProcessingScreenDestination)
    }

    buildSmileIDNavigationHost(
        modifier = modifier,
        startDestination = startDestination,
        destinations = destinations,
    )
}

@Composable
fun buildSmileIDNavigationHost(
    modifier: Modifier = Modifier,
    startDestination: TypedRoute<Unit>,
    destinations: List<DestinationSpec>,
) {
    SmileIDNavigationHost(
        modifier = modifier,
        startDestination = OrchestratedInstructionsScreenDestination,
        destinations = object : DirectionNavHostGraphSpec {
            override val route = "navigation/root"
            override val startRoute: TypedRoute<Unit>
                get() = startDestination
            override val destinations: List<DestinationSpec>
                get() = destinations
            override val defaultTransitions = DefaultFadingTransitions
            override val defaultStartDirection: Direction
                get() = OrchestratedInstructionsScreenDestination
        },
    )
}
