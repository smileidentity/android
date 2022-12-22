package com.smileidentity.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
import androidx.fragment.app.Fragment
import com.smileidentity.ui.compose.SelfieCaptureOrPermissionScreen
import com.smileidentity.ui.core.SelfieCaptureResultCallback

class SelfieFragment private constructor(
    private val callback: SelfieCaptureResultCallback
) : Fragment() {

    companion object {
        @JvmStatic
        fun newInstance(
            callback: SelfieCaptureResultCallback
        ) = SelfieFragment(callback)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            // Dispose of the Composition when the view's LifecycleOwner is destroyed. see:
            // https://developer.android.com/jetpack/compose/interop/interop-apis#compose-in-fragments
            setViewCompositionStrategy(DisposeOnViewTreeLifecycleDestroyed)
            setContent { SelfieCaptureOrPermissionScreen(callback) }
        }
    }
}
