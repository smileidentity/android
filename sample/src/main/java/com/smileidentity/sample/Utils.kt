package com.smileidentity.sample

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import com.smileidentity.sample.compose.SearchableInputFieldItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

fun Context.toast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}

fun Context.toast(@StringRes message: Int) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}

fun SnackbarHostState.showSnackbar(
    scope: CoroutineScope,
    message: String,
    actionLabel: String? = null,
    action: (() -> Unit)? = null,
) {
    scope.launch {
        val result = showSnackbar(message, actionLabel, withDismissAction = true)
        if (result == SnackbarResult.ActionPerformed) {
            action?.invoke()
        }
    }
}

val countryDetails = mapOf(
    "AO" to SearchableInputFieldItem("AO", "Angola", "🇦🇴"),
    "BF" to SearchableInputFieldItem("BF", "Burkina Faso", "🇧🇫"),
    "BI" to SearchableInputFieldItem("BI", "Burundi", "🇧🇮"),
    "BJ" to SearchableInputFieldItem("BJ", "Benin", "🇧🇯"),
    "BW" to SearchableInputFieldItem("BW", "Botswana", "🇧🇼"),
    "CD" to SearchableInputFieldItem("CD", "Congo (DRC)", "🇨🇩"),
    "CF" to SearchableInputFieldItem("CF", "Central African Republic", "🇨🇫"),
    "CG" to SearchableInputFieldItem("CG", "Congo", "🇨🇬"),
    "CI" to SearchableInputFieldItem("CI", "Côte d'Ivoire", "🇨🇮"),
    "CM" to SearchableInputFieldItem("CM", "Cameroon", "🇨🇲"),
    "CV" to SearchableInputFieldItem("CV", "Cabo Verde", "🇨🇻"),
    "DJ" to SearchableInputFieldItem("DJ", "Djibouti", "🇩🇯"),
    "EG" to SearchableInputFieldItem("EG", "Egypt", "🇪🇬"),
    "EH" to SearchableInputFieldItem("EH", "Western Sahara", "🇪🇭"),
    "ER" to SearchableInputFieldItem("ER", "Eritrea", "🇪🇷"),
    "ET" to SearchableInputFieldItem("ET", "Ethiopia", "🇪🇹"),
    "GA" to SearchableInputFieldItem("GA", "Gabon", "🇬🇦"),
    "GH" to SearchableInputFieldItem("GH", "Ghana", "🇬🇭"),
    "GM" to SearchableInputFieldItem("GM", "Gambia", "🇬🇲"),
    "GN" to SearchableInputFieldItem("GN", "Guinea", "🇬🇳"),
    "GQ" to SearchableInputFieldItem("GQ", "Equatorial Guinea", "🇬🇶"),
    "GW" to SearchableInputFieldItem("GW", "Guinea-Bissau", "🇬🇼"),
    "KE" to SearchableInputFieldItem("KE", "Kenya", "🇰🇪"),
    "KM" to SearchableInputFieldItem("KM", "Comoros", "🇰🇲"),
    "LR" to SearchableInputFieldItem("LR", "Liberia", "🇱🇷"),
    "LS" to SearchableInputFieldItem("LS", "Lesotho", "🇱🇸"),
    "LY" to SearchableInputFieldItem("LY", "Libya", "🇱🇾"),
    "MA" to SearchableInputFieldItem("MA", "Morocco", "🇲🇦"),
    "MG" to SearchableInputFieldItem("MG", "Madagascar", "🇲🇬"),
    "ML" to SearchableInputFieldItem("ML", "Mali", "🇲🇱"),
    "MR" to SearchableInputFieldItem("MR", "Mauritania", "🇲🇷"),
    "MU" to SearchableInputFieldItem("MU", "Mauritius", "🇲🇺"),
    "MW" to SearchableInputFieldItem("MW", "Malawi", "🇲🇼"),
    "MZ" to SearchableInputFieldItem("MZ", "Mozambique", "🇲🇿"),
    "NA" to SearchableInputFieldItem("NA", "Namibia", "🇳🇦"),
    "NE" to SearchableInputFieldItem("NE", "Niger", "🇳🇪"),
    "NG" to SearchableInputFieldItem("NG", "Nigeria", "🇳🇬"),
    "RW" to SearchableInputFieldItem("RW", "Rwanda", "🇷🇼"),
    "SC" to SearchableInputFieldItem("SC", "Seychelles", "🇸🇨"),
    "SD" to SearchableInputFieldItem("SD", "Sudan", "🇸🇩"),
    "SL" to SearchableInputFieldItem("SL", "Sierra Leone", "🇸🇱"),
    "SN" to SearchableInputFieldItem("SN", "Senegal", "🇸🇳"),
    "SO" to SearchableInputFieldItem("SO", "Somalia", "🇸🇴"),
    "SS" to SearchableInputFieldItem("SS", "South Sudan", "🇸🇸"),
    "ST" to SearchableInputFieldItem("ST", "São Tomé and Príncipe", "🇸🇹"),
    "SZ" to SearchableInputFieldItem("SZ", "Eswatini", "🇸🇿"),
    "TD" to SearchableInputFieldItem("TD", "Chad", "🇹🇩"),
    "TG" to SearchableInputFieldItem("TG", "Togo", "🇹🇬"),
    "TN" to SearchableInputFieldItem("TN", "Tunisia", "🇹🇳"),
    "TZ" to SearchableInputFieldItem("TZ", "Tanzania", "🇹🇿"),
    "UG" to SearchableInputFieldItem("UG", "Uganda", "🇺🇬"),
    "ZA" to SearchableInputFieldItem("ZA", "South Africa", "🇿🇦"),
    "ZM" to SearchableInputFieldItem("ZM", "Zambia", "🇿🇲"),
    "ZW" to SearchableInputFieldItem("ZW", "Zimbabwe", "🇿🇼"),
)
