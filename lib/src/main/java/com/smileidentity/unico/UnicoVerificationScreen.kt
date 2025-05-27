package com.smileidentity.unico

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@SuppressLint("ComposeViewModelInjection", "ComposeModifierMissing")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnicoVerificationScreen(
    duiType: String,
    duiValue: String,
    friendlyName: String,
    onSuccess: () -> Unit,
    onFailure: () -> Unit,
    callbackUri: String = "https://yourapp.com/unico/callback",
    flow: String = UnicoProcessManager.FLOW_ID_LIVE_TRUST,
    purpose: String = UnicoProcessManager.PURPOSE_CREDIT_PROCESS,
    onClose: () -> Unit,
) {
    val context = LocalContext.current
    val viewModel: UnicoProcessViewModel = viewModel(
        factory = UnicoProcessViewModelFactory(context),
    )
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.createProcess(
            callbackUri = callbackUri,
            flow = flow,
            duiType = duiType,
            duiValue = duiValue,
            friendlyName = friendlyName,
            purpose = purpose,
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Identity Verification") },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                        )
                    }
                },
            )
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                    )
                }

                state.error != null -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = "Error: ${state.error}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error,
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                viewModel.clearError()
                                viewModel.createProcess(
                                    callbackUri, flow, duiType, duiValue, friendlyName, purpose,
                                )
                            },
                        ) {
                            Text("Retry")
                        }
                    }
                }

                state.webLink != null -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        UnicoWebView(
                            webLink = state.webLink!!,
                            callbackUri = callbackUri,
                            onProgressChanged = { progress ->
                                viewModel.updateProgress(progress)
                            },
                            onSuccess = {
                                viewModel.setProcessComplete()
                                onSuccess()
                            },
                            onFailure = {
                                onFailure()
                            },
                            modifier = Modifier.fillMaxSize(),
                        )

                        if (state.progress < 100) {
                            LinearProgressIndicator(
                                progress = { state.progress / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.TopCenter),
                            )
                        }
                    }
                }

                else -> {
                    Text(
                        text = "Initializing verification process...",
                        modifier = Modifier.align(Alignment.Center),
                    )
                }
            }
        }
    }
}
