package com.smileidentity.viewmodel

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalSavedStateRegistryOwner
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.savedstate.SavedStateRegistryOwner
import com.ramcosta.composedestinations.generated.destinations.OrchestratedSelfieCaptureScreenDestination
import com.smileidentity.compose.selfie.viewmodel.OrchestratedSelfieViewModel
import kotlinx.collections.immutable.persistentMapOf

@Suppress("UNCHECKED_CAST")
inline fun <VM : ViewModel> viewModelFactory(crossinline f: () -> VM) =
    object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T = f() as T
    }

@Composable
internal inline fun <reified VM : ViewModel> smileViewModel(
    viewModelStoreOwner: ViewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current) {
        "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
    },
    savedStateRegistryOwner: SavedStateRegistryOwner = LocalSavedStateRegistryOwner.current,
): VM = viewModel(
    viewModelStoreOwner = viewModelStoreOwner,
    factory = ViewModelFactory(
        owner = savedStateRegistryOwner,
        defaultArgs = (savedStateRegistryOwner as? NavBackStackEntry)?.arguments,
    ),
)

internal class ViewModelFactory(
    owner: SavedStateRegistryOwner,
    defaultArgs: Bundle?,
) : AbstractSavedStateViewModelFactory(
    owner = owner,
    defaultArgs = defaultArgs,
) {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        key: String,
        modelClass: Class<T>,
        handle: SavedStateHandle,
    ): T = when (modelClass) {
        OrchestratedSelfieViewModel::class.java -> OrchestratedSelfieViewModel(
            navArgs = OrchestratedSelfieCaptureScreenDestination.argsFrom(
                savedStateHandle = handle,
            ),
            metadata = mutableListOf(),
            extraPartnerParams = persistentMapOf(),
        )

        else -> throw RuntimeException("Unknown ViewModel $modelClass")
    } as T
}
