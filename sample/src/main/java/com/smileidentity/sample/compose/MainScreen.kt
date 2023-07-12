package com.smileidentity.sample.compose

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PlainTooltipBox
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.smileidentity.SmileID
import com.smileidentity.compose.BiometricKYC
import com.smileidentity.compose.DocumentVerification
import com.smileidentity.compose.SmartSelfieAuthentication
import com.smileidentity.compose.SmartSelfieEnrollment
import com.smileidentity.models.Document
import com.smileidentity.models.IdInfo
import com.smileidentity.models.JobType
import com.smileidentity.sample.BottomNavigationScreen
import com.smileidentity.sample.ProductScreen
import com.smileidentity.sample.R
import com.smileidentity.sample.compose.components.IdTypeSelectorAndFieldInputScreen
import com.smileidentity.sample.compose.jobs.OrchestratedJobsScreen
import com.smileidentity.sample.viewmodel.MainScreenUiState.Companion.startScreen
import com.smileidentity.sample.viewmodel.MainScreenViewModel
import com.smileidentity.util.randomJobId
import com.smileidentity.util.randomUserId
import com.smileidentity.viewmodel.viewModelFactory
import kotlinx.coroutines.launch
import java.net.URL

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Preview
@Composable
fun MainScreen(
    viewModel: MainScreenViewModel = viewModel(
        factory = viewModelFactory { MainScreenViewModel() },
    ),
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    val coroutineScope = rememberCoroutineScope()
    val navController = rememberNavController()
    val currentRoute = navController
        .currentBackStackEntryFlow
        .collectAsStateWithLifecycle(initialValue = navController.currentBackStackEntry)
        .value

    // TODO: Switch to BottomNavigationScreen.entries once we are using Kotlin 1.9
    val bottomNavItems = remember { BottomNavigationScreen.values() }
    val bottomNavSelection = uiState.bottomNavSelection

    val pendingJobCount = viewModel.pendingJobCount
        .collectAsStateWithLifecycle()
        .value

    // Show up button when not on a BottomNavigationScreen
    val showUpButton = currentRoute?.destination?.route?.let { route ->
        bottomNavItems.none { it.route.contains(route) }
    } ?: false

    val snackbarHostState = remember { SnackbarHostState() }
    // TODO: Could there be a bug here in case we have the same message twice in a row? (i.e. the same result)
    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { message ->
            coroutineScope.launch {
                snackbarHostState.showSnackbar(message)
            }
        }
    }

    val clipboardManager = LocalClipboardManager.current
    LaunchedEffect(uiState.clipboardText) {
        uiState.clipboardText?.let { text ->
            coroutineScope.launch {
                clipboardManager.setText(text)
            }
        }
    }

    SmileIDTheme {
        Surface {
            Scaffold(
                snackbarHost = {
                    SnackbarHost(snackbarHostState) {
                        Snackbar(
                            snackbarData = it,
                            actionColor = MaterialTheme.colorScheme.tertiary,
                        )
                    }
                },
                topBar = {
                    TopAppBar(
                        title = { Text(stringResource(id = uiState.appBarTitle)) },
                        navigationIcon = {
                            if (showUpButton) {
                                IconButton(onClick = { navController.navigateUp() }) {
                                    Icon(
                                        imageVector = Icons.Filled.ArrowBack,
                                        contentDescription = stringResource(R.string.back),
                                    )
                                }
                            }
                        },
                        actions = {
                            FilterChip(
                                selected = uiState.isProduction,
                                onClick = viewModel::toggleEnvironment,
                                leadingIcon = {
                                    if (uiState.isProduction) {
                                        Icon(
                                            imageVector = Icons.Filled.Warning,
                                            contentDescription = stringResource(
                                                R.string.production,
                                            ),
                                        )
                                    }
                                },
                                label = { Text(stringResource(id = uiState.environmentName)) },
                            )
                            if (bottomNavSelection == BottomNavigationScreen.Jobs) {
                                PlainTooltipBox(
                                    tooltip = {
                                        Text(stringResource(R.string.jobs_clear_jobs_icon_tooltip))
                                    },
                                ) {
                                    IconButton(
                                        onClick = viewModel::clearJobs,
                                        modifier = Modifier.tooltipAnchor(),
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = null,
                                            modifier = Modifier.size(24.dp),
                                        )
                                    }
                                }
                            }
                        },
                    )
                },
                bottomBar = {
                    // Don't show bottom bar when navigating to any product screens
                    val currentRouteValue = remember(currentRoute) {
                        derivedStateOf { currentRoute?.destination?.route ?: "" }
                    }.value
                    if (bottomNavItems.none { it.route.contains(currentRouteValue) }) {
                        return@Scaffold
                    }
                    NavigationBar {
                        bottomNavItems.forEach {
                            NavigationBarItem(
                                selected = it == bottomNavSelection,
                                icon = {
                                    BadgedBox(
                                        badge = {
                                            if (it == BottomNavigationScreen.Jobs &&
                                                pendingJobCount > 0
                                            ) {
                                                Badge { Text(text = pendingJobCount.toString()) }
                                            }
                                        },
                                    ) {
                                        val imageVector = if (it == bottomNavSelection) {
                                            it.selectedIcon
                                        } else {
                                            it.unselectedIcon
                                        }
                                        Icon(imageVector, stringResource(it.label))
                                    }
                                },
                                label = { Text(stringResource(it.label)) },
                                onClick = {
                                    navController.navigate(it.route) {
                                        popUpTo(BottomNavigationScreen.Home.route)
                                        launchSingleTop = true
                                    }
                                },
                            )
                        }
                    }
                },
                content = {
                    NavHost(
                        navController,
                        startScreen.route,
                        Modifier
                            .padding(it)
                            .consumeWindowInsets(it),
                    ) {
                        composable(BottomNavigationScreen.Home.route) {
                            viewModel.onHomeSelected()
                            ProductSelectionScreen { navController.navigate(it.route) }
                        }
                        composable(BottomNavigationScreen.Jobs.route) {
                            viewModel.onJobsSelected()
                            OrchestratedJobsScreen(uiState.isProduction)
                        }
                        composable(BottomNavigationScreen.Resources.route) {
                            viewModel.onResourcesSelected()
                            ResourcesScreen()
                        }
                        composable(BottomNavigationScreen.AboutUs.route) {
                            viewModel.onAboutUsSelected()
                            AboutUsScreen()
                        }
                        composable(ProductScreen.SmartSelfieEnrollment.route) {
                            viewModel.onSmartSelfieEnrollmentSelected()
                            val userId = rememberSaveable { randomUserId() }
                            val jobId = rememberSaveable { randomJobId() }
                            SmileID.SmartSelfieEnrollment(
                                userId = userId,
                                jobId = jobId,
                                allowAgentMode = true,
                                showInstructions = true,
                            ) { result ->
                                viewModel.onSmartSelfieEnrollmentResult(userId, jobId, result)
                                navController.popBackStack()
                            }
                        }
                        composable(ProductScreen.SmartSelfieAuthentication.route) {
                            viewModel.onSmartSelfieAuthenticationSelected()
                            var userId by rememberSaveable {
                                val clipboardText = clipboardManager.getText()?.text
                                // Autofill the value of User ID as it was likely just copied
                                mutableStateOf(clipboardText?.takeIf { "user-" in it } ?: "")
                            }
                            AlertDialog(
                                title = { Text(stringResource(R.string.user_id_dialog_title)) },
                                text = {
                                    OutlinedTextField(
                                        value = userId,
                                        onValueChange = { newValue -> userId = newValue.trim() },
                                        label = { Text(stringResource(R.string.user_id_label)) },
                                        supportingText = {
                                            Text(
                                                stringResource(
                                                    R.string.user_id_dialog_supporting_text,
                                                ),
                                            )
                                        },
                                        // This is needed to allow the dialog to grow vertically in
                                        // case of a long User ID
                                        modifier = Modifier.wrapContentHeight(unbounded = true),
                                    )
                                },
                                onDismissRequest = { navController.popBackStack() },
                                dismissButton = {
                                    OutlinedButton(onClick = { navController.popBackStack() }) {
                                        Text(stringResource(R.string.cancel))
                                    }
                                },
                                confirmButton = {
                                    Button(
                                        enabled = userId.isNotBlank(),
                                        onClick = {
                                            navController.navigate(
                                                "${ProductScreen.SmartSelfieAuthentication.route}/$userId", // ktlint-disable max-line-length
                                            ) { popUpTo(BottomNavigationScreen.Home.route) }
                                        },
                                    ) { Text(stringResource(R.string.cont)) }
                                },
                                // This is needed to allow the dialog to grow vertically in case of
                                // a long User ID
                                modifier = Modifier.wrapContentHeight(),
                            )
                        }
                        composable(ProductScreen.SmartSelfieAuthentication.route + "/{userId}") {
                            viewModel.onSmartSelfieAuthenticationSelected()
                            val userId = it.arguments?.getString("userId")!!
                            val jobId = rememberSaveable { randomJobId() }
                            SmileID.SmartSelfieAuthentication(
                                userId = userId,
                                jobId = jobId,
                                allowAgentMode = true,
                            ) { result ->
                                viewModel.onSmartSelfieAuthenticationResult(userId, jobId, result)
                                navController.popBackStack()
                            }
                        }
                        composable(ProductScreen.EnhancedKyc.route) {
                            viewModel.onEnhancedKycSelected()
                            OrchestratedEnhancedKycScreen { result ->
                                viewModel.onEnhancedKycResult(result)
                                navController.popBackStack()
                            }
                        }
                        composable(ProductScreen.BiometricKyc.route) {
                            viewModel.onBiometricKycSelected()
                            var idInfo: IdInfo? by remember { mutableStateOf(null) }
                            if (idInfo == null) {
                                IdTypeSelectorAndFieldInputScreen(
                                    jobType = JobType.BiometricKyc,
                                    onResult = { idInfo = it },
                                )
                            }
                            idInfo?.let {
                                val url = remember {
                                    URL("https://smileidentity.com/privacy-policy")
                                }
                                val userId = rememberSaveable { randomUserId() }
                                val jobId = rememberSaveable { randomJobId() }
                                SmileID.BiometricKYC(
                                    idInfo = it,
                                    userId = userId,
                                    jobId = jobId,
                                    partnerIcon = painterResource(
                                        id = com.smileidentity.R.drawable.si_logo_with_text,
                                    ),
                                    partnerName = "Smile Identity",
                                    productName = it.idType,
                                    partnerPrivacyPolicy = url,
                                ) { result ->
                                    viewModel.onBiometricKycResult(userId, jobId, result)
                                    navController.popBackStack()
                                }
                            }
                        }
                        composable(ProductScreen.DocumentVerification.route) {
                            viewModel.onDocumentVerificationSelected()
                            DocumentVerificationIdTypeSelector { country, idType ->
                                navController.navigate(
                                    "${ProductScreen.DocumentVerification.route}/$country/$idType",
                                ) { popUpTo(ProductScreen.DocumentVerification.route) }
                            }
                        }
                        composable(
                            ProductScreen.DocumentVerification.route + "/{countryCode}/{idType}",
                        ) {
                            viewModel.onDocumentVerificationSelected()
                            val userId = rememberSaveable { randomUserId() }
                            val jobId = rememberSaveable { randomJobId() }
                            val documentType = remember(it) {
                                Document(
                                    it.arguments?.getString("countryCode")!!,
                                    it.arguments?.getString("idType")!!,
                                )
                            }
                            SmileID.DocumentVerification(
                                userId = userId,
                                jobId = jobId,
                                idType = documentType,
                                showInstructions = true,
                            ) { result ->
                                viewModel.onDocumentVerificationResult(userId, jobId, result)
                                navController.popBackStack(
                                    route = BottomNavigationScreen.Home.route,
                                    inclusive = false,
                                )
                            }
                        }
                    }
                },
            )
        }
    }
}
