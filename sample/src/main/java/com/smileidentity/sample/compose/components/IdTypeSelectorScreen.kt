package com.smileidentity.sample.compose.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smileidentity.models.IdInfo
import com.smileidentity.models.JobType
import com.smileidentity.sample.toast
import com.smileidentity.sample.viewmodel.IdTypeSelectorAndFieldInputViewModel
import com.smileidentity.viewmodel.viewModelFactory

@Composable
fun IdTypeSelectorScreen(
    jobType: JobType,
    modifier: Modifier = Modifier,
    viewModel: IdTypeSelectorAndFieldInputViewModel = viewModel(
        factory = viewModelFactory { IdTypeSelectorAndFieldInputViewModel(jobType) },
    ),
    onResult: (IdInfo) -> Unit,
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    when {
        uiState.errorMessage != null -> {
            val context = LocalContext.current
            LaunchedEffect(uiState.errorMessage) {
                context.toast("Error loading ID Types: ${uiState.errorMessage}")
            }
        }

        !uiState.hasIdTypeSelectionBeenConfirmed -> IdSelectorScreen(
            modifier = modifier,
            onNext = {
                viewModel.onIdTypeConfirmed()
                onResult(viewModel.currentIdInfo)
            },
        )
    }
}
