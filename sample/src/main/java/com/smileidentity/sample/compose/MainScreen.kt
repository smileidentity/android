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
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch
import java.net.URL

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    viewModel: MainScreenViewModel = viewModel(
        factory = viewModelFactory { MainScreenViewModel() },
    ),
) {
    val coroutineScope = rememberCoroutineScope()
    val navController = rememberNavController()
    val currentRoute by navController
        .currentBackStackEntryFlow
        .collectAsStateWithLifecycle(initialValue = navController.currentBackStackEntry)

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val bottomNavSelection = uiState.bottomNavSelection

    // TODO: Switch to BottomNavigationScreen.entries once we are using Kotlin 1.9
    val bottomNavItems = remember { BottomNavigationScreen.values().toList().toImmutableList() }
    // Show up button when not on a BottomNavigationScreen
    val showUpButton = currentRoute?.destination?.route?.let { route ->
        bottomNavItems.none { it.route.contains(route) }
    } ?: false

    val clipboardManager = LocalClipboardManager.current
    LaunchedEffect(uiState.clipboardText) {
        uiState.clipboardText?.let { text ->
            coroutineScope.launch {
                clipboardManager.setText(text)
            }
        }
    }
    Surface(
        modifier = modifier,
    ) {
        Scaffold(
            snackbarHost = { Snackbar() },
            topBar = {
                TopBar(
                    showUpButton = showUpButton,
                    onNavigateUp = { navController.navigateUp() },
                    isJobsScreenSelected = bottomNavSelection == BottomNavigationScreen.Jobs,
                )
            },
            bottomBar = {
                // Don't show bottom bar when navigating to any product screens
                val showBottomBar by remember(currentRoute) {
                    derivedStateOf {
                        bottomNavItems.any {
                            it.route.contains(
                                currentRoute?.destination?.route ?: "",
                            )
                        }
                    }
                }
                if (showBottomBar) {
                    BottomBar(
                        bottomNavItems = bottomNavItems,
                        bottomNavSelection = bottomNavSelection,
                        pendingJobCount = uiState.pendingJobCount,
                    ) {
                        navController.navigate(it.route) {
                            popUpTo(BottomNavigationScreen.Home.route)
                            launchSingleTop = true
                        }
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
                        LaunchedEffect(Unit) { viewModel.onHomeSelected() }
                        ProductSelectionScreen(
                            onProductSelected = { navController.navigate(it.route) },
                        )
                    }
                    composable(BottomNavigationScreen.Jobs.route) {
                        LaunchedEffect(Unit) { viewModel.onJobsSelected() }
                        OrchestratedJobsScreen(uiState.isProduction)
                    }
                    composable(BottomNavigationScreen.Resources.route) {
                        LaunchedEffect(Unit) { viewModel.onResourcesSelected() }
                        ResourcesScreen()
                    }
                    composable(BottomNavigationScreen.Settings.route) {
                        LaunchedEffect(Unit) { viewModel.onSettingsSelected() }
                        SettingsScreen()
                    }
                    composable(ProductScreen.SmartSelfieEnrollment.route) {
                        LaunchedEffect(Unit) { viewModel.onSmartSelfieEnrollmentSelected() }
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
                        LaunchedEffect(Unit) { viewModel.onSmartSelfieAuthenticationSelected() }
                        SmartSelfieAuthenticationUserIdInputDialog(
                            onDismiss = navController::popBackStack,
                            onConfirm = { userId ->
                                navController.navigate(
                                    "${ProductScreen.SmartSelfieAuthentication.route}/$userId",
                                ) { popUpTo(BottomNavigationScreen.Home.route) }
                            },
                        )
                    }
                    composable(ProductScreen.SmartSelfieAuthentication.route + "/{userId}") {
                        LaunchedEffect(Unit) { viewModel.onSmartSelfieAuthenticationSelected() }
                        val userId = rememberSaveable { it.arguments?.getString("userId")!! }
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
                        LaunchedEffect(Unit) { viewModel.onEnhancedKycSelected() }
                        OrchestratedEnhancedKycScreen { result ->
                            viewModel.onEnhancedKycResult(result)
                            navController.popBackStack()
                        }
                    }
                    composable(ProductScreen.BiometricKyc.route) {
                        LaunchedEffect(Unit) { viewModel.onBiometricKycSelected() }
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
                        LaunchedEffect(Unit) { viewModel.onDocumentVerificationSelected() }
                        DocumentVerificationIdTypeSelector { country, idType ->
                            navController.navigate(
                                "${ProductScreen.DocumentVerification.route}/$country/$idType",
                            ) { popUpTo(ProductScreen.DocumentVerification.route) }
                        }
                    }
                    composable(
                        ProductScreen.DocumentVerification.route + "/{countryCode}/{idType}",
                    ) {
                        LaunchedEffect(Unit) { viewModel.onDocumentVerificationSelected() }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    showUpButton: Boolean,
    onNavigateUp: () -> Unit,
    isJobsScreenSelected: Boolean,
    viewModel: MainScreenViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    TopAppBar(
        title = { Text(stringResource(id = uiState.appBarTitle)) },
        navigationIcon = {
            if (showUpButton) {
                IconButton(onClick = onNavigateUp) {
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
            if (isJobsScreenSelected) {
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BottomBar(
    bottomNavItems: ImmutableList<BottomNavigationScreen>,
    bottomNavSelection: BottomNavigationScreen,
    pendingJobCount: Int,
    onBottomNavItemSelected: (BottomNavigationScreen) -> Unit,
) {
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
                onClick = { onBottomNavItemSelected(it) },
            )
        }
    }
}

@Composable
private fun SmartSelfieAuthenticationUserIdInputDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    val clipboardManager = LocalClipboardManager.current
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
        onDismissRequest = onDismiss,
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        },
        confirmButton = {
            Button(
                enabled = userId.isNotBlank(),
                onClick = { onConfirm(userId) },
            ) { Text(stringResource(R.string.cont)) }
        },
        // This is needed to allow the dialog to grow vertically in case of
        // a long User ID
        modifier = Modifier.wrapContentHeight(),
    )
}

@Composable
private fun Snackbar(viewModel: MainScreenViewModel = viewModel()) {
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val snackbarMessage = viewModel.uiState.collectAsStateWithLifecycle().value.snackbarMessage

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let { coroutineScope.launch { snackbarHostState.showSnackbar(it.value) } }
    }

    SnackbarHost(snackbarHostState) {
        Snackbar(
            snackbarData = it,
            actionColor = MaterialTheme.colorScheme.tertiary,
        )
    }
}
