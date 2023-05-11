package com.smileidentity.models

import android.os.Parcelable
import androidx.annotation.FloatRange
import kotlinx.parcelize.Parcelize

/**
 * Represents a document type that can be used for Document Verification.
 */
// TODO: Should this be consolidated with IdType (used for Enhanced KYC)? This would entail not
//  applying orientation to certain types (i.e. BVN, Phone number). It would also entail adding
//  supportsBasicKyc, supportsEnhancedKyc, supportsBiometricKyc, and supportsDocV as properties
@Parcelize
data class Document(
    val countryCode: String,
    val documentType: String,
    @FloatRange(from = 0.0) val aspectRatio: Float = 0f,
) : Parcelable
