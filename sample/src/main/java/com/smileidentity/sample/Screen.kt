package com.smileidentity.sample

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.Icons.Filled
import androidx.compose.material.icons.Icons.Outlined
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed interface Screen {
    val route: String
    val label: Int
}

enum class ProductScreen(
    override val route: String,
    @StringRes override val label: Int,
    @DrawableRes val icon: Int,
) : Screen {
    SmartSelfieEnrollment(
        "smart_selfie_enrollment",
        com.smileidentity.R.string.si_smart_selfie_enrollment_product_name,
        com.smileidentity.R.drawable.si_smart_selfie_instructions_hero,
    ),
    SmartSelfieAuthentication(
        "smart_selfie_authentication",
        com.smileidentity.R.string.si_smart_selfie_authentication_product_name,
        com.smileidentity.R.drawable.si_smart_selfie_instructions_hero,
    ),
    EnhancedKyc(
        "enhanced_kyc",
        R.string.enhanced_kyc_product_name,
        R.drawable.enhanced_kyc,
    ),
    BiometricKyc(
        "biometric_kyc",
        com.smileidentity.R.string.si_biometric_kyc_product_name,
        R.drawable.biometric_kyc,
    ),
    DocumentVerification(
        "document_verification",
        com.smileidentity.R.string.si_doc_v_product_name,
        com.smileidentity.R.drawable.si_doc_v_instructions_hero,
    ),
    BvnConsent(
        "bvn_consent",
        com.smileidentity.R.string.si_bvn_product_name,
        R.drawable.biometric_kyc,
    ),
    EnhancedDocumentVerification(
        "enhanced_docv",
        com.smileidentity.R.string.si_enhanced_docv_product_name,
        com.smileidentity.R.drawable.si_doc_v_instructions_hero,
    ),
}

enum class BottomNavigationScreen(
    override val route: String,
    @StringRes override val label: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
) : Screen {
    Home(
        "home",
        R.string.home,
        Filled.Home,
        Outlined.Home,
    ),
    Jobs(
        "jobs",
        R.string.jobs,
        Icons.AutoMirrored.Filled.List,
        Icons.AutoMirrored.Outlined.List,
    ),
    Resources(
        "resources",
        R.string.resources,
        Filled.Info,
        Outlined.Info,
    ),
    Settings(
        "settings",
        R.string.settings,
        Filled.Settings,
        Outlined.Settings,
    ),
}
