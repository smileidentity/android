package com.smileidentity.sample

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons.Filled
import androidx.compose.material.icons.Icons.Outlined
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.ui.graphics.vector.ImageVector

sealed interface Screen {
    val route: String
    val label: Int
}

sealed class ProductScreen(
    override val route: String,
    @StringRes override val label: Int,
    @DrawableRes val icon: Int,
) : Screen {
    object SmartSelfieEnrollment : ProductScreen(
        "smart_selfie_enrollment",
        com.smileidentity.R.string.si_smart_selfie_enrollment_product_name,
        com.smileidentity.R.drawable.si_smart_selfie_instructions_hero,
    )

    object SmartSelfieAuthentication : ProductScreen(
        "smart_selfie_authentication",
        com.smileidentity.R.string.si_smart_selfie_authentication_product_name,
        com.smileidentity.R.drawable.si_smart_selfie_instructions_hero,
    )

    object EnhancedKyc : ProductScreen(
        "enhanced_kyc",
        R.string.enhanced_kyc_product_name,
        R.drawable.enhanced_kyc,
    )

    object DocumentVerification : ProductScreen(
        "document_verification",
        com.smileidentity.R.string.si_doc_v_product_name,
        com.smileidentity.R.drawable.si_doc_v_instructions_hero,
    )
}

sealed class BottomNavigationScreen(
    override val route: String,
    @StringRes override val label: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
) : Screen {
    object Home : BottomNavigationScreen("home", R.string.home, Filled.Home, Outlined.Home)
    object Resources :
        BottomNavigationScreen("resources", R.string.resources, Filled.Info, Outlined.Info)

    object AboutUs :
        BottomNavigationScreen("about_us", R.string.about_us, Filled.Settings, Outlined.Settings)
}

object BottomNavigationScreenSaver : Saver<BottomNavigationScreen, String> {
    override fun restore(value: String): BottomNavigationScreen = when (value) {
        BottomNavigationScreen.Home.route -> BottomNavigationScreen.Home
        BottomNavigationScreen.Resources.route -> BottomNavigationScreen.Resources
        BottomNavigationScreen.AboutUs.route -> BottomNavigationScreen.AboutUs
        else -> throw IllegalArgumentException("Unknown route: $value")
    }

    override fun SaverScope.save(value: BottomNavigationScreen): String = value.route
}
