package com.smileidentity.sample.compose.jobs

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smileidentity.compose.components.ProcessingState
import com.smileidentity.sample.compose.components.ErrorScreen
import com.smileidentity.sample.viewmodel.JobsViewModel
import com.smileidentity.viewmodel.viewModelFactory

@Composable
fun OrchestratedJobsScreen(
    isProduction: Boolean,
    modifier: Modifier = Modifier,
    viewModel: JobsViewModel = viewModel(
        key = isProduction.toString(),
        factory = viewModelFactory { JobsViewModel(isProduction) },
    ),
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    val jobs = viewModel.jobs.collectAsStateWithLifecycle().value
    Box(
        contentAlignment = Center,
        modifier = modifier
            .fillMaxSize(),
    ) {
        when (uiState.processingState) {
            ProcessingState.InProgress -> CircularProgressIndicator()
            ProcessingState.Error -> ErrorScreen { /* Using a Flow, which automatically retries */ }
            ProcessingState.Success -> JobsListScreen(jobs)
        }
    }
}
