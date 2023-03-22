package com.smileidentity.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
import androidx.fragment.app.Fragment
import com.smileidentity.SmileIdentity
import com.smileidentity.compose.SmartSelfieRegistrationScreen
import com.smileidentity.results.SmartSelfieResult

class SmartSelfieFragment private constructor(
    private val callback: SmartSelfieResult.Callback,
) : Fragment() {

    companion object {
        @JvmStatic
        @JvmOverloads
        fun newInstance(
            callback: SmartSelfieResult.Callback = SmartSelfieResult.Callback { },
        ) = SmartSelfieFragment(callback)
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
                    allowAgentMode = true,
                    onResult = callback,
                )
            }
        }
    }
}
