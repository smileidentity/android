package com.smileidentity.sample

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons.Filled
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults.filterChipColors
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.smallTopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.smileidentity.networking.SmileIdentity
import com.smileidentity.ui.compose.SmartSelfieRegistrationScreen
import com.smileidentity.ui.core.SmartSelfieResult
import com.smileidentity.ui.theme.SmileIdentityTheme
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    var bottomNavSelection: Screens by remember { mutableStateOf(Screens.Home) }
    val bottomNavItems = listOf(Screens.Home, Screens.Resources, Screens.AboutUs)
    SmileIdentityTheme {
        Surface(
            color = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground,
        ) {
            var currentScreenTitle by remember { mutableStateOf(R.string.app_name) }
            Scaffold(
                topBar = {
                    var isProduction by remember { mutableStateOf(false) }
                    TopAppBar(
                        title = {
                            Text(
                                stringResource(currentScreenTitle),
                                color = MaterialTheme.colorScheme.onPrimary,
                            )
                        },
                        navigationIcon = {
                            if (navController.previousBackStackEntry != null) {
                                IconButton(onClick = { navController.navigateUp() }) {
                                    Icon(
                                        imageVector = Filled.ArrowBack,
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
                                    SmileIdentity.setEnvironment(useSandbox = !isProduction)
                                },
                                leadingIcon = {
                                    if (isProduction) {
                                        Icon(
                                            imageVector = Filled.Warning,
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
                                colors = filterChipColors(
                                    labelColor = MaterialTheme.colorScheme.onPrimary,
                                    selectedContainerColor = MaterialTheme.colorScheme.errorContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onErrorContainer,
                                    selectedLeadingIconColor = MaterialTheme.colorScheme.onErrorContainer,
                                ),
                            )
                        },
                        colors = smallTopAppBarColors(MaterialTheme.colorScheme.primary),
                    )
                },
                bottomBar = {
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
                                        popUpTo(Screens.Home.route)
                                        launchSingleTop = true
                                    }
                                },
                            )
                        }
                    }
                },
                content = {
                    NavHost(navController, Screens.Home.route, Modifier.padding(it)) {
                        composable(Screens.Home.route) {
                            bottomNavSelection = Screens.Home
                            // Display "Smile Identity" in the top bar instead of "Home" label
                            currentScreenTitle = R.string.app_name
                            ProductSelectionScreen { navController.navigate(it.route) }
                        }
                        composable(Screens.Resources.route) {
                            bottomNavSelection = Screens.Resources
                            currentScreenTitle = Screens.Resources.label
                            ResourcesScreen()
                        }
                        composable(Screens.AboutUs.route) {
                            bottomNavSelection = Screens.AboutUs
                            currentScreenTitle = Screens.AboutUs.label
                            AboutUsScreen()
                        }
                        composable(Screens.SmartSelfieRegistration.route) {
                            bottomNavSelection = Screens.Home
                            currentScreenTitle = Screens.SmartSelfieRegistration.label
                            val context = LocalContext.current
                            SmileIdentity.SmartSelfieRegistrationScreen(
                                agentMode = true,
                                manualCaptureMode = true,
                            ) { result ->
                                if (result is SmartSelfieResult.Success) {
                                    val message = "SmartSelfie Registration success"
                                    context.toast(message)
                                    Timber.d("$message: $result")
                                } else if (result is SmartSelfieResult.Error) {
                                    val message = "SmartSelfie Registration error"
                                    context.toast(message)
                                    Timber.e(result.throwable, message)
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
