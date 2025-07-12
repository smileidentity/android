package com.smileidentity.sample.compose.jobs

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smileidentity.sample.viewmodel.JobViewModel

@Composable
fun OrchestratedJobsScreen(
    isProduction: Boolean,
    modifier: Modifier = Modifier,
    jobViewModel: JobViewModel = hiltViewModel(),
    viewModel: JobViewModel = viewModel(),
) {
    // jobViewModel.getLocalJobs()
    // val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // val jobs by viewModel.jobs.collectAsStateWithLifecycle()
    // Box(
    //     contentAlignment = Center,
    //     modifier = modifier
    //         .fillMaxSize(),
    // ) {
    //     when (uiState.processingState) {
    //         ProcessingState.InProgress -> CircularProgressIndicator()
    //         ProcessingState.Error -> ErrorScreen { /* Using a Flow, which automatically retries */ }
    //         ProcessingState.Success -> JobsListScreen(jobs)
    //     }
    // }
}
