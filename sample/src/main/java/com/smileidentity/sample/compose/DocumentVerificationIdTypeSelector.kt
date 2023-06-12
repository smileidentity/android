package com.smileidentity.sample.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.smileidentity.sample.R
import timber.log.Timber

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

private val idTypeFriendlyNames = mapOf(
    "NATIONAL_ID" to "National ID",
    "NATIONAL_ID_NO_PHOTO" to "National ID (No Photo)",
    "PASSPORT" to "Passport",
    "VOTER_ID" to "Voter ID",
    "SSNIT" to "SSNIT",
    "NEW_VOTER_ID" to "New Voter ID",
    "DRIVERS_LICENSE" to "Driver's License",
    "V_NIN" to "Virtual NIN",
    "CAC" to "CAC",
    "NIN_V2" to "NIN v2",
    "NIN_SLIP" to "NIN Slip",
    "BANK_ACCOUNT" to "Bank Account",
    "TIN" to "TIN",
    "BVN" to "BVN",
    "PHONE_NUMBER" to "Phone Number",
    "ALIEN_CARD" to "Alien Card",
)

val docVTestData = mapOf(
    "ZA" to listOf(
        "NATIONAL_ID",
        "NATIONAL_ID_NO_PHOTO",
    ),
    "UG" to listOf(
        "NATIONAL_ID_NO_PHOTO",
    ),
    "GH" to listOf(
        "SSNIT",
        "VOTER_ID",
        "NEW_VOTER_ID",
        "DRIVERS_LICENSE",
        "PASSPORT",
    ),
    "KE" to listOf(
        "NATIONAL_ID",
        "ALIEN_CARD",
        "PASSPORT",
        "NATIONAL_ID_NO_PHOTO",
    ),
    "NG" to listOf(
        "V_NIN",
        "CAC",
        "VOTER_ID",
        "NIN_V2",
        "DRIVERS_LICENSE",
        "NIN",
        "NIN_SLIP",
        "BANK_ACCOUNT",
        "NATIONAL_ID",
        "TIN",
        "PASSPORT",
    ),
)

/**
 * A composable that allows the user to select a country and ID type for Document Verification.
 *
 * @param idTypes A map of country codes to a list of ID types for that country.
 * @param onIdTypeSelected A callback that is invoked when the user selects a country and ID type.
 */
@Composable
fun DocumentVerificationIdTypeSelector(
    idTypes: Map<String, List<String>>,
    onIdTypeSelected: (String, String) -> Unit,
) {
    // If an unsupported country code is passed in, it will display the country code with no emoji
    val countries by remember {
        derivedStateOf {
            idTypes.keys.map {
                countryDetails[it] ?: SearchableInputFieldItem(
                    it,
                    it,
                )
            }
        }
    }

    var selectedCountry: String? by rememberSaveable { mutableStateOf(null) }
    var selectedIdType: String? by rememberSaveable { mutableStateOf(null) }
    val isContinueEnabled by remember {
        derivedStateOf { selectedCountry != null && selectedIdType != null }
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.Top),
            modifier = Modifier
                .fillMaxHeight()
                .width(320.dp)
                .verticalScroll(rememberScrollState())
                .weight(1f),
        ) {
            Text(
                text = stringResource(R.string.doc_v_info_title),
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = stringResource(R.string.doc_v_info_subtitle),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Light,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 32.dp),
            )
            Text(text = stringResource(R.string.doc_v_select_country), fontWeight = FontWeight.Bold)
            SearchableInputField(
                fieldLabel = stringResource(R.string.doc_v_country_search_field_hint),
                selectedItem = countryDetails[selectedCountry],
                unfilteredItems = countries,
                onItemSelected = {
                    Timber.v("Selected: ${it.displayName}")
                    selectedCountry = it.key
                    selectedIdType = null
                },
                modifier = Modifier
                    .fillMaxWidth(),
            )

            if (selectedCountry == null) {
                val instructions = listOf(
                    Triple(
                        R.drawable.doc_v_info_government_issued,
                        R.string.doc_v_info_government_issued_title,
                        R.string.doc_v_info_government_issued_subtitle,
                    ),
                    Triple(
                        R.drawable.doc_v_info_encrypted,
                        R.string.doc_v_info_encrypted_title,
                        R.string.doc_v_info_encrypted_subtitle,
                    ),
                )
                Spacer(modifier = Modifier.height(64.dp))
                instructions.forEach { (imageId, title, subtitle) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                    ) {
                        Image(
                            painter = painterResource(id = imageId),
                            contentDescription = null,
                        )
                        Column(modifier = Modifier.padding(start = 16.dp)) {
                            Text(
                                text = stringResource(title),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = stringResource(subtitle),
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                    Spacer(modifier = Modifier.size(24.dp))
                }
            }

            selectedCountry?.let { selectedCountry ->
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.doc_v_select_id_type),
                    fontWeight = FontWeight.Bold,
                )
                val idTypesForCountry = idTypes[selectedCountry]
                idTypesForCountry?.forEach { idType ->
                    val selected = selectedIdType == idType
                    val onClick = {
                        Timber.v("Selected: $idType")
                        selectedIdType = idType
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .selectable(
                                selected = selected,
                                onClick = onClick,
                                role = Role.RadioButton,
                            )
                            .padding(vertical = 2.dp),
                    ) {
                        RadioButton(
                            selected = selected,
                            onClick = onClick,
                        )
                        // If ID type ID is not in the friendly name map, display the raw ID type ID
                        Text(text = idTypeFriendlyNames[idType] ?: idType)
                    }
                }
            }
        }
        Button(
            onClick = { onIdTypeSelected(selectedCountry!!, selectedIdType!!) },
            enabled = isContinueEnabled,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = stringResource(R.string.cont))
        }
    }
}
