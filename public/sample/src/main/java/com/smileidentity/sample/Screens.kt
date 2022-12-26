package com.smileidentity.sample

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons.Filled
import androidx.compose.material.icons.Icons.Outlined
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
    object Home : Screen("home", R.string.home, Filled.Home, Outlined.Home)
    object Resources : Screen("resources", R.string.resources, Filled.Info, Outlined.Info)
    object AboutUs : Screen("about_us", R.string.about_us, Filled.Settings, Outlined.Settings)
    object SmartSelfie : Screen(
        "smart_selfie",
        com.smileidentity.ui.R.string.si_selfie_capture_product_name,
        // TODO: Replace icons with Smile Identity branded icons
        Filled.Face,
        Outlined.Face,
    )
}
