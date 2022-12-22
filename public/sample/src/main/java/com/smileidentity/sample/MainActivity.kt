package com.smileidentity.sample

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.centerAlignedTopAppBarColors
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
import com.smileidentity.ui.fragment.SelfieFragment
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
            Scaffold(
                topBar = { TopBar() },
                bottomBar = { BottomBar { navController.navigate(it) } },
                content = {
                    Box(Modifier.padding(it)) {
                        NavHost(navController = navController, startDestination = "home") {
                            composable("home") { ProductSelectionScreen() }
                            composable("resources") { ResourcesScreen() }
                            composable("about") { AboutScreen() }
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
fun TopBar() {
    var checked by remember { mutableStateOf(false) }
    TopAppBar(
        title = {
            Text(
                stringResource(R.string.app_name),
                color = MaterialTheme.colorScheme.onPrimary,
            )
        },
        actions = {
            IconToggleButton(checked = checked, onCheckedChange = { checked = it }) {
                val icon = if (checked) Icons.Filled.PlayArrow else Icons.Outlined.PlayArrow
                Icon(icon, null, tint = MaterialTheme.colorScheme.onPrimary)
            }
        },
        colors = centerAlignedTopAppBarColors(MaterialTheme.colorScheme.primary),
    )
}

@Preview
@Composable
fun BottomBar(onDestinationSelected: (String) -> Unit = {}) {
    var selectedItem by remember { mutableStateOf(0) }
    val bottomNavItems = listOf(
        Triple("Home", "home", Icons.Rounded.Home),
        Triple("Resources", "resources", Icons.Rounded.Info),
        Triple("About Us", "about", Icons.Rounded.Settings),
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

@Composable
fun MainContent() {
    val context = LocalContext.current
    SelfieCaptureOrPermissionScreen {
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

@Preview
@Composable
fun ProductSelectionScreen() {
    val context = LocalContext.current as MainActivity
    val products: List<Pair<String, () -> Unit>> = listOf(
        // TODO: Use the Compose version (SelfieScreen)
        // This is not the right way to do this, but it's a quick way to get the demo working
        Pair("SmartSelfie™ Authentication") {
            context.supportFragmentManager.beginTransaction().add(
                android.R.id.content,
                SelfieFragment.newInstance { context.toast(it.toString()) },
            ).commit()
        },
    )
    Column(
        horizontalAlignment = CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Text("Test Our Products", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))
        LazyVerticalGrid(columns = GridCells.Fixed(2)) {
            items(products) {
                Card(modifier = Modifier.size(96.dp).clickable(onClick = it.second)) {
                    Column(
                        horizontalAlignment = CenterHorizontally,
                        verticalArrangement = SpaceAround,
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        Image(imageVector = Icons.Default.Face, contentDescription = null)
                        Text(text = it.first, textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun ResourcesScreen() {
    val resources = listOf(
        Pair("Explore our documentation", "Read everything related to our solution stack"),
        Pair("Privacy Policy", "Learn more about how we handle data"),
        Pair("View FAQs", "Explore frequently asked questions"),
        Pair("Supported ID types and documents", "See our coverage range across the continent"),
    )
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        val context = LocalContext.current
        resources.forEach {
            ListItem(
                headlineText = { Text(it.first) },
                supportingText = { Text(it.second) },
                trailingContent = { Icon(Icons.Default.ArrowForward, null) },
                modifier = Modifier.clickable { context.toast(it.first) },
            )
            Divider()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun AboutScreen() {
    val abouts = listOf(
        Pair("Who we are", Icons.Default.Info),
        Pair("Visit our website", Icons.Default.Star),
        Pair("Contact support", Icons.Default.Email),
    )
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        val context = LocalContext.current
        abouts.forEach {
            ListItem(
                headlineText = { Text(it.first) },
                leadingContent = { Icon(it.second, null) },
                trailingContent = { Icon(Icons.Default.ArrowForward, null) },
                modifier = Modifier.clickable { context.toast(it.first) },
            )
            Divider()
        }
    }
}

private fun Context.toast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}
