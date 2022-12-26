package com.smileidentity.sample

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons.Filled
import androidx.compose.material.icons.Icons.Outlined
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.smileidentity.ui.theme.SmileIdentityTheme

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
                    var checked by remember { mutableStateOf(false) }
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
                            IconToggleButton(
                                checked = checked,
                                onCheckedChange = { checked = it },
                            ) {
                                val icon = if (checked) Filled.PlayArrow else Outlined.PlayArrow
                                Icon(icon, null, tint = MaterialTheme.colorScheme.onPrimary)
                            }
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
                                    navController.navigate(it.route) { popUpTo(Screens.Home.route) }
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
                        composable(Screens.SmartSelfie.route) {
                            bottomNavSelection = Screens.Home
                            currentScreenTitle = Screens.SmartSelfie.label
                            SelfieCaptureScreen()
                        }
                    }
                },
            )
        }
    }
}
