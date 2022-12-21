package com.smileidentity.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider

class SelfieFragment : Fragment() {

    companion object {
        fun newInstance() = SelfieFragment()
    }

    private lateinit var viewModel: SelfieViewModel

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[SelfieViewModel::class.java]
        // TODO: Use the ViewModel
    }
}
