package com.smileidentity.sample

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement.SpaceBetween
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.Start
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
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
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun TopBar() {
    TopAppBar(title = { Text(text = stringResource(R.string.app_name)) })
}

data class BottomNavItem(
    val name: String,
    val route: String,
    val icon: ImageVector,
)

@Preview
@Composable
fun BottomBar(onDestinationSelected: (String) -> Unit = {}) {
    var selectedItem by remember { mutableStateOf(0) }
    val bottomNavItems = listOf(
        BottomNavItem(
            name = "Home",
            route = "home",
            icon = Icons.Rounded.Home,
        ),
        BottomNavItem(
            name = "Resources",
            route = "resources",
            icon = Icons.Rounded.Info,
        ),
        BottomNavItem(
            name = "About Us",
            route = "about",
            icon = Icons.Rounded.Settings,
        ),
    )
    NavigationBar {
        bottomNavItems.forEachIndexed { index, item ->
            NavigationBarItem(
                selected = selectedItem == index,
                icon = { Icon(item.icon, item.name) },
                label = { Text(item.name) },
                onClick = {
                    selectedItem = index
                    onDestinationSelected(item.route)
                }
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
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            Timber.d(message)
        } else if (it is SelfieCaptureResult.Error) {
            val message = "Image capture error: $it"
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            Timber.e(it.throwable, message)
        }
    }
}

@Preview
@Composable
fun ProductSelectionScreen() {
    val products = listOf(
        "Basic KYC",
        "Enhanced KYC",
        "Biometric KYC",
        "Document Verification",
        "SmartSelfie™ Authentication",
    )
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Test Our Products")
        LazyVerticalGrid(columns = GridCells.Fixed(2), contentPadding = PaddingValues(8.dp)) {
            items(products) {
                Card(
                    modifier = Modifier
                        .padding(4.dp)
                        .clickable {
                            Toast
                                .makeText(context, "TODO: launch $it", Toast.LENGTH_SHORT)
                                .show()
                        }
                ) {
                    Image(
                        imageVector = Icons.Default.Face,
                        contentDescription = null,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = it,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun ResourcesScreen() {
    Column(
        horizontalAlignment = Start,
        verticalArrangement = spacedBy(8.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        val context = LocalContext.current
        Row(
            horizontalArrangement = SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    Toast
                        .makeText(context, "Who we are", Toast.LENGTH_LONG)
                        .show()
                }
                .padding(8.dp),
        ) {
            Column {
                Text(text = "Explore our documentation")
                Text(text = "Read everything related to our solution stack")
            }
            Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null)
        }

        Row(
            horizontalArrangement = SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    Toast
                        .makeText(context, "Visit our website", Toast.LENGTH_LONG)
                        .show()
                }
                .padding(8.dp),
        ) {
            Column {
                Text(text = "Privacy Policy")
                Text(text = "Learn more about how we handle data")
            }
            Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null)
        }

        Row(
            horizontalArrangement = SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    Toast
                        .makeText(context, "Contact support", Toast.LENGTH_LONG)
                        .show()
                }
                .padding(8.dp),
        ) {
            Column {
                Text(text = "View FAQs")
                Text(text = "Explore frequently asked questions")
            }
            Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null)
        }

        Row(
            horizontalArrangement = SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    Toast
                        .makeText(context, "Contact support", Toast.LENGTH_LONG)
                        .show()
                }
                .padding(8.dp),
        ) {
            Column {
                Text(text = "Supported ID types and documents")
                Text(text = "See our coverage range across the continent")
            }
            Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null)
        }
    }
}

@Preview
@Composable
fun AboutScreen() {
    Column(
        horizontalAlignment = Start,
        verticalArrangement = spacedBy(8.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        val context = LocalContext.current
        Row(
            horizontalArrangement = SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    Toast
                        .makeText(context, "Who we are", Toast.LENGTH_LONG)
                        .show()
                }
                .padding(8.dp),
        ) {
            Icon(imageVector = Icons.Default.Info, contentDescription = null)
            Text(text = "Who we are")
            Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null)
        }

        Row(
            horizontalArrangement = SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    Toast
                        .makeText(context, "Visit our website", Toast.LENGTH_LONG)
                        .show()
                }
                .padding(8.dp),
        ) {
            Icon(imageVector = Icons.Default.Home, contentDescription = null)
            Text(text = "Visit our website")
            Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null)
        }

        Row(
            horizontalArrangement = SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    Toast
                        .makeText(context, "Contact support", Toast.LENGTH_LONG)
                        .show()
                }
                .padding(8.dp),
        ) {
            Icon(imageVector = Icons.Default.Email, contentDescription = null)
            Text(text = "Contact support")
            Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null)
        }
    }
}
