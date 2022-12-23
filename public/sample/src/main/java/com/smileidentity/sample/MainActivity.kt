package com.smileidentity.sample

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement.SpaceAround
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Card
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
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.smileidentity.ui.compose.SelfieCaptureOrPermissionScreen
import com.smileidentity.ui.core.SelfieCaptureResult
import com.smileidentity.ui.theme.SmileIdentityTheme
import timber.log.Timber

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MainScreen() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    SmileIdentityTheme {
        Surface(
            color = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground,
        ) {
            var currentScreen by remember { mutableStateOf(R.string.app_name) }
            Scaffold(
                topBar = {
                    var checked by remember { mutableStateOf(false) }
                    TopAppBar(
                        title = { Text(stringResource(currentScreen), color = MaterialTheme.colorScheme.onPrimary) },
                        navigationIcon = {
                            if (navController.previousBackStackEntry != null) {
                                IconButton(onClick = { navController.navigateUp() }) {
                                    Icon(
                                        imageVector = Icons.Filled.ArrowBack,
                                        contentDescription = stringResource(R.string.back),
                                    )
                                }
                            }
                        },
                        actions = {
                            IconToggleButton(checked = checked, onCheckedChange = { checked = it }) {
                                val icon = if (checked) Icons.Filled.PlayArrow else Icons.Outlined.PlayArrow
                                Icon(icon, null, tint = MaterialTheme.colorScheme.onPrimary)
                            }
                        },
                        colors = smallTopAppBarColors(MaterialTheme.colorScheme.primary),
                    )
                },
                bottomBar = { BottomBar { navController.navigate(it) { popUpTo("home") } } },
                content = {
                    Box(Modifier.padding(it)) {
                        NavHost(navController = navController, startDestination = "home") {
                            composable("home") {
                                currentScreen = R.string.app_name
                                ProductsScreen { selectedProduct ->
                                    when(selectedProduct) {
                                        com.smileidentity.ui.R.string.si_selfie_capture_product_name -> navController.navigate("selfie", )
                                    }
                                }
                            }
                            composable("resources") {
                                currentScreen = R.string.resources
                                ResourcesScreen()
                            }
                            composable("about_us") {
                                currentScreen = R.string.about_us
                                AboutScreen()
                            }
                            composable("selfie") {
                                currentScreen = com.smileidentity.ui.R.string.si_selfie_capture_product_name
                                SelfieCaptureScreen()
                            }
                        }
                    }
                },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun TopBar(@StringRes title: Int = R.string.app_name) {

}

@Preview
@Composable
fun BottomBar(onDestinationSelected: (String) -> Unit = {}) {
    var selectedItem by remember { mutableStateOf(0) }
    val bottomNavItems = listOf(
        Triple("Home", "home", Icons.Rounded.Home),
        Triple(stringResource(R.string.resources), "resources", Icons.Rounded.Info),
        Triple(stringResource(R.string.about_us), "about_us", Icons.Rounded.Settings),
    )
    NavigationBar {
        bottomNavItems.forEachIndexed { index, item ->
            NavigationBarItem(
                selected = selectedItem == index,
                icon = { Icon(item.third, item.first) },
                label = { Text(item.first) },
                onClick = {
                    selectedItem = index
                    onDestinationSelected(item.second)
                },
            )
        }
    }
}

@Preview
@Composable
fun ProductsScreen(onProductSelected: (Int) -> Unit = {}) {
    var selected by remember { mutableStateOf(-1) }
    val products = listOf(com.smileidentity.ui.R.string.si_selfie_capture_product_name)
    when (selected) {
        -1 -> SelectionScreen(products) {
            selected = it
            onProductSelected(it)
        }
        com.smileidentity.ui.R.string.si_selfie_capture_product_name -> SelfieCaptureScreen()
        else -> Text("Unknown screen: $selected")
    }
}

@Preview
@Composable
fun SelectionScreen(
    products: List<Int> = listOf(),
    onProductSelected: (Int) -> Unit = {},
) {
    Column(
        horizontalAlignment = CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Text(
            stringResource(R.string.test_our_products),
            style = MaterialTheme.typography.headlineSmall,
        )
        Spacer(modifier = Modifier.height(16.dp))
        LazyVerticalGrid(columns = GridCells.Fixed(2)) {
            items(products) {
                Card(modifier = Modifier
                    .size(96.dp)
                    .clickable(onClick = { onProductSelected(it) })) {
                    Column(
                        horizontalAlignment = CenterHorizontally,
                        verticalArrangement = SpaceAround,
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        Image(
                            Icons.Default.Face,
                            stringResource(R.string.product_name_icon, stringResource(it)),
                        )
                        Text(stringResource(it), textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun SelfieCaptureScreen() {
    val context = LocalContext.current
    SelfieCaptureOrPermissionScreen(true) {
        if (it is SelfieCaptureResult.Success) {
            val message = "Image captured successfully: ${it.selfieFile}"
            context.toast(message)
            Timber.d(message)
        } else if (it is SelfieCaptureResult.Error) {
            val message = "Image capture error: $it"
            context.toast(message)
            Timber.e(it.throwable, message)
        }
    }
}

