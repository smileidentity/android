package com.smileidentity.sample.compose

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
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
import com.smileidentity.models.JobResult
import com.smileidentity.models.JobType
import com.smileidentity.results.SmileIDResult
import com.smileidentity.sample.BottomNavigationScreen
import com.smileidentity.sample.ProductScreen
import com.smileidentity.sample.R
import com.smileidentity.sample.Screen
import com.smileidentity.sample.compose.components.IdTypeSelectorAndFieldInputScreen
import com.smileidentity.sample.compose.jobs.OrchestratedJobsScreen
import com.smileidentity.sample.jobResultMessageBuilder
import com.smileidentity.sample.model.toJob
import com.smileidentity.sample.repo.DataStoreRepository
import com.smileidentity.sample.showSnackbar
import com.smileidentity.sample.viewmodel.MainScreenViewModel
import com.smileidentity.util.randomJobId
import com.smileidentity.util.randomUserId
import com.smileidentity.viewmodel.viewModelFactory
import kotlinx.coroutines.launch
import timber.log.Timber
import java.net.URL

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Preview
@Composable
fun MainScreen(
    viewModel: MainScreenViewModel = viewModel(
        factory = viewModelFactory { MainScreenViewModel() },
    ),
) {
    val coroutineScope = rememberCoroutineScope()
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val clipboardManager = LocalClipboardManager.current
    val startScreen = BottomNavigationScreen.Home
    val currentRoute = navController
        .currentBackStackEntryFlow
        .collectAsStateWithLifecycle(initialValue = navController.currentBackStackEntry)

    // TODO: Switch to BottomNavigationScreen.entries once we are using Kotlin 1.9
    val bottomNavItems = remember { BottomNavigationScreen.values() }
    var bottomNavSelection: Screen by remember { mutableStateOf(startScreen) }

    // Show up button when not on a BottomNavigationScreen
    val showUpButton = currentRoute.value?.destination?.route?.let { route ->
        bottomNavItems.none { it.route.contains(route) }
    } ?: false
    var isProduction by rememberSaveable { mutableStateOf(false) }

    SmileIDTheme {
        Surface {
            var currentScreenTitle by remember { mutableStateOf(R.string.app_name) }
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
                        title = { Text(stringResource(currentScreenTitle)) },
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
                                selected = isProduction,
                                onClick = {
                                    isProduction = !isProduction
                                    SmileID.setEnvironment(useSandbox = !isProduction)
                                },
                                leadingIcon = {
                                    if (isProduction) {
                                        Icon(
                                            imageVector = Icons.Filled.Warning,
                                            contentDescription = stringResource(
                                                R.string.production,
                                            ),
                                        )
                                    }
                                },
                                label = {
                                    val environmentName = if (isProduction) {
                                        R.string.production
                                    } else {
                                        R.string.sandbox
                                    }
                                    Text(stringResource(environmentName))
                                },
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
                                        )
                                    }
                                }
                            }
                        },
                    )
                },
                bottomBar = {
                    // Don't show bottom bar when navigating to any product screens
                    val currentRouteValue = currentRoute.value?.destination?.route ?: ""
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
                                            if (it == BottomNavigationScreen.Jobs) {
                                                Badge { Text("1") }
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
                            bottomNavSelection = BottomNavigationScreen.Home
                            // Display "Smile ID" in the top bar instead of "Home" label
                            currentScreenTitle = R.string.app_name
                            ProductSelectionScreen { navController.navigate(it.route) }
                        }
                        composable(BottomNavigationScreen.Jobs.route) {
                            bottomNavSelection = BottomNavigationScreen.Jobs
                            currentScreenTitle = BottomNavigationScreen.Jobs.label
                            OrchestratedJobsScreen(isProduction)
                        }
                        composable(BottomNavigationScreen.Resources.route) {
                            bottomNavSelection = BottomNavigationScreen.Resources
                            currentScreenTitle = BottomNavigationScreen.Resources.label
                            ResourcesScreen()
                        }
                        composable(BottomNavigationScreen.AboutUs.route) {
                            bottomNavSelection = BottomNavigationScreen.AboutUs
                            currentScreenTitle = BottomNavigationScreen.AboutUs.label
                            AboutUsScreen()
                        }
                        composable(ProductScreen.SmartSelfieEnrollment.route) {
                            bottomNavSelection = BottomNavigationScreen.Home
                            currentScreenTitle = ProductScreen.SmartSelfieEnrollment.label
                            val userId = rememberSaveable { randomUserId() }
                            val jobId = rememberSaveable { randomJobId() }
                            SmileID.SmartSelfieEnrollment(
                                userId = userId,
                                jobId = jobId,
                                allowAgentMode = true,
                                showInstructions = true,
                            ) { result ->
                                if (result is SmileIDResult.Success) {
                                    val response = result.data.jobStatusResponse ?: run {
                                        val errorMessage =
                                            "SmartSelfie Enrollment jobStatusResponse is null"
                                        Timber.e(errorMessage)
                                        snackbarHostState.showSnackbar(coroutineScope, errorMessage)
                                        return@SmartSelfieEnrollment
                                    }
                                    val actualResult = response.result as? JobResult.Entry
                                    val message = jobResultMessageBuilder(
                                        jobName = "SmartSelfie Enrollment",
                                        jobComplete = response.jobComplete,
                                        jobSuccess = response.jobSuccess,
                                        code = response.code,
                                        resultCode = actualResult?.resultCode,
                                        resultText = actualResult?.resultText,
                                        suffix = "The User ID has been copied to your clipboard",
                                    )
                                    Timber.d("$message: $result")
                                    clipboardManager.setText(AnnotatedString(userId))
                                    snackbarHostState.showSnackbar(coroutineScope, message)
                                    coroutineScope.launch {
                                        DataStoreRepository.addJob(
                                            partnerId = SmileID.config.partnerId,
                                            isProduction = isProduction,
                                            job = response.toJob(userId, jobId, true),
                                        )
                                    }
                                } else if (result is SmileIDResult.Error) {
                                    val th = result.throwable
                                    val message = "SmartSelfie Enrollment error: ${th.message}"
                                    Timber.e(th, message)
                                    snackbarHostState.showSnackbar(coroutineScope, message)
                                }
                                navController.popBackStack()
                            }
                        }
                        composable(ProductScreen.SmartSelfieAuthentication.route) {
                            bottomNavSelection = BottomNavigationScreen.Home
                            currentScreenTitle = ProductScreen.SmartSelfieAuthentication.label
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
                            bottomNavSelection = BottomNavigationScreen.Home
                            currentScreenTitle = ProductScreen.SmartSelfieAuthentication.label
                            val userId = it.arguments?.getString("userId")!!
                            val jobId = rememberSaveable { randomJobId() }
                            SmileID.SmartSelfieAuthentication(
                                userId = userId,
                                jobId = jobId,
                                allowAgentMode = true,
                            ) { result ->
                                if (result is SmileIDResult.Success) {
                                    val response = result.data.jobStatusResponse ?: run {
                                        val errorMessage =
                                            "SmartSelfie Authentication jobStatusResponse is null"
                                        Timber.e(errorMessage)
                                        snackbarHostState.showSnackbar(coroutineScope, errorMessage)
                                        return@SmartSelfieAuthentication
                                    }
                                    val actualResult = response.result as? JobResult.Entry
                                    val message = jobResultMessageBuilder(
                                        jobName = "SmartSelfie Authentication",
                                        jobComplete = response.jobComplete,
                                        jobSuccess = response.jobSuccess,
                                        code = response.code,
                                        resultCode = actualResult?.resultCode,
                                        resultText = actualResult?.resultText,
                                    )
                                    snackbarHostState.showSnackbar(coroutineScope, message)
                                    Timber.d("$message: $result")
                                    coroutineScope.launch {
                                        DataStoreRepository.addJob(
                                            partnerId = SmileID.config.partnerId,
                                            isProduction = isProduction,
                                            job = response.toJob(userId, jobId, true),
                                        )
                                    }
                                } else if (result is SmileIDResult.Error) {
                                    val th = result.throwable
                                    val message = "SmartSelfie Authentication error: ${th.message}"
                                    Timber.e(th, message)
                                    snackbarHostState.showSnackbar(coroutineScope, message)
                                }
                                navController.popBackStack()
                            }
                        }
                        composable(ProductScreen.EnhancedKyc.route) {
                            bottomNavSelection = BottomNavigationScreen.Home
                            currentScreenTitle = ProductScreen.EnhancedKyc.label
                            OrchestratedEnhancedKycScreen { result ->
                                if (result is SmileIDResult.Success) {
                                    val resultData = result.data.response
                                    val message = jobResultMessageBuilder(
                                        jobName = "Enhanced KYC",
                                        jobComplete = true,
                                        jobSuccess = true,
                                        code = null,
                                        resultCode = resultData.resultCode,
                                        resultText = resultData.resultText,
                                    )
                                    snackbarHostState.showSnackbar(coroutineScope, message)
                                    coroutineScope.launch {
                                        DataStoreRepository.addJob(
                                            partnerId = SmileID.config.partnerId,
                                            isProduction = isProduction,
                                            job = resultData.toJob(),
                                        )
                                    }
                                } else if (result is SmileIDResult.Error) {
                                    val th = result.throwable
                                    val message = "Enhanced KYC error: ${th.message}"
                                    Timber.e(th, message)
                                    snackbarHostState.showSnackbar(coroutineScope, message)
                                }
                                navController.popBackStack()
                            }
                        }
                        composable(ProductScreen.BiometricKyc.route) {
                            bottomNavSelection = BottomNavigationScreen.Home
                            currentScreenTitle = ProductScreen.BiometricKyc.label
                            var idInfo: IdInfo? by remember { mutableStateOf(null) }
                            if (idInfo == null) {
                                IdTypeSelectorAndFieldInputScreen(
                                    jobType = JobType.BiometricKyc,
                                    onResult = { idInfo = it },
                                )
                            }
                            idInfo?.let {
                                val url = URL("https://smileidentity.com/privacy-policy")
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
                                    if (result is SmileIDResult.Success) {
                                        val response = result.data.jobStatusResponse
                                        val actualResult = response.result as? JobResult.Entry
                                        Timber.d("Biometric KYC Result: $result")
                                        val message = jobResultMessageBuilder(
                                            jobName = "Biometric KYC",
                                            jobComplete = response.jobComplete,
                                            jobSuccess = response.jobSuccess,
                                            code = response.code,
                                            resultCode = actualResult?.resultCode,
                                            resultText = actualResult?.resultText,
                                        )
                                        snackbarHostState.showSnackbar(coroutineScope, message)
                                        coroutineScope.launch {
                                            DataStoreRepository.addJob(
                                                partnerId = SmileID.config.partnerId,
                                                isProduction = isProduction,
                                                job = response.toJob(userId, jobId),
                                            )
                                        }
                                    } else if (result is SmileIDResult.Error) {
                                        val th = result.throwable
                                        val message = "Biometric KYC error: ${th.message}"
                                        Timber.e(th, message)
                                        snackbarHostState.showSnackbar(coroutineScope, message)
                                    }
                                    navController.popBackStack()
                                }
                            }
                        }
                        composable(ProductScreen.DocumentVerification.route) {
                            bottomNavSelection = BottomNavigationScreen.Home
                            currentScreenTitle = ProductScreen.DocumentVerification.label
                            DocumentVerificationIdTypeSelector { country, idType ->
                                navController.navigate(
                                    "${ProductScreen.DocumentVerification.route}/$country/$idType",
                                ) { popUpTo(ProductScreen.DocumentVerification.route) }
                            }
                        }
                        composable(
                            ProductScreen.DocumentVerification.route + "/{countryCode}/{idType}",
                        ) {
                            val userId = rememberSaveable { randomUserId() }
                            val jobId = rememberSaveable { randomJobId() }
                            val documentType = Document(
                                it.arguments?.getString("countryCode")!!,
                                it.arguments?.getString("idType")!!,
                            )
                            SmileID.DocumentVerification(
                                userId = userId,
                                jobId = jobId,
                                idType = documentType,
                                showInstructions = true,
                            ) { result ->
                                if (result is SmileIDResult.Success) {
                                    val response = result.data.jobStatusResponse
                                    val actualResult = response.result as? JobResult.Entry
                                    val message = jobResultMessageBuilder(
                                        jobName = "Document Verification",
                                        jobComplete = response.jobComplete,
                                        jobSuccess = response.jobSuccess,
                                        code = response.code,
                                        resultCode = actualResult?.resultCode,
                                        resultText = actualResult?.resultText,
                                    )
                                    Timber.d("$message: $result")
                                    snackbarHostState.showSnackbar(coroutineScope, message)
                                    coroutineScope.launch {
                                        DataStoreRepository.addJob(
                                            partnerId = SmileID.config.partnerId,
                                            isProduction = isProduction,
                                            job = response.toJob(userId, jobId),
                                        )
                                    }
                                } else if (result is SmileIDResult.Error) {
                                    val th = result.throwable
                                    val message = "Document Verification error: ${th.message}"
                                    Timber.e(th, message)
                                    snackbarHostState.showSnackbar(coroutineScope, message)
                                }
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
