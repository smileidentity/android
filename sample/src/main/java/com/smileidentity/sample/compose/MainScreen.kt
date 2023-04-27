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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.smileidentity.SmileID
import com.smileidentity.compose.SmartSelfieAuthenticationScreen
import com.smileidentity.compose.SmartSelfieRegistrationScreen
import com.smileidentity.randomUserId
import com.smileidentity.results.EnhancedKycResult
import com.smileidentity.results.SmartSelfieResult
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
        .collectAsState(initial = navController.currentBackStackEntry)
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
                    var isProduction by remember { mutableStateOf(false) }
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
                                            contentDescription = stringResource(R.string.production),
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
                        composable(ProductScreen.SmartSelfieRegistration.route) {
                            bottomNavSelection = BottomNavigationScreen.Home
                            currentScreenTitle = ProductScreen.SmartSelfieRegistration.label
                            val userId = randomUserId()
                            val actionLabel = stringResource(R.string.copy_user_id_snackbar_action)
                            SmileID.SmartSelfieRegistrationScreen(
                                userId = userId,
                                allowAgentMode = true,
                            ) { result ->
                                if (result is SmartSelfieResult.Success) {
                                    val message = "SmartSelfie Registration success"
                                    Timber.d("$message: $result")
                                    snackbarHostState.showSnackbar(
                                        coroutineScope,
                                        message,
                                        actionLabel,
                                    ) {
                                        clipboardManager.setText(AnnotatedString(userId))
                                    }
                                } else if (result is SmartSelfieResult.Error) {
                                    val th = result.throwable
                                    val message = "SmartSelfie Registration error: ${th.message}"
                                    Timber.e(th, message)
                                    snackbarHostState.showSnackbar(coroutineScope, message)
                                }
                                navController.popBackStack()
                            }
                        }
                        composable(ProductScreen.SmartSelfieAuthentication.route) {
                            bottomNavSelection = BottomNavigationScreen.Home
                            currentScreenTitle = ProductScreen.SmartSelfieAuthentication.label
                            var userId by remember { mutableStateOf("") }
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
                                                "${ProductScreen.SmartSelfieAuthentication.route}/$userId",
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
                                if (result is SmartSelfieResult.Success) {
                                    val message = "SmartSelfie Authentication success"
                                    snackbarHostState.showSnackbar(coroutineScope, message)
                                    Timber.d("$message: $result")
                                } else if (result is SmartSelfieResult.Error) {
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
                                if (result is EnhancedKycResult.Success) {
                                    val message = "Enhanced KYC success"
                                    Timber.d("$message: $result")
                                    snackbarHostState.showSnackbar(coroutineScope, message)
                                } else if (result is EnhancedKycResult.Error) {
                                    val th = result.throwable
                                    val message = "Enhanced KYC error: ${th.message}"
                                    Timber.e(th, message)
                                    snackbarHostState.showSnackbar(coroutineScope, message)
                                }
                                navController.popBackStack()
                            }
                        }
                    }
                },
            )
        }
    }
}
