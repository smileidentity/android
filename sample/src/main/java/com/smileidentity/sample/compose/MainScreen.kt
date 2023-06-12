package com.smileidentity.sample.compose

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.smileidentity.SmileID
import com.smileidentity.compose.DocumentVerification
import com.smileidentity.compose.SmartSelfieAuthenticationScreen
import com.smileidentity.compose.SmartSelfieEnrollmentScreen
import com.smileidentity.models.Document
import com.smileidentity.randomJobId
import com.smileidentity.randomUserId
import com.smileidentity.results.SmileIDResult
import com.smileidentity.sample.BottomNavigationScreen
import com.smileidentity.sample.ProductScreen
import com.smileidentity.sample.R
import com.smileidentity.sample.Screen
import com.smileidentity.sample.showSnackbar
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Preview
@Composable
fun MainScreen() {
    val coroutineScope = rememberCoroutineScope()
    val navController = rememberNavController()
    val currentRoute = navController
        .currentBackStackEntryFlow
        .collectAsStateWithLifecycle(initialValue = navController.currentBackStackEntry)
    val showUpButton = when (currentRoute.value?.destination?.route) {
        BottomNavigationScreen.Home.route -> false
        else -> true
    }
    var bottomNavSelection: Screen by remember { mutableStateOf(BottomNavigationScreen.Home) }
    val bottomNavItems = listOf(
        BottomNavigationScreen.Home,
        BottomNavigationScreen.Resources,
        BottomNavigationScreen.AboutUs,
    )
    val snackbarHostState = remember { SnackbarHostState() }
    val clipboardManager = LocalClipboardManager.current
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
                    var isProduction by rememberSaveable { mutableStateOf(false) }
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
                                    val imageVector = if (it == bottomNavSelection) {
                                        it.selectedIcon
                                    } else {
                                        it.unselectedIcon
                                    }
                                    Icon(imageVector, stringResource(it.label))
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
                        BottomNavigationScreen.Home.route,
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
                            SmileID.SmartSelfieEnrollmentScreen(
                                userId = userId,
                                allowAgentMode = true,
                            ) { result ->
                                if (result is SmileIDResult.Success) {
                                    val resultData = result.data
                                    val message = StringBuilder("SmartSelfie Enrollment ")
                                    if (resultData.jobStatusResponse.jobComplete) {
                                        if (resultData.jobStatusResponse.jobSuccess) {
                                            message.append("completed successfully. ")
                                        } else {
                                            message.append("completed unsuccessfully. ")
                                        }
                                    } else {
                                        message.append("still pending. ")
                                    }
                                    message.append("The User ID has been copied to your clipboard.")
                                    Timber.d("$message: $result")
                                    clipboardManager.setText(AnnotatedString(userId))
                                    snackbarHostState.showSnackbar(
                                        coroutineScope,
                                        message.toString(),
                                    )
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
                            SmileID.SmartSelfieAuthenticationScreen(
                                userId = it.arguments?.getString("userId")!!,
                                allowAgentMode = true,
                            ) { result ->
                                if (result is SmileIDResult.Success) {
                                    val resultData = result.data
                                    val message = StringBuilder("SmartSelfie Authentication ")
                                    if (resultData.jobStatusResponse.jobComplete) {
                                        if (resultData.jobStatusResponse.jobSuccess) {
                                            message.append("completed successfully. ")
                                        } else {
                                            message.append("completed unsuccessfully. ")
                                        }
                                    } else {
                                        message.append("still pending. ")
                                    }
                                    snackbarHostState.showSnackbar(
                                        coroutineScope,
                                        message.toString(),
                                    )
                                    Timber.d("$message: $result")
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
                            EnhancedKycScreen { result ->
                                if (result is SmileIDResult.Success) {
                                    val message = "Enhanced KYC success"
                                    Timber.d("$message: $result")
                                    snackbarHostState.showSnackbar(coroutineScope, message)
                                } else if (result is SmileIDResult.Error) {
                                    val th = result.throwable
                                    val message = "Enhanced KYC error: ${th.message}"
                                    Timber.e(th, message)
                                    snackbarHostState.showSnackbar(coroutineScope, message)
                                }
                                navController.popBackStack()
                            }
                        }
                        composable(ProductScreen.DocumentVerification.route) {
                            bottomNavSelection = BottomNavigationScreen.Home
                            currentScreenTitle = ProductScreen.DocumentVerification.label
                            // TODO: fetch this from
                            //  https://api.smileidentity.com/v1/products_config once it supports
                            //  signature auth (there should be a story for this for Platform)
                            DocumentVerificationIdTypeSelector(docVTestData) { country, idType ->
                                Timber.v("Selected country: $country, idType: $idType")
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
                                enforcedIdType = documentType,
                            ) { result ->
                                if (result is SmileIDResult.Success) {
                                    val resultData = result.data
                                    val message = StringBuilder("Document Verification ")
                                    if (resultData.jobStatusResponse.jobComplete) {
                                        if (resultData.jobStatusResponse.jobSuccess) {
                                            message.append("completed successfully.")
                                        } else {
                                            message.append("completed unsuccessfully.")
                                        }
                                    } else {
                                        message.append("still pending.")
                                    }
                                    Timber.d("$message: $result")
                                    snackbarHostState.showSnackbar(
                                        coroutineScope,
                                        message.toString(),
                                    )
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
