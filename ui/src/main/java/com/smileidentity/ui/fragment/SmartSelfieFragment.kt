package com.smileidentity.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
import androidx.fragment.app.Fragment
import com.smileidentity.ui.compose.SmartSelfieOrPermissionScreen
import com.smileidentity.ui.core.SmartSelfieCallback

class SmartSelfieFragment private constructor(
    private val callback: SmartSelfieCallback,
) : Fragment() {

    companion object {
        @JvmStatic
        @JvmOverloads
        fun newInstance(
            callback: SmartSelfieCallback = SmartSelfieCallback { },
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
                SmartSelfieOrPermissionScreen(
                    agentMode = true,
                    manualCaptureMode = true,
                    onResult = callback,
                )
            }
        }
    }
}
