package com.smileidentity.sample

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons.Filled
import androidx.compose.material.icons.Icons.Outlined
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Face
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screens(
    val route: String,
    @StringRes val label: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
) {
    object Home : Screens("home", R.string.home, Filled.Home, Outlined.Home)
    object Resources : Screens("resources", R.string.resources, Filled.Info, Outlined.Info)
    object AboutUs : Screens("about_us", R.string.about_us, Filled.Settings, Outlined.Settings)
    object SmartSelfieRegistration : Screens(
        "smart_selfie_registration",
        com.smileidentity.R.string.si_smartselfie_registration_product_name,
        // TODO: Replace icons with Smile ID branded icons
        Filled.Face,
        Outlined.Face,
    )

    object SmartSelfieAuthentication : Screens(
        "smart_selfie_authentication",
        com.smileidentity.R.string.si_smartselfie_authentication_product_name,
        // TODO: Replace icons with Smile ID branded icons
        Filled.Face,
        Outlined.Face,
    )

    object EnhancedKyc : Screens(
        "enhanced_kyc",
        R.string.enhanced_kyc_product_name,
        Filled.Person,
        Outlined.Person,
    )
}
