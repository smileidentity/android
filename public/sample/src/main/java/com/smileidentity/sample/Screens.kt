package com.smileidentity.sample

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Face
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    @StringRes val label: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
) {
    object Home : Screen("home", R.string.home, Icons.Filled.Home, Icons.Outlined.Home)
    object Resources : Screen("resources", R.string.resources, Icons.Filled.Info, Icons.Outlined.Info)
    object AboutUs : Screen("about_us", R.string.about_us, Icons.Filled.Settings, Icons.Outlined.Settings)
    object SmartSelfie : Screen(
        "smart_selfie",
        com.smileidentity.ui.R.string.si_selfie_capture_product_name,
        // TODO: Replace icons with Smile Identity branded icons
        Icons.Filled.Face,
        Icons.Outlined.Face,
    )
}
