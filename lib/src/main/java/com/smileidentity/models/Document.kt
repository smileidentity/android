package com.smileidentity.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Represents a document type that can be used for Document Verification
 */
// TODO: Should this be consolidated with IdType (used for Enhanced KYC)? This would entail not
//  applying orientation to certain types (i.e. BVN, Phone number). It would also entail adding
//  supportsBasicKyc, supportsEnhancedKyc, supportsBiometricKyc, and supportsDocV as properties
@Suppress("MemberVisibilityCanBePrivate", "unused")
@Parcelize
data class Document(
    val countryCode: String,
    val documentType: String,
    val orientation: Orientation = Orientation.Landscape,
) : Parcelable {
    // TODO: go through each document and ensure orientation is correct
    enum class Orientation {
        Portrait,
        Landscape,
    }

    companion object {
        // TODO: List of all documents?
        // TODO: Map of countries to their respective documents?
    }

    // TODO: Still need to add European and North American countries

    // TODO: Should these be getters so that they're not instantiated until they're used for
    //  better performance?
    object Algeria {
        const val countryCode = "DZ"

        @JvmField
        val DriverLicense = Document(countryCode, "DRIVERS_LICENSE")

        @JvmField
        val NationalID = Document(countryCode, "NATIONAL_ID")

        @JvmField
        val Passport = Document(countryCode, "PASSPORT")

        // TODO: Should we keep a list of all documents for each country?
    }

    object Angola {
        const val countryCode = "AO"

        @JvmField
        val DriverLicense = Document(countryCode, "DRIVERS_LICENSE")

        @JvmField
        val NationalID = Document(countryCode, "NATIONAL_ID")

        @JvmField
        val Passport = Document(countryCode, "PASSPORT")

        @JvmField
        val RefugeeCard = Document(countryCode, "REFUGEE_CARD")

        @JvmField
        val ResidentCard = Document(countryCode, "RESIDENT_CARD")

        @JvmField
        val VoterID = Document(countryCode, "VOTER_ID")
    }

    object Benin {
        const val countryCode = "BJ"

        @JvmField
        val DriverLicense = Document(countryCode, "DRIVERS_LICENSE")

        @JvmField
        val EcowasID = Document(countryCode, "ECOWAS_ID")

        @JvmField
        val NationalID = Document(countryCode, "NATIONAL_ID")

        @JvmField
        val Passport = Document(countryCode, "PASSPORT")

        @JvmField
        val ResidentCard = Document(countryCode, "RESIDENT_CARD")

        @JvmField
        val VoterID = Document(countryCode, "VOTER_ID")
    }

    object Botswana {
        const val countryCode = "BW"

        @JvmField
        val DriverLicense = Document(countryCode, "DRIVERS_LICENSE")

        @JvmField
        val NationalID = Document(countryCode, "NATIONAL_ID")

        @JvmField
        val Passport = Document(countryCode, "PASSPORT")

        @JvmField
        val ResidentCard = Document(countryCode, "RESIDENT_CARD")
    }

    object BurkinaFaso {
        const val countryCode = "BF"

        @JvmField
        val NationalID = Document(countryCode, "NATIONAL_ID")

        @JvmField
        val Passport = Document(countryCode, "PASSPORT")

        @JvmField
        val DriverLicense = Document(countryCode, "DRIVERS_LICENSE")

        @JvmField
        val VoterID = Document(countryCode, "VOTER_ID")
    }

    object Burundi {
        const val countryCode = "BI"

        @JvmField
        val Passport = Document(countryCode, "PASSPORT")
    }

    object Cameroon {
        const val countryCode = "CM"

        @JvmField
        val DriverLicense = Document(countryCode, "DRIVERS_LICENSE")

        @JvmField
        val NationalID = Document(countryCode, "NATIONAL_ID")

        @JvmField
        val Passport = Document(countryCode, "PASSPORT")

        @JvmField
        val ResidentCard = Document(countryCode, "RESIDENT_CARD")

        @JvmField
        val VoterID = Document(countryCode, "VOTER_ID")
    }

    object Cabo {
        const val countryCode = "CV"

        @JvmField
        val DriverLicense = Document(countryCode, "DRIVERS_LICENSE")

        @JvmField
        val NationalID = Document(countryCode, "NATIONAL_ID")

        @JvmField
        val Passport = Document(countryCode, "PASSPORT")
    }

    object Chad {
        const val countryCode = "TD"

        @JvmField
        val DriverLicense = Document(countryCode, "DRIVERS_LICENSE")

        @JvmField
        val NationalID = Document(countryCode, "NATIONAL_ID")

        @JvmField
        val Passport = Document(countryCode, "PASSPORT")

        @JvmField
        val VoterID = Document(countryCode, "VOTER_ID")
    }

    object Comoros {
        const val countryCode = "KM"

        @JvmField
        val NationalID = Document(countryCode, "NATIONAL_ID")

        @JvmField
        val Passport = Document(countryCode, "PASSPORT")

        @JvmField
        val ResidentCard = Document(countryCode, "RESIDENT_CARD")
    }

    object Congo {
        const val countryCode = "CG"

        @JvmField
        val DriverLicense = Document(countryCode, "DRIVERS_LICENSE")

        @JvmField
        val NationalID = Document(countryCode, "NATIONAL_ID")

        @JvmField
        val Passport = Document(countryCode, "PASSPORT")

        @JvmField
        val ResidentCard = Document(countryCode, "RESIDENT_CARD")
    }

    object CoteDIvoire {
        const val countryCode = "CI"

        @JvmField
        val AttestationCard = Document(countryCode, "ATTESTATION_CARD")

        @JvmField
        val DriverLicense = Document(countryCode, "DRIVERS_LICENSE")

        @JvmField
        val HealthInsuranceID = Document(countryCode, "HEALTH_INSURANCE_ID")

        @JvmField
        val NationalID = Document(countryCode, "NATIONAL_ID", orientation = Orientation.Portrait)

        @JvmField
        val Passport = Document(countryCode, "PASSPORT")

        @JvmField
        val ResidentCard = Document(countryCode, "RESIDENT_CARD")
    }

    object DemocraticRepublicOfTheCongo {
        const val countryCode = "CD"

        @JvmField
        val DriverLicense = Document(countryCode, "DRIVERS_LICENSE")

        @JvmField
        val Passport = Document(countryCode, "PASSPORT")

        @JvmField
        val VoterID = Document(countryCode, "VOTER_ID")
    }

    object Djibouti {
        const val countryCode = "DJ"

        @JvmField
        val NationalID = Document(countryCode, "NATIONAL_ID")

        @JvmField
        val Passport = Document(countryCode, "PASSPORT")
    }

    object Egypt {
        const val countryCode = "EG"

        @JvmField
        val DriverLicense = Document(countryCode, "DRIVERS_LICENSE")

        @JvmField
        val NationalID = Document(countryCode, "NATIONAL_ID")

        @JvmField
        val Passport = Document(countryCode, "PASSPORT")

        @JvmField
        val ResidentCard = Document(countryCode, "RESIDENT_CARD")
    }

    object EquatorialGuinea {
        const val countryCode = "GQ"

        @JvmField
        val DriverLicense = Document(countryCode, "DRIVERS_LICENSE")

        @JvmField
        val NationalID = Document(countryCode, "NATIONAL_ID")

        @JvmField
        val Passport = Document(countryCode, "PASSPORT")

        @JvmField
        val ResidentCard = Document(countryCode, "RESIDENT_CARD")
    }

    object Eritrea {
        const val countryCode = "ER"

        @JvmField
        val Passport = Document(countryCode, "PASSPORT")
    }

    object Eswatini {
        const val countryCode = "SZ"

        @JvmField
        val DriverLicense = Document(countryCode, "DRIVERS_LICENSE")

        @JvmField
        val NationalID = Document(countryCode, "NATIONAL_ID")

        @JvmField
        val Passport = Document(countryCode, "PASSPORT")
    }

    object Ethiopia {
        const val countryCode = "ET"

        @JvmField
        val DriverLicense = Document(countryCode, "DRIVERS_LICENSE")

        @JvmField
        val NationalID = Document(countryCode, "NATIONAL_ID")

        @JvmField
        val NonCitizenID = Document(countryCode, "NON_CITIZEN_ID")

        @JvmField
        val Passport = Document(countryCode, "PASSPORT")

        @JvmField
        val ResidentCard = Document(countryCode, "RESIDENT_CARD")
    }

    object Gabon {
        const val countryCode = "GA"

        @JvmField
        val HealthInsuranceID = Document(countryCode, "HEALTH_INSURANCE_ID")

        @JvmField
        val NationalID = Document(countryCode, "NATIONAL_ID")

        @JvmField
        val Passport = Document(countryCode, "PASSPORT")

        @JvmField
        val ResidentCard = Document(countryCode, "RESIDENT_CARD")
    }

    object Gambia {
        const val countryCode = "GM"

        @JvmField
        val DriverLicense = Document(countryCode, "DRIVERS_LICENSE")

        @JvmField
        val EcowasID = Document(countryCode, "ECOWAS_ID")

        @JvmField
        val NationalID = Document(countryCode, "NATIONAL_ID")

        @JvmField
        val Passport = Document(countryCode, "PASSPORT")

        @JvmField
        val ResidentCard = Document(countryCode, "RESIDENT_CARD")

        @JvmField
        val VoterID = Document(countryCode, "VOTER_ID")
    }

    object Ghana {
        const val countryCode = "GH"

        @JvmField
        val DriverLicense = Document(countryCode, "DRIVERS_LICENSE")

        @JvmField
        val GhanaCard = Document(countryCode, "GHANA_CARD")

        @JvmField
        val Passport = Document(countryCode, "PASSPORT")

        @JvmField
        val SSNIT = Document(countryCode, "SSNIT")

        @JvmField
        val NationalID = Document(countryCode, "NATIONAL_ID")

        @JvmField
        val ResidentCard = Document(countryCode, "RESIDENT_CARD")

        @JvmField
        val VoterID = Document(countryCode, "VOTER_ID")
    }

    object Guinea {
        const val countryCode = "GN"

        @JvmField
        val DriverLicense = Document(countryCode, "DRIVERS_LICENSE")

        @JvmField
        val EcowasID = Document(countryCode, "ECOWAS_ID")

        @JvmField
        val NationalID = Document(countryCode, "NATIONAL_ID")

        @JvmField
        val Passport = Document(countryCode, "PASSPORT")

        @JvmField
        val ResidentCard = Document(countryCode, "RESIDENT_CARD")

        @JvmField
        val VoterID = Document(countryCode, "VOTER_ID")
    }

    object GuineaBissau {
        const val countryCode = "GW"

        @JvmField
        val DriverLicense = Document(countryCode, "DRIVERS_LICENSE")

        @JvmField
        val EcowasID = Document(countryCode, "ECOWAS_ID")

        @JvmField
        val NationalID = Document(countryCode, "NATIONAL_ID")

        @JvmField
        val Passport = Document(countryCode, "PASSPORT")
    }

    object Kenya {
        const val countryCode = "KE"

        @JvmField
        val AlienCard = Document(countryCode, "ALIEN_CARD")

        @JvmField
        val DriverLicense = Document(countryCode, "DRIVERS_LICENSE")

        @JvmField
        val NationalID = Document(countryCode, "NATIONAL_ID")

        @JvmField
        val Passport = Document(countryCode, "PASSPORT")

        @JvmField
        val RefugeeID = Document(countryCode, "REFUGEE_ID")
    }

    object Lesotho {
        const val countryCode = "LS"

        @JvmField
        val DriverLicense = Document(countryCode, "DRIVERS_LICENSE")

        @JvmField
        val NationalID = Document(countryCode, "NATIONAL_ID")

        @JvmField
        val Passport = Document(countryCode, "PASSPORT")
    }

    object Liberia {
        const val countryCode = "LR"

        @JvmField
        val DriverLicense = Document(countryCode, "DRIVERS_LICENSE")

        @JvmField
        val NationalID = Document(countryCode, "NATIONAL_ID")

        @JvmField
        val Passport = Document(countryCode, "PASSPORT")
    }

    object Libya {
        const val countryCode = "LY"

        @JvmField
        val DriverLicense = Document(countryCode, "DRIVERS_LICENSE")

        @JvmField
        val Passport = Document(countryCode, "PASSPORT")
    }

    object Madagascar {
        const val countryCode = "MG"

        @JvmField
        val DriverLicense = Document(countryCode, "DRIVERS_LICENSE")

        @JvmField
        val NationalID = Document(countryCode, "NATIONAL_ID")

        @JvmField
        val Passport = Document(countryCode, "PASSPORT")

        @JvmField
        val ResidentCard = Document(countryCode, "RESIDENT_CARD")
    }

    object Malawi {
        const val countryCode = "MW"

        @JvmField
        val DriverLicense = Document(countryCode, "DRIVERS_LICENSE")

        @JvmField
        val NationalID = Document(countryCode, "NATIONAL_ID")

        @JvmField
        val Passport = Document(countryCode, "PASSPORT")

        @JvmField
        val ResidentCard = Document(countryCode, "RESIDENT_CARD")
    }

    object Mali {
        const val countryCode = "ML"

        @JvmField
        val NationalID = Document(countryCode, "NATIONAL_ID")

        @JvmField
        val Passport = Document(countryCode, "PASSPORT")
    }

    object Mauritius {
        const val countryCode = "MU"

        @JvmField
        val NationalID = Document(countryCode, "NATIONAL_ID")

        @JvmField
        val Passport = Document(countryCode, "PASSPORT")
    }

    object Morocco {
        const val countryCode = "MA"

        @JvmField
        val DriverLicense = Document(countryCode, "DRIVERS_LICENSE")

        @JvmField
        val NationalID = Document(countryCode, "NATIONAL_ID")

        @JvmField
        val Passport = Document(countryCode, "PASSPORT")
    }

    object Mozambique {
        const val countryCode = "MZ"

        @JvmField
        val DriverLicense = Document(countryCode, "DRIVERS_LICENSE")

        @JvmField
        val NationalID = Document(countryCode, "NATIONAL_ID")

        @JvmField
        val Passport = Document(countryCode, "PASSPORT")

        @JvmField
        val RefugeeID = Document(countryCode, "REFUGEE_ID")

        @JvmField
        val ResidentCard = Document(countryCode, "RESIDENT_CARD")

        @JvmField
        val VoterID = Document(countryCode, "VOTER_ID")
    }

    object Namibia {
        const val countryCode = "NA"

        @JvmField
        val DriverLicense = Document(countryCode, "DRIVERS_LICENSE")

        @JvmField
        val NationalID = Document(countryCode, "NATIONAL_ID")

        @JvmField
        val Passport = Document(countryCode, "PASSPORT")

        @JvmField
        val VoterID = Document(countryCode, "VOTER_ID")
    }

    object Niger {
        const val countryCode = "NE"

        @JvmField
        val DriverLicense = Document(countryCode, "DRIVERS_LICENSE")

        @JvmField
        val Passport = Document(countryCode, "PASSPORT")
    }

    object Nigeria {
        const val countryCode = "NG"

        @JvmField
        val DriverLicense = Document(countryCode, "DRIVERS_LICENSE")

        @JvmField
        val NationalID = Document(countryCode, "NATIONAL_ID")

        @JvmField
        val Passport = Document(countryCode, "PASSPORT")

        @JvmField
        val ResidentCard = Document(countryCode, "RESIDENT_CARD")

        @JvmField
        val VoterID = Document(countryCode, "VOTER_ID")
    }

    object Rwanda {
        const val countryCode = "RW"

        @JvmField
        val DriverLicense = Document(countryCode, "DRIVERS_LICENSE")

        @JvmField
        val NationalID = Document(countryCode, "NATIONAL_ID")

        @JvmField
        val NonCitizenID = Document(countryCode, "NON_CITIZEN_ID")

        @JvmField
        val Passport = Document(countryCode, "PASSPORT")

        @JvmField
        val RefugeeCard = Document(countryCode, "REFUGEE_CARD")
    }

    object SaoTomeAndPrincipe {
        const val countryCode = "ST"

        @JvmField
        val DriverLicense = Document(countryCode, "DRIVERS_LICENSE")

        @JvmField
        val Passport = Document(countryCode, "PASSPORT")
    }

    object Senegal {
        const val countryCode = "SN"

        @JvmField
        val DriverLicense = Document(countryCode, "DRIVERS_LICENSE")

        @JvmField
        val EcowasID = Document(countryCode, "ECOWAS_ID")

        @JvmField
        val NationalID = Document(countryCode, "NATIONAL_ID")

        @JvmField
        val Passport = Document(countryCode, "PASSPORT")

        @JvmField
        val VoterID = Document(countryCode, "VOTER_ID")
    }

    object Seychelles {
        const val countryCode = "SC"

        @JvmField
        val DriverLicense = Document(countryCode, "DRIVERS_LICENSE")

        @JvmField
        val NationalID = Document(countryCode, "NATIONAL_ID")

        @JvmField
        val Passport = Document(countryCode, "PASSPORT")
    }

    object SierraLeone {
        const val countryCode = "SL"

        @JvmField
        val DriverLicense = Document(countryCode, "DRIVERS_LICENSE")

        @JvmField
        val NationalID = Document(countryCode, "NATIONAL_ID")

        @JvmField
        val Passport = Document(countryCode, "PASSPORT")

        @JvmField
        val VoterID = Document(countryCode, "VOTER_ID")
    }

    object Somalia {
        const val countryCode = "SO"

        @JvmField
        val DriverLicense = Document(countryCode, "DRIVERS_LICENSE")

        @JvmField
        val NationalID = Document(countryCode, "NATIONAL_ID")

        @JvmField
        val Passport = Document(countryCode, "PASSPORT")
    }

    object SouthAfrica {
        const val countryCode = "ZA"

        @JvmField
        val DriverLicense = Document(countryCode, "DRIVERS_LICENSE")

        @JvmField
        val GreenBook = Document(countryCode, "GREEN_BOOK", orientation = Orientation.Portrait)

        @JvmField
        val NationalID = Document(countryCode, "NATIONAL_ID")

        @JvmField
        val Passport = Document(countryCode, "PASSPORT")

        @JvmField
        val ResidentCard = Document(countryCode, "RESIDENT_CARD")
    }

    object SouthSudan {
        const val countryCode = "SS"

        @JvmField
        val DriverLicense = Document(countryCode, "DRIVERS_LICENSE")

        @JvmField
        val NationalID = Document(countryCode, "NATIONAL_ID")

        @JvmField
        val Passport = Document(countryCode, "PASSPORT")

        @JvmField
        val RefugeeID = Document(countryCode, "REFUGEE_ID")
    }

    object Sudan {
        const val countryCode = "SD"

        @JvmField
        val DriverLicense = Document(countryCode, "DRIVERS_LICENSE")

        @JvmField
        val NationalID = Document(countryCode, "NATIONAL_ID")

        @JvmField
        val Passport = Document(countryCode, "PASSPORT")

        @JvmField
        val ResidentCard = Document(countryCode, "RESIDENT_CARD")
    }

    object Togo {
        const val countryCode = "TG"

        @JvmField
        val DriverLicense = Document(countryCode, "DRIVERS_LICENSE")

        @JvmField
        val NationalID = Document(countryCode, "NATIONAL_ID")

        @JvmField
        val Passport = Document(countryCode, "PASSPORT")

        @JvmField
        val ResidentCard = Document(countryCode, "RESIDENT_CARD")

        @JvmField
        val VoterID = Document(countryCode, "VOTER_ID")
    }

    object Tunisia {
        const val countryCode = "TN"

        @JvmField
        val DriverLicense = Document(countryCode, "DRIVERS_LICENSE")

        @JvmField
        val NationalID = Document(countryCode, "NATIONAL_ID")

        @JvmField
        val Passport = Document(countryCode, "PASSPORT")

        @JvmField
        val ResidentCard = Document(countryCode, "RESIDENT_CARD")
    }

    object Uganda {
        const val countryCode = "UG"

        @JvmField
        val AlienID = Document(countryCode, "ALIEN_ID")

        @JvmField
        val DriverLicense = Document(countryCode, "DRIVERS_LICENSE")

        @JvmField
        val NationalID = Document(countryCode, "NATIONAL_ID")

        @JvmField
        val Passport = Document(countryCode, "PASSPORT")

        @JvmField
        val RefugeeCard = Document(countryCode, "REFUGEE_CARD")
    }

    object Tanzania {
        const val countryCode = "TZ"

        @JvmField
        val AlienID = Document(countryCode, "ALIEN_ID")

        @JvmField
        val DriverLicense = Document(countryCode, "DRIVERS_LICENSE")

        @JvmField
        val NationalID = Document(countryCode, "NATIONAL_ID")

        @JvmField
        val Passport = Document(countryCode, "PASSPORT")

        @JvmField
        val ResidentCard = Document(countryCode, "RESIDENT_CARD")

        @JvmField
        val VoterID = Document(countryCode, "VOTER_ID")
    }

    object Zambia {
        const val countryCode = "ZM"

        @JvmField
        val DriverLicense = Document(countryCode, "DRIVERS_LICENSE")

        @JvmField
        val NationalID = Document(countryCode, "NATIONAL_ID")

        @JvmField
        val Passport = Document(countryCode, "PASSPORT")

        @JvmField
        val ResidentCard = Document(countryCode, "RESIDENT_CARD")
    }

    object Zimbabwe {
        const val countryCode = "ZW"

        @JvmField
        val DriverLicense = Document(countryCode, "DRIVERS_LICENSE")

        @JvmField
        val NationalID = Document(countryCode, "NATIONAL_ID")

        @JvmField
        val Passport = Document(countryCode, "PASSPORT")

        @JvmField
        val ResidentCard = Document(countryCode, "RESIDENT_CARD")
    }
}
