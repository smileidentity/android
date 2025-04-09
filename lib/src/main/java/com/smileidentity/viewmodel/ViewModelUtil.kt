package com.smileidentity.viewmodel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController

@Suppress("UNCHECKED_CAST")
inline fun <VM : ViewModel> viewModelFactory(crossinline f: () -> VM) =
    object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T = f() as T
    }

@Composable
inline fun <reified VM : ViewModel> createSmileViewModel(
    navController: NavController,
    navGraphRoute: String,
    navBackStackEntry: NavBackStackEntry? = null,
    crossinline factory: (NavBackStackEntry) -> VM,
): VM {
    val parentEntry = remember(navBackStackEntry) {
        navController.getBackStackEntry(navGraphRoute)
    }

    return viewModel(
        viewModelStoreOwner = parentEntry,
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return factory(parentEntry) as T
            }
        },
    )
}

// @Composable
// internal inline fun <reified VM : ViewModel> smileViewModel(
//     viewModelStoreOwner: ViewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current) {
//         "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
//     },
//     savedStateRegistryOwner: SavedStateRegistryOwner = LocalSavedStateRegistryOwner.current,
//     noinline extraParams: (() -> Any)? = null
// ): VM = viewModel(
//     viewModelStoreOwner = viewModelStoreOwner,
//     factory = ViewModelFactory(
//         owner = savedStateRegistryOwner,
//         defaultArgs = (savedStateRegistryOwner as? NavBackStackEntry)?.arguments,
//         extraParams = if (extraParams != null) mapOf(VM::class.java to extraParams) else emptyMap()
//     ),
// )

// internal class ViewModelFactory(
//     owner: SavedStateRegistryOwner,
//     defaultArgs: Bundle?,
//     private val extraParams: Map<Class<out ViewModel>, () -> Any> = emptyMap()
// ) : AbstractSavedStateViewModelFactory(
//     owner = owner,
//     defaultArgs = defaultArgs,
// ) {
//     @Suppress("UNCHECKED_CAST")
//     override fun <T : ViewModel> create(
//         key: String,
//         modelClass: Class<T>,
//         handle: SavedStateHandle,
//     ): T = when (modelClass) {
//         SmartSelfieEnhancedViewModel::class.java -> {
//             val params = extraParams[SmartSelfieEnhancedViewModel::class.java]?.invoke() as? SmartSelfieParams
//                 ?: throw IllegalArgumentException("Parameters for SmartSelfieEnhancedViewModel are required")
//
//             SmartSelfieEnhancedViewModel(
//                 userId = params.userId,
//                 isEnroll = params.isEnroll,
//                 selfieQualityModel = params.selfieQualityModel,
//                 metadata = params.metadata,
//                 allowNewEnroll = params.allowNewEnroll,
//                 skipApiSubmission = params.skipApiSubmission,
//                 extraPartnerParams = params.extraPartnerParams,
//                 onResult = params.onResult
//             )
//         }
//         else -> throw RuntimeException("Unknown ViewModel $modelClass")
//     } as T
// }

