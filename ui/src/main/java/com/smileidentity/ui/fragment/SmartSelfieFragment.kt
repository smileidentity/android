package com.smileidentity.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
import androidx.fragment.app.Fragment
import com.smileidentity.networking.SmileIdentity
import com.smileidentity.ui.compose.SmartSelfieRegistrationScreen
import com.smileidentity.ui.core.SmartSelfieResult
import com.smileidentity.ui.core.randomSessionId
import com.smileidentity.ui.core.randomUserId

class SmartSelfieFragment private constructor(
    private val userId: String,
    private val allowAgentMode: Boolean,
    private val allowManualCapture: Boolean,
    private val sessionId: String,
    private val onResult: SmartSelfieResult.Callback,
) : Fragment() {

    companion object {
        @JvmStatic
        @JvmOverloads
        fun newInstance(
            userId: String = randomUserId(),
            allowAgentMode: Boolean = false,
            allowManualCapture: Boolean = false,
            sessionId: String = randomSessionId(),
            onResult: SmartSelfieResult.Callback = SmartSelfieResult.Callback {},
        ) = SmartSelfieFragment(
            userId = userId,
            allowAgentMode = allowAgentMode,
            allowManualCapture = allowManualCapture,
            sessionId = sessionId,
            onResult = onResult,
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            // Dispose of the Composition when the view's LifecycleOwner is destroyed. see:
            // https://developer.android.com/jetpack/compose/interop/interop-apis#compose-in-fragments
            setViewCompositionStrategy(DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                SmileIdentity.SmartSelfieRegistrationScreen(
                    userId = userId,
                    allowAgentMode = allowAgentMode,
                    allowManualCapture = allowManualCapture,
                    sessionId = sessionId,
                    onResult = onResult,
                )
            }
        }
    }
}
