package com.smileidentity.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
import androidx.fragment.app.Fragment

class SelfieFragment : Fragment() {

    companion object {
        @JvmStatic
        fun newInstance() = SelfieFragment()
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
            setContent { SelfieCaptureOrPermissionScreen() }
        }
    }
}
